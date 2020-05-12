package com.example.handsight

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.view.children
import com.airbnb.paris.Paris
import kotlinx.android.synthetic.main.finish_popup.view.*
import logic.WordGame
import java.util.*


class WordGameActivity : AbstractCameraXActivity() {

    private lateinit var predictions: AnalysisResult
    private var mMovingAvgSum: Long = 0
    private var questionStartTime: Long? = null
    private val mMovingAvgQueue: Queue<Long> = LinkedList()
    lateinit var questionCountdownText: TextView
    lateinit var wordContainer: LinearLayout
    override val contentViewLayoutId: Int
        get() = R.layout.activity_word_game
    lateinit var inflater: LayoutInflater
    lateinit var letterCards: List<View>
    private val game = WordGame()

    private var questionCountDown = object : CountDownTimer(game.timerLength, 100) {
        override fun onTick(millisUntilFinished: Long) {
            questionCountdownText.text = (millisUntilFinished / (1000) + 1).toString()
            game.elapsedTime = game.timerLength - millisUntilFinished
        }

        override fun onFinish() {
            game.advanceWord()
            updateLetter(false)
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
        // perfText = findViewById(R.id.PerfText)
        inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        Log.d("TEST", game.getQuestion().correctAnswer.toString())

        updateUI(true)

        questionStartTime = System.currentTimeMillis()
        questionCountDown.start()
    }

    var gameFrozen = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun applyToUiAnalyzeImageResult(result: AnalysisResult?) {
        if (!gameFrozen) {
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
                game.advanceWord()
                updateLetter(true)
            }
        }
    }

    private fun updateLetter(succeded: Boolean) {
        if(succeded) {
            val doneSound = MediaPlayer.create(this, R.raw.success_perc)
            doneSound.start()
            doneSound.setOnCompletionListener { doneSound.stop()}        } else {
            val doneSound = MediaPlayer.create(this, R.raw.fail_perc)
            doneSound.start()
            doneSound.setOnCompletionListener { doneSound.stop()}
        }
        var delayTime: Long = 0
        questionCountDown.cancel()
        if (game.wordPosition == game.getQuestion().correctAnswer.length) {
            findViewById<TextView>(R.id.questionCountdown).text = "0"
            updateUI(succeded)
            game.advanceGame()
            delayTime = 500
        }
        if (game.finished) {
            gameFrozen = true
            val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.finish_popup,null)
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = false
            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupView.RestartButton.setOnClickListener {popupWindow.dismiss(); game.reset(); questionCountDown.start(); updateUI(succeded); gameFrozen = false}
            popupView.MenuButton.setOnClickListener {popupWindow.dismiss(); finish()}
            popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)
        } else {
            Handler().postDelayed({
                game.performanceScore = 0
                questionCountDown.start()
                updateUI(succeded)
            }, delayTime)
        }
    }

    private fun updateUI(succeded: Boolean) {
        val word = game.getQuestion().correctAnswer

        // Set the letters
        if (game.wordPosition == 0) {
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
        for (i in word.indices) {
            var constraintView = letterCards[i]
            var cardView = constraintView.findViewById<CardView>(R.id.letterCard)
            val letterText = cardView.findViewById<TextView>(R.id.letterCardText)
            var constParams = constraintView.layoutParams as LinearLayout.LayoutParams

            if (i == game.wordPosition-1) {
                constParams.weight = 1.0f
                if(succeded) {
                    Paris.style(cardView).apply(R.style.card_success)
                    Paris.style(letterText).apply(R.style.card_text_success)
                } else {
                    Paris.style(cardView).apply(R.style.card_fail)
                    Paris.style(letterText).apply(R.style.card_text_fail)
                }

            } else if (i == game.wordPosition) {
                constParams.weight = 1.5f
                Paris.style(cardView).apply(R.style.card_current)
                Paris.style(letterText).apply(R.style.card_text_current)
            } else if (i > game.wordPosition) {
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
        findViewById<TextView>(R.id.questionTextView).text =
            "Question ${game.currentQuestionIndex} of ${game.numberOfQuestions}"
    }

}
