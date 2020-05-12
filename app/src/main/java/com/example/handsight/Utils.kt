package com.example.handsight

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.progress_bar.view.*
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

    var lastColor = Color.HSVToColor(floatArrayOf(27F, 0.82F, 0.70F))
    fun updatePerformanceMeter(activity: Activity, performanceScore: Int) {
        // Color animation
        var perf = performanceScore/100
        var color = Color.HSVToColor(floatArrayOf(27F+(100*perf).toFloat(), 0.82F, 0.70F+0.15F*perf))

        val valueAnimator = ValueAnimator.ofArgb(lastColor, color)
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Int
            activity.ProgressFill.setBackgroundColor(value)
        }
        valueAnimator.duration = 1000
        valueAnimator.start()
        lastColor = color

        // Height animation
        var progressBar = activity.findViewById<ConstraintLayout>(R.id.ProgressBar).findViewById<ConstraintLayout>(R.id.ProgressBarInnerConstraintView)
        val set = ConstraintSet()
        set.clone(progressBar.ProgressBarInnerConstraintView)
        set.setGuidelinePercent(R.id.InverseGuideline, 1-(performanceScore.toFloat()/100))
        var transition = ChangeBounds()
        transition.setDuration(1000)
        TransitionManager.beginDelayedTransition(progressBar, transition)
        set.applyTo(progressBar)
    }
}