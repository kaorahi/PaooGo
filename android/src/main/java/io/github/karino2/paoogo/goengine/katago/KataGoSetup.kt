package io.github.karino2.paoogo.goengine.katago

import android.content.Context
import android.content.res.AssetManager
import java.io.File

class KataGoSetup(val context: Context, val assetManager: AssetManager) {
    companion object {
        val MODEL_NAME="kata1-b6c96-s175395328-d26788732.txt.gz"
        val CFG_NAME="gtp_jp.cfg"
    }

    fun ensureDir(relative: String) {
        val dir = getFile(relative)
        if (!dir.exists())
            if(!dir.mkdirs())
                throw Exception("failed to create dir ${dir.absolutePath}")
    }

    private fun getFile(relative: String): File = File(context.filesDir, relative)

    private fun ensureFileCopy(fname: String) {
        // txt.gz is automatically modified to txt.
        // noCompress is not working for txt.gz.
        // https://stackoverflow.com/questions/4666098/why-does-android-aapt-remove-gz-file-extension-of-assets
        val assetName = fname.replace("txt.gz", "txt_gz")
        val dest = getFile("katago/${fname}")
        if (dest.exists())
            return

        ensureDir("katago")
        assetManager.open("katago/${assetName}").use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun ensureDelete(fname: String) {
        val dest = getFile("katago/${fname}")
        if (dest.exists())
            dest.delete()
    }

    fun extractFiles() {
        ensureFileCopy(MODEL_NAME)
        // update config often for a while.
        ensureDelete(CFG_NAME)
        ensureFileCopy(CFG_NAME)
    }

    val configFile: File
        get() = getFile("katago/${CFG_NAME}")

    val modelFile: File
        get() = getFile("katago/${MODEL_NAME}")

}