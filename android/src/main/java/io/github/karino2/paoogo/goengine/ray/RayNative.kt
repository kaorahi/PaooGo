package io.github.karino2.paoogo.goengine.ray

import android.content.res.AssetManager
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RayNative {
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
    external fun setKomi(komi: Float)
    external fun clearBoard()
    external fun setBoardSize(komi: Int)
    external fun doMove(x: Int, y: Int, isBlack: Boolean) : Boolean
    external fun genMoveInternal(isBlack: Boolean) : Int
    external fun doPass(isBlack: Boolean)

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