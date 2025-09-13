package io.github.karino2.paoogo.goengine.gnugo2

import io.github.karino2.paoogo.goengine.GoEngine

data class MovePos(val x: Int, val y: Int, val pass: Boolean = false)
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
    override external fun setBoardSize(komi: Int)
    override external fun debugInfo(): String?
    override external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    override external fun genMoveInternal(isBlack: Boolean) : Int
    override external fun doPass(isBlack: Boolean)


    // external fun runCommand(pInput: String?): String?
}