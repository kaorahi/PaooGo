package io.github.karino2.paoogo.goengine.ray

import android.content.res.AssetManager

class RayNative {
    companion object {
        init{
            System.loadLibrary("ray")
        }
    }

    external fun initNative()
    external fun initUctParams(src: DoubleArray)

    // return ByteBuffer
    external fun getLargeParamPtr(ltype: Int) : Any
    external fun getHashPtr(htype: Int) : Any
    external fun getHashIndexPtr(htype: Int) : Any
    external fun getPoMD2Ptr() : Any

    fun setupAssetParams(assetManager: AssetManager)
    {
        val paramReader = RayParamReader(assetManager)
        val uct_prefix = "ray_params/uct_params/"

        for(item in listOf("KoExist.txt", "Pass.txt", "CaptureFeature.txt", "SaveExtensionFeature.txt",
            "AtariFeature.txt","ExtensionFeature.txt", "DameFeature.txt", "ConnectionFeature.txt",
            "ThrowInFeature.txt",
            "PosID.txt",
            "MoveDistance1.txt",
            "MoveDistance2.txt",
            "MoveDistance3.txt",
            "MoveDistance4.txt",
            )) {
            paramReader.readParamInto("${uct_prefix}${item}")
        }
        // println(paramReader.result.size)
        initUctParams(paramReader.result)

    }

}