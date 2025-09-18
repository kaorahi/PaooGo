package io.github.karino2.paoogo.goengine

import android.content.res.AssetManager
import io.github.karino2.paoogo.goengine.gnugo2.GnuGo2Native
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
import io.github.karino2.paoogo.goengine.liberty.LibertyNative
import io.github.karino2.paoogo.goengine.ray.RayNative
import org.ligi.gobandroid_hd.logic.GoGame

interface GoConfig {
    fun setKomi(komi: Float)
    fun clearBoard()
    fun setBoardSize(size: Int)
    fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    fun doPass(isBlack: Boolean)
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

interface GoAnalyzer : GoConfig {
    fun hint(isBlack: Boolean, game: GoGame) : MovePos
}

class EngineRepository(val assetManager: AssetManager) {
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
            initNative(Runtime.getRuntime().availableProcessors(), 3.0)
            setupAssetParams(assetManager)
            initGame()
        }
    }

    fun getAnalyzer() : GoAnalyzer { return rayEngine }

    fun getEngine(level: Int) : GoEngine {
        return when(level) {
            2-> libertyEngine
            3-> gnugo2Engine
            else -> rayEngine
        }
    }
}