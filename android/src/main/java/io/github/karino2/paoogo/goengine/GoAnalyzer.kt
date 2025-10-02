package io.github.karino2.paoogo.goengine

import android.graphics.Color
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
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

interface GoAnalyzer : EngineConfig {
    fun hint(isBlack: Boolean, game: GoGame) : MovePos
    fun analyzeSituation(isBlack: Boolean, game: GoGame) : List<AnalyzeInfo>
}
