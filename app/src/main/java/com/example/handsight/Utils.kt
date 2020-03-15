package com.example.handsight

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object Utils {
    fun assetFilePath(context: Context, assetName: String): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        try {
            context.assets.open(assetName).use { `is` ->
                FileOutputStream(file).use { os ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (`is`.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
                return file.absolutePath
            }
        } catch (e: IOException) {
            Log.e(
                Constants.TAG,
                "Error process asset $assetName to file path"
            )
        }
        return null
    }

    fun topK(a: FloatArray, topk: Int): IntArray {
        val values = FloatArray(topk)
        Arrays.fill(values, -Float.MAX_VALUE)
        val ixs = IntArray(topk)
        Arrays.fill(ixs, -1)
        for (i in a.indices) {
            for (j in 0 until topk) {
                if (a[i] > values[j]) {
                    for (k in topk - 1 downTo j + 1) {
                        values[k] = values[k - 1]
                        ixs[k] = ixs[k - 1]
                    }
                    values[j] = a[i]
                    ixs[j] = i
                    break
                }
            }
        }
        return ixs
    }
}