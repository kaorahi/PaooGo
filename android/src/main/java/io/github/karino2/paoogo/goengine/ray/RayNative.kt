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

    external fun initNative()
    external fun initUctParams(src: DoubleArray)
    external fun initUctMD2(firstLineNum: Int, indices: IntArray, src: DoubleArray)
    external fun initUctLargePattern(htype: Int, firstLineNum: Int, indices: IntArray, hashs: ByteArray, src: DoubleArray)
    external fun getPat3Ptr() : Any

    fun setupAssetParams(assetManager: AssetManager)
    {
        val setup = RayParamSetup(assetManager, this)
        setup.setupUctSmallParams()
        setup.setupUctPat3()
        setup.setupUctMD2Bin()
        setup.setupUctLargePatterns()
        // debPrint()
    }

}