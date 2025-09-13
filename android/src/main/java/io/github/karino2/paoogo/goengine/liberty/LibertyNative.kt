package io.github.karino2.paoogo.goengine.liberty

import io.github.karino2.paoogo.goengine.GoEngine

class LibertyNative : GoEngine {
    companion object {
        init{
            System.loadLibrary("liberty")
        }

    }
    external fun initNative()

    override external fun setKomi(komi: Float)
    override external fun clearBoard()
    override external fun setBoardSize(komi: Int)
    override external fun debugInfo(): String?
    override external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    override external fun genMoveInternal(isBlack: Boolean) : Int
    override external fun doPass(isBlack: Boolean)
}