package io.github.karino2.paoogo.goengine.ray

import android.content.res.AssetManager
import io.github.karino2.paoogo.goengine.GoAnalyzer
import io.github.karino2.paoogo.goengine.GoEngine
import io.github.karino2.paoogo.goengine.gnugo2.MovePos
import org.ligi.gobandroid_hd.logic.GTPHelper
import org.ligi.gobandroid_hd.logic.GoGame

class RayNative : GoEngine {
    companion object {
        init{
            System.loadLibrary("ray")
        }
    }

    external fun initNative(threadNum: Int, thinkingTime: Double)
    external fun initUctParams(src: DoubleArray)
    external fun initUctMD2(firstLineNum: Int, indices: IntArray, src: DoubleArray)
    external fun initUctLargePatternBlock(htype: Int, firstLineNum: Int, blockSize: Int, block: ByteArray)
    external fun initUctPat3(blockSize: Int, block: ByteArray)
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
}