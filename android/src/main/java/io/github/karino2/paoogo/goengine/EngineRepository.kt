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