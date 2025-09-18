package io.github.karino2.paoogo.goengine.ray

import android.content.res.AssetManager
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.DoubleBuffer
import kotlin.collections.map

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

    fun openFMPReader(path: String) : FMParamReader = FMParamReader(openReader(path))

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

class MD2Util {
    fun revI(p:Int, i:Int) = ((p ushr i) or ((p and 0x3) shl i))
    fun rev16(p:Int) = revI(p, 32)
    fun rev14(p:Int) = revI(p, 28)
    fun rev12(p:Int) = revI(p, 24)
    fun rev10(p:Int) = revI(p, 20)
    fun rev8(p:Int) = revI(p, 16)
    fun rev6(p:Int) = revI(p, 12)
    fun rev4(p:Int) = revI(p, 8)
    fun rev3(p:Int) = ((p ushr 4) or (p and 0xc) or ((p and 0x3) shl 4))
    fun rev2(p:Int) = revI(p, 4)
    fun rev(p:Int) = revI(p, 2)

    fun md4VerticalMirror(md4: Int)
        = rev8(md4 and 0x00030003) or // 25<->33
        (rev6((md4 and 0x0000C00C) ushr 2) shl 2) or// 26<->32
        (rev4((md4 and 0x00003030) ushr 4) shl 4) or // 27<->31
        (rev2((md4 and 0x00000CC0) ushr 6) shl 6) or // 28<->30
        (rev6(((md4.toUInt() and 0xC00C0000u).toInt() ushr 18) shl 18)) or // 34<->40
        (rev4(((md4.toUInt() and 0x30300000u).toInt() ushr 20) shl 20)) or // 35<->39
        (rev2(((md4.toUInt() and 0x0CC00000u).toInt() ushr 22) shl 22)) or // 36<->38
        (md4 and 0x03000300)

    fun md4HorizontalMirror(md4: Int) =
        (md4 and 0x00030003) or
        (rev14((md4.toUInt() and 0xC000000Cu).toInt() ushr 2) shl 2) or // 26<->40
        (rev12((md4 and 0x30000030) ushr 4) shl 4) or // 27<->39
                (rev10((md4 and 0x0C0000C0) ushr 6) shl 6) or // 28<->38
                (rev8((md4.toUInt() and 0x03000300u).toInt() ushr 8) shl 8) or // 29<->37
                (rev6((md4.toUInt() and 0x00C00C00u).toInt() ushr 10) shl 10) or // 30<->36
                (rev4((md4.toUInt() and 0x00303000u).toInt() ushr 12) shl 12) or // 31<->35
                (rev2((md4.toUInt() and 0x000CC000u).toInt() ushr 14) shl 14) // 32<->34


    fun md2VerticalMirror(md2: Int) =
        ((md2 and 0x00FC00) ushr 10) or
                (md2 and 0x0003C0) or
                ((md2 and 0x00003F) shl 10) or
                (rev2((md2 and 0x330000) ushr 16) shl 16) or // 9<->11
                (md2 and 0xCC0000)

    fun md2HorizontalMirror(md2: Int) =
        (rev3((md2 and 0x00FC00) ushr 10) shl 10) or
        (rev((md2 and 0x0003C0) ushr 6) shl 6) or
        rev3(md2 and 0x00003F) or
        (rev2(((md2 and 0xCC0000) ushr 18) shl 18)) or
        (md2 and 0x330000)

    fun md2Rotate90(md2: Int) =
        ((md2 and 0x000003) shl 10) or
                ((md2 and 0x000C0C) shl 4) or
                ((md2 and 0x003030) ushr 4) or
                ((md2 and 0x0300C0) shl 6) or
                ((md2 and 0x000300) ushr 6) or
                ((md2 and 0x00C000) ushr 10) or
                ((md2 and 0xFC0000) ushr 2)

    fun md2Reverse(md2: Int) =
        ((md2 ushr 1) and 0x55555555) or ((md2 and 0x55555555) shl 1)
    fun md2Transpose8(md2: Int, transp: IntArray) {
        transp[0] = md2
        transp[1] = md2VerticalMirror(md2)
        transp[2] = md2HorizontalMirror(md2)
        transp[3] = md2VerticalMirror(transp[2])
        transp[4] = md2Rotate90(md2)
        transp[5] = md2Rotate90(transp[1])
        transp[6] = md2Rotate90(transp[2])
        transp[7] = md2Rotate90(transp[3])
    }

    fun md2Transpose16(md2: Int, transp: IntArray) {
        md2Transpose8(md2, transp)
        for(i in 0..<8) {
            transp[i + 8] = md2Reverse(transp[i])
        }
    }

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