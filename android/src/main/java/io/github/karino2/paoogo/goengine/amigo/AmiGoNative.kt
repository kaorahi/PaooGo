package io.github.karino2.paoogo.goengine.amigo

import io.github.karino2.paoogo.goengine.GoEngine

class AmiGoNative : GoEngine  {
    companion object {
        init {
            System.loadLibrary("amigo")
        }
    }

    external fun initNative()

    override fun setKomi(komi: Float){}
    override external fun clearBoard()
    override fun debugInfo() = ""
    external fun setLevel(level: Int)
    override external fun setBoardSize(size: Int)
    override external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    override external fun genMoveInternal(isBlack: Boolean) : Int
    override external fun doPass(isBlack: Boolean)
}