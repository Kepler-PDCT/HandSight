package com.example.handsight

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewStub
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import com.example.handsight.Utils.updatePerformanceMeter
import kotlinx.android.synthetic.main.activity_guessing_mode.questionFinish
import kotlinx.android.synthetic.main.activity_imitation_mode.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.progress_bar.view.*
import logic.ImitationChallengeGame
import java.time.LocalDateTime
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
    lateinit var progressBarPadding : Guideline
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
                    finishQuestion(true)
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
            finishQuestion(game.setScoreAccordingToPosition(bestGuessSoFar))
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
        progressBarPadding = ProgressBar.InverseGuideline

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

        findViewById<TextView>(R.id.questionTextView)!!.setText("Question ${game.currentQuestionIndex} of ${game.numberOfQuestions}")
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
        //Log.d("TEST", result.topNScores[0].toString())

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
        updatePerformanceMeter(this, game.performanceScore)
    }

    private fun finishQuestion(succeeded:Boolean) {

        val doneSound : MediaPlayer
        if(succeeded) {
            questionFinish.setImageDrawable(getDrawable(R.drawable.checkmark))
            doneSound = MediaPlayer.create(this, R.raw.success_perc)
        } else {
            questionFinish.setImageDrawable(getDrawable(R.drawable.fail))
            doneSound = MediaPlayer.create(this, R.raw.fail_perc)
        }
        val anim = AlphaAnimation(0f, 1f)
        anim.duration = 300
        anim.repeatCount = 1
        anim.repeatMode = Animation.REVERSE
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                doneSound.start()
                doneSound.setOnCompletionListener { doneSound.stop() }
            }

            override fun onAnimationEnd(animation: Animation?) {
                questionFinish.visibility = View.GONE

                game.performanceScore = 0
                if (game.finished) {
                    game.reset()
                }


                bestGuessSoFar = 99
                questionCountDown.start()
                updateUI()
                Log.d("TEST", game.score.toString())
            }

            override fun onAnimationStart(animation: Animation?) {
                questionFinish.visibility= View.VISIBLE
            }
        })

        questionFinish.startAnimation(anim)

    }

}