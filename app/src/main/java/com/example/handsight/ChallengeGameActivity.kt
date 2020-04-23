package com.example.handsight

import android.os.*
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import androidx.annotation.RequiresApi
import logic.ImitationChallengeGame
import java.util.*

class ChallengeGameActivity  :  AbstractCameraXActivity() {

    private lateinit var predictions: AnalysisResult

    private var mMovingAvgSum: Long = 0
    private var questionStartTime : Long? = null
    private val game = ImitationChallengeGame()
    private var correctAnswerCountdown = object : CountDownTimer(2000,100) {
        override fun onTick(millisUntilFinished: Long) {
            correctAnswerCountdownText.text = (millisUntilFinished/1000f + 1).toInt().toString()
        }
        override fun onFinish() {
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
    private var questionCountDown = object : CountDownTimer(game.timerLength,100) {
        override fun onTick(millisUntilFinished: Long) {
            questionCountdownText.text = (millisUntilFinished/(1000)+1).toString()
        }
        override fun onFinish() {
            game.setScoreAccordingToPosition(bestGuessSoFar)
            game.advanceGame()
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
        get() = R.layout.activity_challenge_mode

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

        for (i in 0 until predictions.topNClassNames.size) {
            if(game.isCorrect(predictions.topNClassNames[i]!!.single())) {
                perfText.text = predictions.topNScores[i].toString()
                if(bestGuessSoFar > i) {
                    bestGuessSoFar = i
                }
            }
            else {
                perfText.text = ""
            }
            Log.d("TEST2", predictions.topNClassNames[i].toString())
        }
        game.updatePerformanceScore(predictions.topNClassNames, predictions.topNScores)
        if(game.isCorrect(predictions.topNClassNames[0]!!.single()) && !answerCurrentlyCorrect) {
            correctAnswerCountdown.start()
            answerCurrentlyCorrect = true
        }else if (!game.isCorrect((predictions.topNClassNames[0]!!.single()))) {
            correctAnswerCountdownText.text = ""
            correctAnswerCountdown.cancel()
            answerCurrentlyCorrect = false
        }

    }

    private fun finishQuestion () {
        game.performanceScore = 0
        if(game.finished) {
            game.reset()
        }

        bestGuessSoFar = 99
        questionCountDown.start()
        updateUI()
    }










}
