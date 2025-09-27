package io.github.karino2.paoogo.goengine

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import io.github.karino2.paoogo.goengine.gnugo2.GnuGo2Native
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
import io.github.karino2.paoogo.goengine.katago.KataGoNative
import io.github.karino2.paoogo.goengine.katago.KataGoSetup
import io.github.karino2.paoogo.goengine.liberty.LibertyNative
import io.github.karino2.paoogo.goengine.ray.RayNative
import org.ligi.gobandroid_hd.logic.Cell
import org.ligi.gobandroid_hd.logic.GoGame
import org.ligi.gobandroid_hd.logic.GoMove
import timber.log.Timber

interface GoConfig {
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
                Timber.w("sync: pass")
                doPass(tmp_move.isBlack)
            } else {
                Timber.w("sync: doMove (%d, %d, %d, %b)", tmp_move.cell!!.x, tmp_move.cell!!.y, tmp_move.player, tmp_move.isBlack)
                doMove(tmp_move.cell!!.x, tmp_move.cell!!.y, tmp_move.isBlack)
            }
        }
    }
}

interface GoEngine : GoConfig {

    fun genMoveInternal(isBlack: Boolean) : Int
    fun genMove(isBlack: Boolean) : MovePos {
        val move = genMoveInternal(isBlack)
        if (move == -1) {
            return MovePos.PASS
        }
        return MovePos(move and 0xff, move shr 16)
    }

    fun debugInfo(): String?
}

data class AnalyzeInfo(val cell: Cell, val rate: Double, val pv: List<Cell>) {
    val rateColor: Color
        get() {
            // interpolate from RED to Green by winrate.
            val rcomp = 1.0-rate
            val gcomp = rate
            return Color.valueOf(rcomp.toFloat(), gcomp.toFloat(), 0.0F)
        }

    val rateString : String
        get() = "%.1f".format(rate*100.0)
}

interface GoAnalyzer : GoConfig {
    fun hint(isBlack: Boolean, game: GoGame) : MovePos
    fun analyzeSituation(isBlack: Boolean, game: GoGame) : List<AnalyzeInfo>
}

class EngineRepository(val context: Context, val assetManager: AssetManager) {
    val gnugo2Engine by lazy {
        GnuGo2Native().apply {
            initNative()
            setDepth(4)
        }
    }

    val libertyEngine by lazy {
        LibertyNative().apply {
            initNative()
        }
    }

    val rayEngine by lazy {
        RayNative().apply {
            initNative(Runtime.getRuntime().availableProcessors(), 1.0)
            setupAssetParams(assetManager)
            initGame()
        }
    }

    val katagoEngine by lazy {
        val setup = KataGoSetup(context, assetManager)
        setup.extractFiles()
        KataGoNative().apply {
            initNative(
                Runtime.getRuntime().availableProcessors(),
                setup.configFile.absolutePath,
                setup.modelFile.absolutePath
            )
        }
    }

    fun getAnalyzer() : GoAnalyzer { return katagoEngine }

    fun getEngine(level: Int) : GoEngine {
        return when(level) {
            2-> libertyEngine
            3-> gnugo2Engine
            else -> rayEngine
        }
    }
}