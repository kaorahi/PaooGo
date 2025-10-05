package io.github.karino2.paoogo.goengine.gnugo3

import io.github.karino2.paoogo.goengine.GoEngine

class GnuGo3Native : GoEngine {
    companion object {
        init{
            System.loadLibrary("gnugo3")
        }

    }
    external fun initNative()

    override external fun setKomi(komi: Float)
    override external fun clearBoard()
    override external fun setBoardSize(size: Int)
    override external fun debugInfo(): String?
    override external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    override external fun genMoveInternal(isBlack: Boolean) : Int
    override external fun doPass(isBlack: Boolean)
}