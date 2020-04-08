package com.example.handsight

import android.graphics.drawable.ClipDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
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
import org.w3c.dom.Text
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
    private var mModule: Module? = null
    private lateinit var predictions: AnalysisResult
    private var mInputTensorBuffer: FloatBuffer? = null
    private var mInputTensor: Tensor? = null
    private var mMovingAvgSum: Long = 0
    private var questionStartTime : Long? = null
    private var correctAnswerCountdown = object : CountDownTimer(2000,100) {
        override fun onTick(millisUntilFinished: Long) {
            correctAnswerCountdownText.text = (millisUntilFinished/(1000f)+1).toString()
        }
        override fun onFinish() {
            game.makeGuess(predictions.topNClassNames[0]!!.single())
            finishQuestion()
        }
    }
    private var questionCountDown = object : CountDownTimer(20000,100) {
        override fun onTick(millisUntilFinished: Long) {
            questionCountdownText.text = (millisUntilFinished/(1000)+1).toString()
        }
        override fun onFinish() {
            game.setScoreAccordingToPosition(bestGuessSoFar)
            finishQuestion()
        }
    }
    private var bestGuessSoFar = 99
    private val mMovingAvgQueue: Queue<Long> = LinkedList()
    private var answerCurrentlyCorrect : Boolean = false
    lateinit var correctAnswerCountdownText : TextView
    lateinit var perfText: TextView
    lateinit var questionCountdownText : TextView
    override val contentViewLayoutId: Int
        get() = R.layout.imitation_mode

    private val game = ImitationGame()

    override val cameraPreviewTextureView: TextureView
        get() = (findViewById<View>(R.id.image_classification_texture_view_stub) as ViewStub)
            .inflate()
            .findViewById(R.id.image_classification_texture_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        correctAnswerCountdownText = findViewById(R.id.correctAswerCountdown)
        questionCountdownText = findViewById(R.id.questionCountdown)
        perfText = findViewById(R.id.PerfText)
        Log.d("TEST", game.getQuestion().correctAnswer.toString())
        val uri = "@drawable/" + game.getQuestion().correctAnswer.toString().toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)
        findViewById<ImageView>(R.id.CorrectAnswerImage).setImageDrawable(res)
        findViewById<TextView>(R.id.scoreTextView)!!.setText("Score: ${game.score} \nQuestion: ${game.count} out of ${game.numberOfQuestions}")
        questionStartTime = System.currentTimeMillis()
        questionCountDown.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun applyToUiAnalyzeImageResult(result: AnalysisResult?) {
        mMovingAvgSum += result!!.moduleForwardDuration
        mMovingAvgQueue.add(result!!.moduleForwardDuration)
        if (mMovingAvgQueue.size > MOVING_AVG_PERIOD) {
            mMovingAvgSum -= mMovingAvgQueue.remove()
        }
        predictions = result
        Log.d("TEST", result.topNClassNames.toString())
        Log.d("TEST", result.topNScores.toString())
        for (i in 0 until predictions.topNClassNames.size) {
            if(game.isCorrect(predictions.topNClassNames[i]!!.single())) {
                perfText.text = predictions.topNScores[i].toString()
                if(bestGuessSoFar > i) {
                    bestGuessSoFar = i
                }
            }
        }
        if(game.isCorrect(predictions.topNClassNames[0]!!.single()) && !answerCurrentlyCorrect) {
            correctAnswerCountdown.start()
            answerCurrentlyCorrect = true
        }else if (!game.isCorrect((predictions.topNClassNames[0]!!.single()))) {
            correctAnswerCountdownText.text = ""
            correctAnswerCountdown.cancel()
        }

    }

    private fun finishQuestion () {
        game.advanceGame()
        if(game.finished) {
            game.reset()
        }

        bestGuessSoFar = 99
        questionCountDown.start()
        findViewById<TextView>(R.id.scoreTextView)!!.setText("Score: ${game.score} \nQuestion: ${game.count} out of ${game.numberOfQuestions}")
        Log.d("TEST", game.score.toString())
        val uri = "@drawable/" + game.getQuestion().correctAnswer.toString().toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)
        findViewById<ImageView>(R.id.CorrectAnswerImage).setImageDrawable(res)

    }

    protected val moduleAssetName: String
        protected get() = "android_model_2_softmax.pt"

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