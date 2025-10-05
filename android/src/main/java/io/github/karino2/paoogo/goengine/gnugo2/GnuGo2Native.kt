package io.github.karino2.paoogo.goengine.gnugo2

import io.github.karino2.paoogo.goengine.GoEngine
import org.ligi.gobandroid_hd.logic.GTPHelper
import org.ligi.gobandroid_hd.logic.GoGame

data class MovePos(val x: Int, val y: Int, val pass: Boolean = false) {
    companion object {
        val PASS = MovePos(0, 0, true)
        fun fromString(str: String, game: GoGame) : MovePos {
            if (str.length < 2)
                return PASS

            val cell = GTPHelper.strToCell(str, game)
            return MovePos(cell.x, cell.y, false)
        }
    }
}
public class GnuGo2Native : GoEngine {
    companion object {
        init{
            System.loadLibrary("gnugo2")
        }

    }
    external fun initNative()

    override external fun setKomi(komi: Float)
    external fun setDepth(depth: Int)
    override external fun clearBoard()
    override external fun setBoardSize(size: Int)
    override external fun debugInfo(): String?
    override external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    override external fun genMoveInternal(isBlack: Boolean) : Int
    override external fun doPass(isBlack: Boolean)
}