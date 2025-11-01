package io.github.karino2.paoogo.goengine

import android.graphics.Color
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
import io.github.karino2.paoogo.goengine.katago.RootInfo
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import org.ligi.gobandroid_hd.logic.Cell
import org.ligi.gobandroid_hd.logic.GoGame
import org.ligi.gobandroid_hd.logic.GoMove
import timber.log.Timber

interface EngineConfig {
    fun setKomi(komi: Float)
    fun clearBoard()
    fun setBoardSize(size: Int)
    fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    fun doPass(isBlack: Boolean)

    fun sync(game: GoGame) {
        clearBoard()
        val replay_moves = ArrayList<GoMove>()
        replay_moves.add(game.actMove)
        var tmp_move: GoMove
        while (true) {
            tmp_move = replay_moves.last()
            if (tmp_move.isFirstMove || tmp_move.parent == null) break
            replay_moves.add(tmp_move.parent)
        }
        for (step in replay_moves.indices.reversed()) {
            tmp_move = replay_moves[step]

            // どうもisFirstMoveがtrueの時は何も無いらしい。
            if (tmp_move.isFirstMove)
                continue

            if (tmp_move.isPassMove) {
                Timber.Forest.w("sync: pass")
                doPass(tmp_move.isBlack)
            } else {
                Timber.Forest.w("sync: doMove (%d, %d, %d, %b)", tmp_move.cell!!.x, tmp_move.cell!!.y, tmp_move.player, tmp_move.isBlack)
                doMove(tmp_move.cell!!.x, tmp_move.cell!!.y, tmp_move.isBlack)
            }
        }
    }
}

data class AnalyzeInfo(
    val cell: Cell,
    val rate: Double,
    val pv: List<Cell>,
    val score: Double,
    val order: Int,
    val visits: Int,
    val maxVisits: Int,
    val rootInfo: RootInfo,
    val komi: Float,
) {
    val visitsColor: Color
        get() {
            if (order == 0)
                return Color.valueOf(0.0F, 1.0F, 1.0F)
            // Borrowed from Lizzie (drawLeelazSuggestionsBackgroundShadow)
            val saturation = 1.0F
            val brightness = 0.85F
            val maxAlpha = 240
            val minAlpha = 20
            val alphaFactor = 5.0
            val redHue = 0
            val greenHue = 120
            val p = visits / maxVisits.toDouble()
            val q = 2 * p
            val f = if (q < 1) q.pow(2/3.0) / 2 else 1 - (2 - q).pow(0.5) / 2
            val hue = redHue + (greenHue - redHue) * f
            val alpha = minAlpha + (maxAlpha - minAlpha) * max(0.0, ln(p) / alphaFactor + 1)
            val alphaInt = alpha.roundToInt().coerceIn(0, 255)
            val hsv = floatArrayOf(hue.toFloat(), saturation, brightness)
            return Color.valueOf(Color.HSVToColor(alphaInt, hsv))
        }

    val scoreString : String
        get() {
            val isHalfKomi = abs(komi - komi.roundToInt()) > 0.25
            val sign = if (score > 0) "+" else "-"
            val a = abs(score)
            val smallZero = " 0 "
            if (isHalfKomi) {
                // 0 ==> "0", 0.5 ==> "+0", 1.5 ==> "+1", 2.5 ==> "+2"
                return if (a < 0.25) smallZero else sign + (a - 0.5).roundToInt()
            } else {
                return if (a < 0.5) smallZero else "%+d".format(score.roundToInt())
            }
        }
}

interface GoAnalyzer : EngineConfig {
    fun hint(isBlack: Boolean, game: GoGame) : MovePos
    fun analyzeSituation(msec: Int, isBlack: Boolean, game: GoGame) : List<AnalyzeInfo>
}
