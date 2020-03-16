package com.example.handsight

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.camera.core.ImageProxy
import com.example.handsight.ImitationGameActivity.AnalysisResult
import logic.GuessingGame
import logic.ImitationGame
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.nio.FloatBuffer
import java.time.Duration
import java.util.*

class ImitationGameActivity :  AbstractCameraXActivity<AnalysisResult?>() {
    class AnalysisResult(
        val topNClassNames: Array<String?>,
        val topNScores: FloatArray,
        val moduleForwardDuration: Long,
        val analysisDuration: Long
    )

    private var mAnalyzeImageErrorState = false
    //private ResultRowView[] mResultRowViews = new ResultRowView[TOP_K];
    private var mModule: Module? = null
    private val mModuleAssetName: String? = null
    private var mInputTensorBuffer: FloatBuffer? = null
    private var mInputTensor: Tensor? = null
    private var mMovingAvgSum: Long = 0
    private var questionStartTime : Long? = null
    private val guessDelay = 2000
    private var bestGuessSoFar = 99
    var correctAnswerStartTime: Long? = null
    private val mMovingAvgQueue: Queue<Long> = LinkedList()
    private var answerCurrentlyCorrect : Boolean = false
    override val contentViewLayoutId: Int
        get() = R.layout.activity_image_classification

    private val game = ImitationGame()

    override val cameraPreviewTextureView: TextureView
        get() = (findViewById<View>(R.id.image_classification_texture_view_stub) as ViewStub)
            .inflate()
            .findViewById(R.id.image_classification_texture_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TEST", game.getQuestion().correctAnswer.toString())
        val uri = "@drawable/" + game.getQuestion().correctAnswer.toString().toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)
        findViewById<ImageView>(R.id.CorrectAnswerImage).setImageDrawable(res)
        findViewById<TextView>(R.id.scoreTextView)!!.setText("${game.score} / ${game.count} out of ${game.numberOfQuestions}")
        questionStartTime = System.currentTimeMillis()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun applyToUiAnalyzeImageResult(result: AnalysisResult?) {
        mMovingAvgSum += result!!.moduleForwardDuration
        mMovingAvgQueue.add(result!!.moduleForwardDuration)
        if (mMovingAvgQueue.size > MOVING_AVG_PERIOD) {
            mMovingAvgSum -= mMovingAvgQueue.remove()
        }
        Log.d("TEST", result.topNClassNames[0])
        Log.d("TEST", java.lang.Float.toString(result.topNScores[0]))
        Log.d("TEST", game.getQuestion().correctAnswer.toString())
        for (i in 0 until result.topNClassNames.size) {
            if(game.isCorrect(result.topNClassNames[i]!!.single())) {
                if(bestGuessSoFar > i) {
                    bestGuessSoFar = i
                }
            }
        }
        if(game.isCorrect(result.topNClassNames[0]!!.single()) && !answerCurrentlyCorrect) {
            correctAnswerStartTime = System.currentTimeMillis()
            Log.d("TEST", correctAnswerStartTime.toString())
            answerCurrentlyCorrect = true
            Log.d("TEST", answerCurrentlyCorrect.toString())
        } else if (game.isCorrect(result.topNClassNames[0]!!.single())) {
            //Top answer correct for 2 seconds
            if(System.currentTimeMillis()-correctAnswerStartTime!! > guessDelay) {
                Log.d("TEST", "making guess")
                game.makeGuess(result.topNClassNames[0]!!.single())
                finishQuestion()
            }
        } else if(System.currentTimeMillis() - questionStartTime!! > 10000) {
            game.setScoreAccordingToPosition(bestGuessSoFar)
            finishQuestion()
        }

    }

    private fun finishQuestion () {
        game.advanceGame()
        if(game.finished) {
            game.reset()
        }
        bestGuessSoFar = 99
        questionStartTime = System.currentTimeMillis()
        findViewById<TextView>(R.id.scoreTextView)!!.setText("${game.score} / ${game.count} out of ${game.numberOfQuestions}")
        Log.d("TEST", game.score.toString())
        val uri = "@drawable/" + game.getQuestion().correctAnswer.toString().toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)
        findViewById<ImageView>(R.id.CorrectAnswerImage).setImageDrawable(res)

    }

    protected val moduleAssetName: String
        protected get() = "android_model_2_softmax.pt"

    /*override fun getInfoViewAdditionalText(): String {
        return moduleAssetName
    }*/

    @WorkerThread
    override fun analyzeImage(image: ImageProxy?, rotationDegrees: Int): AnalysisResult? {
        return if (mAnalyzeImageErrorState) {
            null
        } else try {
            if (mModule == null) {
                val moduleFileAbsoluteFilePath = File(
                    Utils.assetFilePath(this, moduleAssetName)
                ).absolutePath
                mModule = Module.load(moduleFileAbsoluteFilePath)
                mInputTensorBuffer =
                    Tensor.allocateFloatBuffer(3 * INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT)
                mInputTensor = Tensor.fromBlob(
                    mInputTensorBuffer,
                    longArrayOf(
                        1,
                        3,
                        INPUT_TENSOR_HEIGHT.toLong(),
                        INPUT_TENSOR_WIDTH.toLong()
                    )
                )
            }
            val startTime = SystemClock.elapsedRealtime()
            TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
                image!!.image,
                rotationDegrees,
                INPUT_TENSOR_WIDTH,
                INPUT_TENSOR_HEIGHT,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB,
                mInputTensorBuffer,
                0
            )
            val moduleForwardStartTime = SystemClock.elapsedRealtime()
            val outputTensor = mModule!!.forward(IValue.from(mInputTensor)).toTensor()
            val moduleForwardDuration =
                SystemClock.elapsedRealtime() - moduleForwardStartTime
            val scores = outputTensor.dataAsFloatArray
            val ixs =
                Utils.topK(scores, TOP_K)
            val topKClassNames =
                arrayOfNulls<String>(TOP_K)
            val topKScores =
                FloatArray(TOP_K)
            for (i in 0 until TOP_K) {
                val ix = ixs[i]
                topKClassNames[i] = Constants.IMAGENET_CLASSES[ix]
                topKScores[i] = scores[ix]
            }
            val analysisDuration = SystemClock.elapsedRealtime() - startTime
            AnalysisResult(
                topKClassNames,
                topKScores,
                moduleForwardDuration,
                analysisDuration
            )
        } catch (e: Exception) {
            Log.e(
                Constants.TAG,
                "Error during image analysis",
                e
            )
            mAnalyzeImageErrorState = true
            runOnUiThread {
                if (!isFinishing) {
                    Log.d("TEST", "is finishing")
                }
            }
            null
        }
    }


   /* override fun getInfoViewCode(): Int {
        return intent.getIntExtra(INTENT_INFO_VIEW_TYPE, -1)
    }*/

    override fun onDestroy() {
        super.onDestroy()
        if (mModule != null) {
            mModule!!.destroy()
        }
    }

    companion object {
        const val INTENT_MODULE_ASSET_NAME = "INTENT_MODULE_ASSET_NAME"
        const val INTENT_INFO_VIEW_TYPE = "INTENT_INFO_VIEW_TYPE"
        private const val INPUT_TENSOR_WIDTH = 224
        private const val INPUT_TENSOR_HEIGHT = 224
        private const val TOP_K = 3
        private const val MOVING_AVG_PERIOD = 10
        private const val FORMAT_MS = "%dms"
        private const val FORMAT_AVG_MS = "avg:%.0fms"
        private const val FORMAT_FPS = "%.1fFPS"
        const val SCORES_FORMAT = "%.2f"
    }


}