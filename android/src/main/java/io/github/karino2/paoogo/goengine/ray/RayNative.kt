package io.github.karino2.paoogo.goengine.ray

import android.content.res.AssetManager
import io.github.karino2.paoogo.goengine.GoAnalyzer
import io.github.karino2.paoogo.goengine.GoEngine
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
import org.ligi.gobandroid_hd.logic.GTPHelper
import org.ligi.gobandroid_hd.logic.GoGame

class RayNative : GoEngine, GoAnalyzer {
    companion object {
        init{
            System.loadLibrary("ray")
        }
    }

    external fun initNative(threadNum: Int, thinkingTime: Double)
    external fun initUctParams(src: DoubleArray)
    external fun initUctMD2(firstLineNum: Int, indices: IntArray, src: DoubleArray)
    external fun initUctLargePattern(htype: Int, firstLineNum: Int, indices: IntArray, hashs: ByteArray, src: DoubleArray)
    external fun getPat3Ptr() : Any
    external fun initSimFeatureParameters(src: FloatArray)
    external fun initSimMD2(firstLineNum: Int, indices: IntArray, src: FloatArray)
    external fun initSimPat3(firstLineNum: Int, src: FloatArray)
    external fun finishInitSimMD2()

    // gtp like
    external fun initGame()
    override external fun setKomi(komi: Float)
    override external fun clearBoard()
    override external fun setBoardSize(size: Int)
    override external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    override external fun genMoveInternal(isBlack: Boolean) : Int
    override fun debugInfo(): String? {
        return null
    }

    override external fun doPass(isBlack: Boolean)

    fun setupAssetParams(assetManager: AssetManager)
    {
        val setup = RayParamSetup(assetManager, this)
        setup.setupUctSmallParams()
        setup.setupUctPat3()
        setup.setupUctMD2Bin()
        setup.setupUctLargePatterns()
        setup.setupSimSmallParams()
        setup.setupSimPat3Bin()
        setup.setupSimMD2Bin()
        // debPrint()
    }

    external fun analyze(msec: Int, isBlack: Boolean) : String

    override fun hint(isBlack: Boolean, game: GoGame): MovePos {
        val res = analyze(3000, isBlack)
        // D6,5800,C6,D5,C5,C4,B4,C3,B3 E6,5569,D6,D5,C5,E5 D3,5482,C4,C3,B3 E5,5466,D5 G7,5446,G8,D6,C6 B4,5425,C5,C4 D5,5200,C4,C5 E4,5000 B3,4931,C4 C2,4705 F7,4382 E7,4264 D2,3548
        println(res)
        if (res.isEmpty())
            return MovePos.PASS
        val arr = res.split("\n")
            .filterNot { it.isEmpty() }
            .last()
            .split(",")
        if (arr.size <= 2)
            return MovePos.PASS
        if (arr[0].uppercase() == "PASS")
            return MovePos.PASS

        return MovePos.fromString(arr[0], game)
    }

}