package io.github.karino2.paoogo.goengine.ray

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader

data class FMParam(val fparams : DoubleArray) {
    init {
        // w = 1, BTFM_DIMENSION = 5
        assert(fparams.size == 6)
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
        return reader.readLines()
            .map{
                it.split(" ")
                    .map { it.toDouble() }
                    .toDoubleArray()
            }.map { FMParam(it) }
    }
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
        val reader = BufferedReader(InputStreamReader(assetManager.open(path)))
        reader.let {
            val params = FMParamReader(it).readParams()
            it.close()
            return params
        }
    }

    fun readParamInto(path: String) {
        readParams(path).forEach {
            builder.add(it)
        }
    }

    val result : DoubleArray
        get() = builder.toArray()
}