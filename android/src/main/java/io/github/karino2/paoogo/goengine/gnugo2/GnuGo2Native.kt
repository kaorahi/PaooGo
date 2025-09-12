package io.github.karino2.paoogo.goengine.gnugo2

data class MovePos(val x: Int, val y: Int, val pass: Boolean = false)
public class GnuGo2Native {
    companion object {
        init{
            System.loadLibrary("gnugo2")
        }

    }
    external fun initNative()

    external fun setKomi(komi: Float)
    external fun setDepth(depth: Int)
    external fun clearBoard()
    external fun setBoardSize(komi: Int)
    external fun debugInfo(): String?
    external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    external fun genMoveInternal(isBlack: Boolean) : Int
    external fun doPass(isBlack: Boolean)


    // external fun runCommand(pInput: String?): String?
}