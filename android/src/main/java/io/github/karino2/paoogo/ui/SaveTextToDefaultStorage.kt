package io.github.karino2.paoogo.ui

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore.Downloads
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun saveTextToDefaultStorage(context: Context, text: String): String? {
    for (n in 1..9999) {
        val yymmdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
        val fileName = "paoo${yymmdd}_${n}.sgf"
        if (!fileExists(context, fileName)) {
            val path = writeToFile(context, fileName, text)
            return path
        }
    }
    return null
}

// Use MediaStore or AppStorage depending on Android version

private fun fileExists(context: Context, fileName: String): Boolean {
    return if (isMediaStoreAvailable())
        isInMediaStore(context, safeNameForMediaStore(fileName))
    else
        isInAppStorage(context, fileName)
}

private fun writeToFile(context: Context, fileName: String, text: String): String? {
    return if (isMediaStoreAvailable())
        writeToMediaStore(context, safeNameForMediaStore(fileName), text)
    else
        writeToAppStorage(context, fileName, text)
}

private fun isMediaStoreAvailable(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

// Note on MediaStore:
// Using MIME type "application/x-go-sgf" may cause accessibility
// issues; due to Android's scoped storage restrictions, other apps or
// a PC (via MTP) might not be able to read the file.
// Using "text/plain" keeps the file visible to others, but the system
// may silently append ".txt" to the filename. This can also cause
// issues.
// As a compromise, we explicitly add ".txt" ourselves and save with
// "text/plain", to keep "permissionless" policy. [2025-10-28]

private fun safeNameForMediaStore(fileName: String): String {
    val safeExt = ".txt"
    return if (fileName.endsWith(safeExt)) fileName else fileName + safeExt
}

// media store

private fun isInMediaStore(context: Context, fileName: String): Boolean {
    val uri = Downloads.EXTERNAL_CONTENT_URI
    val projection = arrayOf(Downloads._ID)
    val selection = "${Downloads.DISPLAY_NAME}=? AND ${Downloads.RELATIVE_PATH}=?"
    val selectionArgs = arrayOf(fileName, "${Environment.DIRECTORY_DOWNLOADS}/")
    context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use {
        return it.moveToFirst()
    }
    return false
}

private fun writeToMediaStore(context: Context, fileName: String, text: String): String? {
    val values = ContentValues().apply {
        put(Downloads.DISPLAY_NAME, fileName)
        put(Downloads.MIME_TYPE, "text/plain")
        put(Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/")
    }
    val uri = context.contentResolver.insert(Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
    try {
        context.contentResolver.openOutputStream(uri)?.use {
            it.write(text.toByteArray())
        }
        return "Download/${fileName}"
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

// app storage

private fun isInAppStorage(context: Context, fileName: String): Boolean {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    return File(dir, fileName).exists()
}

private fun writeToAppStorage(context: Context, fileName: String, text: String): String? {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return null
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, fileName)
    try {
        file.writeText(text)
        return file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}
