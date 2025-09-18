package io.github.karino2.paoogo.goengine.ray

import android.content.res.AssetManager
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class FMParam(val fparams : DoubleArray) {
    companion object {
        // w = 1, BTFM_DIMENSION = 5
        const val PARAM_DOUBLE_NUM = 6
    }
    init {
        assert(fparams.size == PARAM_DOUBLE_NUM)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FMParam

        if (!fparams.contentEquals(other.fparams)) return false

        return true
    }

    override fun hashCode(): Int {
        return fparams.contentHashCode()
    }
}

/*
  あまり大きくないfm_t[]のテキストファイルを読み込んでこれを返す
 */
class FMParamReader(val reader: BufferedReader) {
    fun readParams() : List<FMParam> {
        return readParamsSeq().toList()
    }

    fun readParamsSeq() : Sequence<FMParam> {
        return reader.lineSequence()
            .map{
                it.split(" ")
                    .map { it.toDouble() }
                    .toDoubleArray()
            }.map { FMParam(it) }
    }

    fun close() { reader.close() }
}

class DoubleArrayBuilder {
    val list = mutableListOf<Double>()
    fun add(fmParam: FMParam) {
        list.addAll(fmParam.fparams.toList())
    }
    fun toArray() : DoubleArray {
        return list.toDoubleArray()
    }
}


class RayParamReader(val assetManager: AssetManager) {
    val builder = DoubleArrayBuilder()

    fun readParams(path: String) : List<FMParam> {
        val reader = openReader(path)
        reader.let {
            val params = FMParamReader(it).readParams()
            it.close()
            return params
        }
    }

    fun openReader(path: String): BufferedReader =
        BufferedReader(InputStreamReader(assetManager.open(path)))

    fun readParamInto(path: String) {
        readParams(path).forEach {
            builder.add(it)
        }
    }

    val result : DoubleArray
        get() = builder.toArray()
}

class RayParamSetup(val assetManager: AssetManager, val rayNative: RayNative) {
    val paramReader = RayParamReader(assetManager)

    fun setupUctSmallParams() {
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
        rayNative.initUctParams(paramReader.result)
    }


    fun setupUctPat3() {
        val PAT3_MAX = 65536

        val path = "ray_params/uct_params/Pat3.bin"

        val istream = BufferedInputStream(assetManager.open(path))
        val buf = ByteArray(PAT3_MAX*FMParam.PARAM_DOUBLE_NUM*8)
        val readLen = istream.read(buf, 0, buf.size)
        istream.close()
        assert(readLen == buf.size )
        rayNative.initUctPat3(readLen, buf)
    }

    fun setupUctMD2Bin() {
        val path = "ray_params/uct_params/MD2.bin"

        val BUCKET_SIZE = 64*1024
        val iarr = IntArray(BUCKET_SIZE)
        val darr = DoubleArray(FMParam.PARAM_DOUBLE_NUM*BUCKET_SIZE)

        val istream = BufferedInputStream(assetManager.open(path))
        val oneElemSize = 4+8*FMParam.PARAM_DOUBLE_NUM
        istream.readChunks(oneElemSize)
            .map { (len, buf)->
                assert(len == oneElemSize)
                val bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN)

                val index = bb.int
                val dparams = (0..<FMParam.PARAM_DOUBLE_NUM).map {
                    bb.double
                }.toDoubleArray()


                Pair(index, dparams)
            }
            .chunked(BUCKET_SIZE)
            .forEachIndexed{ cindex, chunk->
                chunk.forEachIndexed {
                        index, pair ->
                    iarr[index] = pair.first
                    pair.second.copyInto(darr, index*FMParam.PARAM_DOUBLE_NUM)
                }
                rayNative.initUctMD2(cindex*BUCKET_SIZE, iarr, darr)
            }
        istream.close()
    }

    fun setupUctLargePatterns() {
        for((i, fname) in listOf("MD3.bin", "MD4.bin", "MD5.bin").withIndex()) {
            setupLargePatternBin("ray_params/uct_params/${fname}", i)
        }
    }

    fun BufferedInputStream.readChunks(size: Int) : Sequence<Pair<Int, ByteArray>> {
        val buf = ByteArray(size)
        return sequence {
            var len = read(buf, 0, size)
            while(len != -1) {
                yield(Pair(len, buf))
                len = read(buf, 0, size)
            }
        }
    }

    private fun setupLargePatternBin(path: String, htype: Int) {
        val BUCKET_SIZE = 64 * 1024

        val istream = BufferedInputStream(assetManager.open(path))
        val oneElemSize = 4+8+8*FMParam.PARAM_DOUBLE_NUM
        istream.readChunks(oneElemSize*BUCKET_SIZE)
            .forEachIndexed { cindex, (len, buf) ->
                rayNative.initUctLargePatternBlock(htype, cindex * BUCKET_SIZE, len,buf)
            }
        istream.close()
    }

    fun setupSimSmallParams() {
        val sim_prefix = "ray_params/sim_params/"

        val builder = mutableListOf<Float>()
        for(item in listOf( "PreviousDistance.txt",
            "CaptureFeature.txt",
            "SaveExtensionFeature.txt",
            "AtariFeature.txt",
            "ExtensionFeature.txt",
            "DameFeature.txt",
            "ThrowInFeature.txt",
        )) {
            val reader = paramReader.openReader("${sim_prefix}${item}")
            reader.readLines()
                .map { it.toFloat() }
                .let { builder.addAll(it) }
            reader.close()
        }
        // println(paramReader.result.size)
        rayNative.initSimFeatureParameters(builder.toFloatArray())
    }

    fun setupSimPat3Bin() {
        val path = "ray_params/sim_params/Pat3.bin"

        val BUCKET_SIZE = 64*1024

        val istream = BufferedInputStream(assetManager.open(path))
        val oneElemSize = 4
        istream.readChunks(oneElemSize)
            .map { (len, buf)->
                assert(len == oneElemSize)
                val bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN)
                bb.float
            }
            .chunked(BUCKET_SIZE)
            .forEachIndexed{ cindex, chunk->
                rayNative.initSimPat3(cindex*BUCKET_SIZE, chunk.toFloatArray())
            }
        istream.close()
    }

    fun setupSimMD2Bin() {
        val path = "ray_params/sim_params/MD2.bin"

        val BUCKET_SIZE = 64*1024
        val iarr = IntArray(BUCKET_SIZE)
        val farr = FloatArray(BUCKET_SIZE)

        val istream = BufferedInputStream(assetManager.open(path))
        val oneElemSize = 4+4
        istream.readChunks(oneElemSize)
            .map { (len, buf)->
                assert(len == oneElemSize)
                val bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN)

                val index = bb.int
                val rate = bb.float


                Pair(index, rate)
            }
            .chunked(BUCKET_SIZE)
            .forEachIndexed{ cindex, chunk->
                chunk.forEachIndexed {
                        index, pair ->
                    iarr[index] = pair.first
                    farr[index] = pair.second
                }
                rayNative.initSimMD2(cindex*BUCKET_SIZE, iarr, farr)
            }
        istream.close()
        rayNative.finishInitSimMD2()
    }
}