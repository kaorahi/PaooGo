package io.github.karino2.paoogo.goengine

import android.content.res.AssetManager
import io.github.karino2.paoogo.goengine.gnugo2.GnuGo2Native
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
import io.github.karino2.paoogo.goengine.ray.RayNative

interface GoEngine {
    fun setKomi(komi: Float)
    fun clearBoard()
    fun setBoardSize(size: Int)
    fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    fun doPass(isBlack: Boolean)

    fun genMoveInternal(isBlack: Boolean) : Int
    fun genMove(isBlack: Boolean) : MovePos {
        val move = genMoveInternal(isBlack)
        if (move == -1) {
            return MovePos( 0, 0, true)
        }
        return MovePos(move and 0xff, move shr 16)
    }

    fun debugInfo(): String?
}

class EngineRepository(val assetManager: AssetManager) {
    val gnugo2Engine by lazy {
        GnuGo2Native().apply {
            initNative()
            setDepth(4)
        }
    }

    val rayEngine by lazy {
        RayNative().apply {
            initNative(Runtime.getRuntime().availableProcessors(), 1.0)
            setupAssetParams(assetManager)
            initGame()
        }
    }

    fun getEngine(level: Int) : GoEngine {
        return when(level) {
            1-> object: GoEngine {
                override fun setKomi(komi: Float) {
                    gnugo2Engine.setKomi(komi)
                }

                override fun clearBoard() {
                    gnugo2Engine.clearBoard()
                }

                override fun setBoardSize(size: Int) {
                    gnugo2Engine.setBoardSize(size)
                }

                override fun doMove(
                    x: Int,
                    y: Int,
                    isBlack: Boolean
                ): Boolean {
                    return gnugo2Engine.doMove(x, y, isBlack)
                }

                override fun doPass(isBlack: Boolean) {
                    gnugo2Engine.doPass(isBlack)
                }

                override fun genMoveInternal(isBlack: Boolean): Int {
                    return gnugo2Engine.genMoveInternal(isBlack)
                }

                override fun debugInfo(): String? {
                    return gnugo2Engine.debugInfo()
                }
            }
            else -> object: GoEngine {
                override fun setKomi(komi: Float) {
                    rayEngine.setKomi(komi)
                }

                override fun clearBoard() {
                    rayEngine.clearBoard()
                }

                override fun setBoardSize(size: Int) {
                    rayEngine.setBoardSize(size)
                }

                override fun doMove(
                    x: Int,
                    y: Int,
                    isBlack: Boolean
                ): Boolean {
                    return rayEngine.doMove(x, y, isBlack)
                }

                override fun doPass(isBlack: Boolean) {
                    rayEngine.doPass(isBlack)
                }

                override fun genMoveInternal(isBlack: Boolean): Int {
                    return rayEngine.genMoveInternal(isBlack)
                }

                override fun debugInfo(): String? {
                    return null
                }
            }
        }
    }
}