package io.github.karino2.paoogo.goengine.katago

import io.github.karino2.paoogo.goengine.AnalyzeInfo
import io.github.karino2.paoogo.goengine.GoAnalyzer
import io.github.karino2.paoogo.goengine.GoEngine
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
import java.util.LinkedHashMap
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.ligi.gobandroid_hd.logic.GTPHelper
import org.ligi.gobandroid_hd.logic.GoGame

// (cf.) https://github.com/lightvector/KataGo/blob/master/docs/Analysis_Engine.md#responses
@Serializable
data class MoveInfo(
    val move: String,
    val order: Int,
    val prior: Double,
    val pv: List<String>,
    val scoreLead: Double,
    val scoreStdev: Double,
    val visits: Int,
    val winrate: Double,
)

@Serializable
data class AnalysisResponse(
    val moveInfos: List<MoveInfo>,
)

class KataGoNative : GoEngine, GoAnalyzer {
    companion object {
        init{
            System.loadLibrary("katago")
        }

    }
    external fun initNativeHum(threadNum: Int, cfgPath: String, modelPath: String, humanModelPath: String)
    external fun initNative(threadNum: Int, cfgPath: String, modelPath: String)
    override external fun setKomi(komi: Float)
    override external fun clearBoard()
    override external fun setBoardSize(size: Int)
    external fun setGenmoveProfile(profile: String)
    override external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    // override external fun genMoveInternal(isBlack: Boolean) : Int
    override fun genMoveInternal(isBlack: Boolean) = genMoveInternalUsingRawNN(isBlack)
    override fun debugInfo(): String? {
        return null
    }

    override external fun doPass(isBlack: Boolean)

    // based on KataGo's analysis command
    // https://github.com/lightvector/KataGo/blob/master/docs/Analysis_Engine.md#responses
    external fun analyze(msec: Int, isBlack: Boolean) : String

    private val json = Json { ignoreUnknownKeys = true }
    private fun parseAnalysisResponse(res: String) : AnalysisResponse {
        return json.decodeFromString<AnalysisResponse>(res)
        // return json.decodeFromString(serializer<AnalysisResponse>(), res)
    }

    override fun analyzeSituation(isBlack: Boolean, game: GoGame): List<AnalyzeInfo> {
        val res = analyze(2000, isBlack)
        println(res)
        if (res.isEmpty())
            return emptyList()
        val moveInfos = parseAnalysisResponse(res).moveInfos
        val maxVisits = moveInfos.maxOfOrNull { it.visits }?.coerceAtLeast(1) ?: 1
        return moveInfos.mapNotNull {
                val move = it.move
                if (move.uppercase() == "PASS")
                    return@mapNotNull null
                val pos = MovePos.fromString(move, game)
                val rate = it.winrate
                val pv = it.pv.mapNotNull {
                    if (it.uppercase() == "PASS") null else GTPHelper.strToCell(it, game)
                }
                val score = if (isBlack) it.scoreLead else - it.scoreLead
                AnalyzeInfo(game.visualBoard.getCell(pos.x, pos.y), rate, pv, score,
                            it.order, it.visits,
                            maxVisits)
            }
    }

    override fun hint(isBlack: Boolean, game: GoGame): MovePos {
        val res = analyze(2000, isBlack)
        println(res)
        if (res.isEmpty())
            return MovePos.PASS
        val moveInfos = parseAnalysisResponse(res).moveInfos
        val bestMove = moveInfos.minByOrNull { it.order }?.move
        if (bestMove == null || bestMove.uppercase() == "PASS")
            return MovePos.PASS
        return MovePos.fromString(bestMove, game)
    }

    private fun genMoveInternalUsingRawNN(isBlack: Boolean) : Int {
        val temperature = 0.8
        val whichSymmetry = Random.nextInt(8)
        val nnOutput = parseRawNN(rawNN(whichSymmetry, 0.0, true));
        val policyList = nnOutput["policy"].orEmpty()
        val policyPassList = nnOutput["policyPass"].orEmpty()  // size = 1
        val weightList = (policyPassList + policyList).map { it.pow(1.0 / temperature) }
        val policyIndex = (weightedRandomIndex(weightList) ?: 0) - 1
        if (policyIndex < 0) {
            doPass(isBlack)
            return -1
        }
        val boardSize = sqrt(policyList.size.toDouble()).roundToInt()
        val x = policyIndex % boardSize
        val y = policyIndex / boardSize
        doMove(x, y, isBlack)
        return x or (y shl 16)
    }

    external fun rawNN(whichSymmetry: Int, policyOptimism: Double, useHumanModel: Boolean) : String

    private fun parseRawNN(text: String): Map<String, List<Double>> {
        val out = LinkedHashMap<String, MutableList<Double>>()
        var key: String = ""
        Regex("\\S+").findAll(text).forEach { m ->
            val t = m.value
            val v = if (t.equals("NAN")) 0.0 else t.toDoubleOrNull()
            if (v != null) {
                out.getOrPut(key) { mutableListOf() }.add(v)
            } else {
                key = t
                out.putIfAbsent(key, mutableListOf())
            }
        }
        return out.mapValues { it.value.toList() }
    }

    private fun weightedRandomIndex(weightList: List<Double>): Int? {
        return weightList.indices.minByOrNull { i ->
            val w = weightList[i]
            if (w > 0.0) - ln(Random.nextDouble()) / w
            else Double.POSITIVE_INFINITY
        }
    }

}
