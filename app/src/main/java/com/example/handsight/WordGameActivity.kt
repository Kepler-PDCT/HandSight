package com.example.handsight

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.camera.core.ImageProxy
import androidx.cardview.widget.CardView
import androidx.core.view.children
import com.airbnb.paris.Paris
import logic.WordGame
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.nio.FloatBuffer
import java.util.*


class WordGameActivity : AbstractCameraXActivity<WordGameActivity.AnalysisResult?>() {

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

    private var bestGuessSoFar = 99
    private val mMovingAvgQueue: Queue<Long> = LinkedList()
    private var answerCurrentlyCorrect : Boolean = false
    lateinit var correctAnswerCountdownText : TextView
    lateinit var perfText: TextView
    lateinit var questionCountdownText : TextView
    lateinit var wordContainer : LinearLayout
    override val contentViewLayoutId: Int
        get() = R.layout.activity_word_game
    lateinit var inflater: LayoutInflater
    lateinit var letterCards : List<View>

    private val game = WordGame()

    private var questionCountDown = object : CountDownTimer(game.timerLength.toLong(),100) {
        override fun onTick(millisUntilFinished: Long) {
            questionCountdownText.text = (millisUntilFinished/(1000)+1).toString()
            game.elapsedTime = game.timerLength - millisUntilFinished
        }
        override fun onFinish() {
            game.advanceWord()
            updateLetter()
        }
    }


    override val cameraPreviewTextureView: TextureView
        get() = (findViewById<View>(R.id.image_classification_texture_view_stub) as ViewStub)
            .inflate()
            .findViewById(R.id.image_classification_texture_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wordContainer = findViewById(R.id.wordContainer)
        questionCountdownText = findViewById(R.id.questionCountdown)
//        perfText = findViewById(R.id.PerfText)
        inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        Log.d("TEST", game.getQuestion().correctAnswer.toString())

        updateUI()

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

        game.updatePerformanceScore(predictions.topNClassNames, predictions.topNScores)

        for (prediction in predictions.topNClassNames.sliceArray(0..2)) {
            Log.d("TEST", prediction)
        }

        if (game.checkPredictions(predictions.topNClassNames.toList().map { it!!.single() })) {
            updateLetter()
        }
    }

    private fun updateLetter () {
        if(game.finished) {
            game.reset()
        }
        game.performanceScore = 0
        questionCountDown.cancel()
        questionCountDown.start()
        updateUI()
    }

    private fun updateUI() {
        val word = game.getQuestion().correctAnswer

        // Set the letters
        if(game.wordPosition == 0) {
            wordContainer.removeAllViews()
            for (i in word.indices) {
                inflater.inflate(
                    R.layout.module_letter_card,
                    wordContainer as ViewGroup
                )
                var cardView = wordContainer.children.last()
                var textView = cardView.findViewById<TextView>(R.id.letterCardText)
                textView.text = word[i].toString()
            }
            letterCards = wordContainer.children.toList()
        }

        // Update letter styles
        for(i in word.indices) {
            var constraintView = letterCards[i]
            var cardView = constraintView.findViewById<CardView>(R.id.letterCard)
            val letterText = cardView.findViewById<TextView>(R.id.letterCardText)
            var constParams = constraintView.layoutParams as LinearLayout.LayoutParams

            if (i < game.wordPosition) {
                constParams.weight = 1.0f
                Paris.style(cardView).apply(R.style.card_done)
                Paris.style(letterText).apply(R.style.card_text_done)
            }
            else if (i == game.wordPosition) {
                constParams.weight = 1.5f
                Paris.style(cardView).apply(R.style.card_current)
                Paris.style(letterText).apply(R.style.card_text_current)
            }
            else if (i > game.wordPosition) {
                constParams.weight = 1.0f
                Paris.style(cardView).apply(R.style.card_upcoming)
                Paris.style(letterText).apply(R.style.card_text_upcoming)
            }
            // Make sure auto text size is enabled
            // Paris seem to do mess with it otherwise
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                letterText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            }
        }

        findViewById<TextView>(R.id.scoreTextView).text = "Score: ${game.score}"
        findViewById<TextView>(R.id.questionTextView).text = "Question: ${game.count} of ${game.numberOfQuestions}"
    }

    protected val moduleAssetName: String
        protected get() = "android_model_2_softmax.pt"
        //protected get() = "2020-04-21model.pt"
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
        private const val TOP_K = 5
        private const val MOVING_AVG_PERIOD = 10
        private const val FORMAT_MS = "%dms"
        private const val FORMAT_AVG_MS = "avg:%.0fms"
        private const val FORMAT_FPS = "%.1fFPS"
        const val SCORES_FORMAT = "%.2f"
    }

}
