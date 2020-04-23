package com.example.handsight

import android.os.*
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import logic.ImitationChallengeGame
import java.util.*

class ImitationGameActivity : AbstractCameraXActivity() {

    private lateinit var predictions: AnalysisResult
    private var mMovingAvgSum: Long = 0
    private var questionStartTime: Long? = null
    private val game = ImitationChallengeGame()
    private var bestGuessSoFar = 99
    private val mMovingAvgQueue: Queue<Long> = LinkedList()
    private var answerCurrentlyCorrect: Boolean = false
    lateinit var correctAnswerCountdownText: TextView
    lateinit var perfText: TextView
    lateinit var questionCountdownText: TextView
    override val contentViewLayoutId: Int
        get() = R.layout.activity_imitation_mode

    private var correctAnswerCountdown = object : CountDownTimer(2000, 100) {
        override fun onTick(millisUntilFinished: Long) {
            correctAnswerCountdownText.text = (millisUntilFinished / 1000f + 1).toInt().toString()
        }

        override fun onFinish() {
            Log.d("aaa", predictions.topNClassNames[0]!!.single().toString())
            correctAnswerCountdownText.text = "0"
            Handler().postDelayed(
                {
                    correctAnswerCountdownText.text = ""
                    game.makeGuess(predictions.topNClassNames[0]!!.single())
                    answerCurrentlyCorrect = false
                    finishQuestion()
                },
                1000
            )
        }
    }

    private var questionCountDown = object : CountDownTimer(game.timerLength, 100) {
        override fun onTick(millisUntilFinished: Long) {
            questionCountdownText.text = (millisUntilFinished / (1000) + 1).toString()
        }

        override fun onFinish() {
            game.setScoreAccordingToPosition(bestGuessSoFar)
            game.advanceGame()
            finishQuestion()
        }
    }

    override val cameraPreviewTextureView: TextureView
        get() = (findViewById<View>(R.id.image_classification_texture_view_stub) as ViewStub)
            .inflate()
            .findViewById(R.id.image_classification_texture_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        correctAnswerCountdownText = findViewById(R.id.correctAswerCountdown)
        correctAnswerCountdownText.text = ""
        questionCountdownText = findViewById(R.id.questionCountdown)
        perfText = findViewById(R.id.PerfText)
        perfText.text = ""

        updateUI()
        questionStartTime = System.currentTimeMillis()
        questionCountDown.start()
    }

    private fun updateUI() {
        val correctLetter = game.getQuestion().correctAnswer.toString()
        val uri = "@drawable/" + correctLetter.toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)
        findViewById<ImageView>(R.id.CorrectAnswerImage).setImageDrawable(res)
        findViewById<TextView>(R.id.CorrectLetterTextView).setText(correctLetter)

        findViewById<TextView>(R.id.questionTextView)!!.setText("Question ${game.count} of ${game.numberOfQuestions}")
        findViewById<TextView>(R.id.scoreTextView)!!.setText("Score: ${game.score}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun applyToUiAnalyzeImageResult(result: AnalysisResult?) {
        mMovingAvgSum += result!!.moduleForwardDuration
        mMovingAvgQueue.add(result!!.moduleForwardDuration)
        if (mMovingAvgQueue.size > MOVING_AVG_PERIOD) {
            mMovingAvgSum -= mMovingAvgQueue.remove()
        }
        predictions = result
        Log.d("TEST", result.topNClassNames[0].toString())
        Log.d("TEST", result.topNScores[0].toString())
        for (i in 0 until predictions.topNClassNames.size) {
            if (game.isCorrect(predictions.topNClassNames[i]!!.single())) {
                perfText.text = predictions.topNScores[i].toString()
                if (bestGuessSoFar > i) {
                    bestGuessSoFar = i
                }
                Log.d("size", predictions.topNClassNames.size.toString())
            } else {
                perfText.text = ""
            }
        }
        if (game.isCorrect(predictions.topNClassNames[0]!!.single()) && !answerCurrentlyCorrect) {
            correctAnswerCountdown.start()
            answerCurrentlyCorrect = true
        } else if (!game.isCorrect((predictions.topNClassNames[0]!!.single()))) {
            correctAnswerCountdownText.text = ""
            correctAnswerCountdown.cancel()
            answerCurrentlyCorrect = false
        }
        game.updatePerformanceScore(predictions.topNClassNames, predictions.topNScores)
    }

    private fun finishQuestion() {
        game.performanceScore = 0
        if (game.finished) {
            game.reset()
        }

        bestGuessSoFar = 99
        questionCountDown.start()
        updateUI()
        Log.d("TEST", game.score.toString())
    }

}