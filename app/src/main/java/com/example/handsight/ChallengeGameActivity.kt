package com.example.handsight

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.handsight.Constants.CHALLENGE_HIGHSCORE
import com.example.handsight.Constants.HIGHSCORE_NAME
import com.example.handsight.Constants.PRIVATE_MODE
import com.example.handsight.Constants.SOUND_NAME
import kotlinx.android.synthetic.main.activity_guessing_mode.*
import kotlinx.android.synthetic.main.finish_popup.view.*
import logic.ImitationChallengeGame
import java.util.*

class ChallengeGameActivity : AbstractCameraXActivity() {

    private lateinit var predictions: AnalysisResult
    private var mMovingAvgSum: Long = 0
    private var questionStartTime: Long? = null
    private val game = ImitationChallengeGame()
    private var bestGuessSoFar = 99
    private val mMovingAvgQueue: Queue<Long> = LinkedList()
    private var answerCurrentlyCorrect: Boolean = false
    lateinit var correctAnswerCountdownText: TextView
    lateinit var questionCountdownText: TextView
    override val contentViewLayoutId: Int
        get() = R.layout.activity_challenge_mode
    private var soundEnabled = true

    fun loadSoundOption(): Boolean {
        val pref = getSharedPreferences(SOUND_NAME, Context.MODE_PRIVATE)
        graphicalSoundToggle(pref.getBoolean(SOUND_NAME, true))
        return pref.getBoolean(SOUND_NAME, true)
    }

    fun toggleSoundOption(view: View): Boolean {
        val pref = getSharedPreferences(SOUND_NAME, Context.MODE_PRIVATE)
        val state = pref.getBoolean(SOUND_NAME, true).not()
        val editor = pref.edit()
        editor.putBoolean(SOUND_NAME, state)
        editor.apply()
        soundEnabled = pref.getBoolean(SOUND_NAME, true)
        graphicalSoundToggle(state)
        return state
    }

    fun graphicalSoundToggle(state: Boolean){
        if (state){
            val res = resources.getDrawable(R.drawable.ic_volume_on)
            findViewById<ImageView>(R.id.volumeIcon).setImageDrawable(res)
        }
        else{
            val res = resources.getDrawable(R.drawable.ic_volume_mute)
            findViewById<ImageView>(R.id.volumeIcon).setImageDrawable(res)
        }
    }

    private var correctAnswerCountdown = object : CountDownTimer(2000, 100) {
        override fun onTick(millisUntilFinished: Long) {
            correctAnswerCountdownText.text = (millisUntilFinished / 1000f + 1).toInt().toString()
        }

        override fun onFinish() {
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
            finishQuestion(
                game.setScoreAccordingToPosition(bestGuessSoFar)
            )
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

        updateUI()
        questionStartTime = System.currentTimeMillis()
        questionCountDown.start()

        val pref = getSharedPreferences(SOUND_NAME, MODE_PRIVATE)
        soundEnabled = pref.getBoolean(SOUND_NAME, true)

        loadSoundOption()
    }

    private fun updateUI() {
        val correctLetter = game.getQuestion().correctAnswer.toString()
        findViewById<TextView>(R.id.CorrectLetterTextView).setText(correctLetter)
        findViewById<TextView>(R.id.questionTextView)!!.setText("Question ${game.currentQuestionIndex} of ${game.numberOfQuestions}")
        findViewById<TextView>(R.id.scoreTextView)!!.setText("Score: ${game.score}")
    }

    var gameFrozen = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun applyToUiAnalyzeImageResult(result: AnalysisResult?) {
        if(!gameFrozen) {
            mMovingAvgSum += result!!.moduleForwardDuration
            mMovingAvgQueue.add(result!!.moduleForwardDuration)
            if (mMovingAvgQueue.size > MOVING_AVG_PERIOD) {
                mMovingAvgSum -= mMovingAvgQueue.remove()
            }
            predictions = result

            for (i in 0 until predictions.topNClassNames.size) {
                if (game.isCorrect(predictions.topNClassNames[i]!!.single())) {
                    if (bestGuessSoFar > i) {
                        bestGuessSoFar = i
                    }
                }
                Log.d("TEST2", predictions.topNClassNames[i].toString())
            }
            game.updatePerformanceScore(predictions.topNClassNames, predictions.topNScores)
            Utils.updatePerformanceMeter(this, game.performanceScore)
            if (game.isCorrect(predictions.topNClassNames[0]!!.single()) && !answerCurrentlyCorrect) {
                correctAnswerCountdown.start()
                answerCurrentlyCorrect = true
            } else if (!game.isCorrect((predictions.topNClassNames[0]!!.single()))) {
                correctAnswerCountdownText.text = ""
                correctAnswerCountdown.cancel()
                answerCurrentlyCorrect = false
            }
        }
    }

    private fun finishQuestion(succeeded: Boolean) {

        val doneSound: MediaPlayer
        if (succeeded) {
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
                if (soundEnabled) {
                    doneSound.start()
                    doneSound.setOnCompletionListener { doneSound.stop() }
                }
                game.performanceScore = 0
                if (game.finished) {
                    val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val popupView = inflater.inflate(R.layout.finish_popup,null)
                    val width = LinearLayout.LayoutParams.WRAP_CONTENT + 1000
                    val height = LinearLayout.LayoutParams.WRAP_CONTENT + 1000
                    val focusable = false
                    val popupWindow = PopupWindow(popupView, width, height, focusable)
                    popupView.RestartButton.setOnClickListener {popupWindow.dismiss(); game.reset(); bestGuessSoFar = 99; questionCountDown.start(); updateUI(); gameFrozen = false}
                    popupView.MenuButton.setOnClickListener {popupWindow.dismiss(); finish()}
                    popupView.scoreTextView.text = "Score: ${game.score}"
                    val highScore = getSharedPreferences(HIGHSCORE_NAME, PRIVATE_MODE).getInt(Constants.CHALLENGE_HIGHSCORE, 0)
                    popupView.HighscoreTextView.text = "High Score: $highScore"
                    popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)
                    val sharedPref = getSharedPreferences(
                        HIGHSCORE_NAME,
                        PRIVATE_MODE
                    )
                    val oldHighscore = sharedPref.getInt(CHALLENGE_HIGHSCORE, 0)
                    if (oldHighscore < game.score) {
                        val editor = sharedPref.edit()
                        editor.putInt(CHALLENGE_HIGHSCORE, game.score)
                        editor.apply()

                        // TODO display that new highscore was achieved.
                    }
                }
                else {
                    bestGuessSoFar = 99
                    questionCountDown.start()
                    updateUI()
                    gameFrozen = false
                }

            }

            override fun onAnimationEnd(animation: Animation?) {
                questionFinish.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                questionFinish.visibility = View.VISIBLE
                gameFrozen = true
            }
        })

        questionFinish.startAnimation(anim)
    }
}
