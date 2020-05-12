package com.example.handsight

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.handsight.Constants.GUESSING_HIGHSCORE
import com.example.handsight.Constants.HIGHSCORE_NAME
import com.example.handsight.Constants.PRIVATE_MODE
import com.example.handsight.Constants.SOUND_NAME
import kotlinx.android.synthetic.main.activity_guessing_mode.*
import logic.GuessingGame


class GuessingGameActivity : AppCompatActivity() {
    private var soundEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guessing_mode)
        updateUI()

        val pref = getSharedPreferences(SOUND_NAME, MODE_PRIVATE)
        soundEnabled = pref.getBoolean(SOUND_NAME, true)
        loadSoundOption()
    }

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

    private val game = GuessingGame()

    fun madeGuess(view: View) {
        val text = (view as Button).text
        var doneSound: MediaPlayer
        if (game.makeGuess(text.single())) {
            questionFinish.setImageDrawable(getDrawable(R.drawable.checkmark))
            doneSound = MediaPlayer.create(this, R.raw.success_perc)
        } else {
            questionFinish.setImageDrawable(getDrawable(R.drawable.fail))
            doneSound = MediaPlayer.create(this, R.raw.fail_perc)
        }
        questionFinish.visibility = View.VISIBLE
        val anim = AlphaAnimation(0f, 1f)
        anim.duration = 200
        anim.repeatCount = 1
        anim.repeatMode = Animation.REVERSE
        anim.setAnimationListener(object : AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                if (soundEnabled) {
                    doneSound.start()
                    doneSound.setOnCompletionListener { doneSound.stop() }
                }

            }

            override fun onAnimationEnd(animation: Animation?) {
                questionFinish.visibility = View.GONE
                if (game.finished) {
                    val sharedPref = getSharedPreferences(HIGHSCORE_NAME, PRIVATE_MODE)
                    val oldHighscore = sharedPref.getInt(GUESSING_HIGHSCORE, 0)
                    if (oldHighscore < game.score) {
                        val editor = sharedPref.edit()
                        editor.putInt(GUESSING_HIGHSCORE, game.score)
                        editor.apply()

                        // TODO display that new highscore was achieved.
                    }

                    game.reset()
                }
                updateUI()
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        questionFinish.startAnimation(anim)

    }

    private fun setScore() {
        findViewById<TextView>(R.id.questionTextView).setText(
            "Question ${game.currentQuestionIndex} of ${game.numberOfQuestions}"
        )

        findViewById<TextView>(R.id.scoreTextView).setText(
            "Score: ${game.score}"
        )
    }

    private fun updateUI() {
        setScore()

        val alternatives = game.getQuestion().alternatives!!

        findViewById<View>(R.id.answer1).findViewById<Button>(R.id.button).text =
            alternatives[0].toString()
        findViewById<View>(R.id.answer2).findViewById<Button>(R.id.button).text =
            alternatives[1].toString()
        findViewById<View>(R.id.answer3).findViewById<Button>(R.id.button).text =
            alternatives[2].toString()
        findViewById<View>(R.id.answer4).findViewById<Button>(R.id.button).text =
            alternatives[3].toString()

        val uri = "@drawable/" + game.getQuestion().correctAnswer.toString().toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)
        findViewById<ImageView>(R.id.handImageView).setImageDrawable(res); // set as image
    }
}
