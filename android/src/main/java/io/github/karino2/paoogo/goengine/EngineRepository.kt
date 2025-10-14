package io.github.karino2.paoogo.goengine

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import io.github.karino2.paoogo.goengine.amigo.AmiGoNative
import io.github.karino2.paoogo.goengine.gnugo2.GnuGo2Native
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
import io.github.karino2.paoogo.goengine.gnugo3.GnuGo3Native
import io.github.karino2.paoogo.goengine.katago.KataGoNative
import io.github.karino2.paoogo.goengine.katago.KataGoSetup
import io.github.karino2.paoogo.goengine.liberty.LibertyNative
import io.github.karino2.paoogo.goengine.ray.RayNative
import org.ligi.gobandroid_hd.R
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

    val gnugo3Engine by lazy {
        GnuGo3Native().apply {
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

    val amigoEngine by lazy {
        AmiGoNative().apply {
            initNative()
        }
    }

    fun getAnalyzer() : GoAnalyzer { return katagoEngine }


    fun getEngine(level: Int) : Pair<GoEngine, Int> {
        return when(level) {
            1-> Pair(amigoEngine.apply { setLevel(0) }, R.string.paomigojr)
            2-> Pair(amigoEngine.apply { setLevel(7)}, R.string.paomigo)
            3-> Pair(libertyEngine, R.string.paolibe)
            4-> Pair(gnugo2Engine, R.string.paognujr)
            5-> Pair(gnugo3Engine, R.string.paognu)
            6-> Pair(katagoEngine.apply { setGenmoveProfile("rank_20k") }, R.string.paokata20k)
            7-> Pair(katagoEngine.apply { setGenmoveProfile("rank_19k") }, R.string.paokata19k)
            8-> Pair(katagoEngine.apply { setGenmoveProfile("rank_18k") }, R.string.paokata18k)
            9-> Pair(katagoEngine.apply { setGenmoveProfile("rank_17k") }, R.string.paokata17k)
            10-> Pair(katagoEngine.apply { setGenmoveProfile("rank_16k") }, R.string.paokata16k)
            11-> Pair(katagoEngine.apply { setGenmoveProfile("rank_15k") }, R.string.paokata15k)
            12-> Pair(katagoEngine.apply { setGenmoveProfile("rank_14k") }, R.string.paokata14k)
            13-> Pair(katagoEngine.apply { setGenmoveProfile("rank_13k") }, R.string.paokata13k)
            14-> Pair(katagoEngine.apply { setGenmoveProfile("rank_12k") }, R.string.paokata12k)
            15-> Pair(katagoEngine.apply { setGenmoveProfile("rank_11k") }, R.string.paokata11k)
            16-> Pair(katagoEngine.apply { setGenmoveProfile("rank_10k") }, R.string.paokata10k)
            17-> Pair(katagoEngine.apply { setGenmoveProfile("rank_9k") }, R.string.paokata9k)
            18-> Pair(katagoEngine.apply { setGenmoveProfile("rank_8k") }, R.string.paokata8k)
            19-> Pair(katagoEngine.apply { setGenmoveProfile("rank_7k") }, R.string.paokata7k)
            20-> Pair(katagoEngine.apply { setGenmoveProfile("rank_6k") }, R.string.paokata6k)
            21-> Pair(katagoEngine.apply { setGenmoveProfile("rank_5k") }, R.string.paokata5k)
            22-> Pair(katagoEngine.apply { setGenmoveProfile("rank_4k") }, R.string.paokata4k)
            23-> Pair(katagoEngine.apply { setGenmoveProfile("rank_3k") }, R.string.paokata3k)
            24-> Pair(katagoEngine.apply { setGenmoveProfile("rank_2k") }, R.string.paokata2k)
            25-> Pair(katagoEngine.apply { setGenmoveProfile("rank_1k") }, R.string.paokata1k)
            26-> Pair(katagoEngine.apply { setGenmoveProfile("rank_1d") }, R.string.paokata1d)
            27-> Pair(katagoEngine.apply { setGenmoveProfile("rank_2d") }, R.string.paokata2d)
            28-> Pair(katagoEngine.apply { setGenmoveProfile("rank_3d") }, R.string.paokata3d)
            29-> Pair(katagoEngine.apply { setGenmoveProfile("rank_4d") }, R.string.paokata4d)
            30-> Pair(katagoEngine.apply { setGenmoveProfile("rank_5d") }, R.string.paokata5d)
            31-> Pair(katagoEngine.apply { setGenmoveProfile("rank_6d") }, R.string.paokata6d)
            32-> Pair(katagoEngine.apply { setGenmoveProfile("rank_7d") }, R.string.paokata7d)
            33-> Pair(katagoEngine.apply { setGenmoveProfile("rank_8d") }, R.string.paokata8d)
            else-> Pair(katagoEngine.apply { setGenmoveProfile("rank_9d") }, R.string.paokata9d)
        }
    }
}
