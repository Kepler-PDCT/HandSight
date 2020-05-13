package com.example.handsight

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.handsight.Constants.GUESSING_HIGHSCORE
import com.example.handsight.Constants.HIGHSCORE_NAME
import com.example.handsight.Constants.PRIVATE_MODE
import com.example.handsight.Constants.SOUND_NAME
import kotlinx.android.synthetic.main.activity_guessing_mode.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.finish_popup.view.*
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

    private var game = GuessingGame()

    var gameFrozen = false

    fun madeGuess(view: View) {
        if(!gameFrozen) {
            val text = (view as Button).text
            var doneSound : MediaPlayer
            if(game.makeGuess(text.single())) {
                questionFinish.setImageDrawable(getDrawable(R.drawable.checkmark))
                doneSound = MediaPlayer.create(this, R.raw.success_perc)
            } else {
                questionFinish.setImageDrawable(getDrawable(R.drawable.fail))
                doneSound = MediaPlayer.create(this, R.raw.fail_perc)
            }
            questionFinish.visibility= View.VISIBLE
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
                    if (game.finished) {
                        val inflater: LayoutInflater =
                            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val popupView = inflater.inflate(R.layout.finish_popup, null)
                        val width = LinearLayout.LayoutParams.MATCH_PARENT
                        val height = LinearLayout.LayoutParams.MATCH_PARENT
                        val focusable = false
                        val popupWindow = PopupWindow(popupView, width, height, focusable)
                        popupView.RestartButton.setOnClickListener { popupWindow.dismiss(); game.reset(); updateUI(); gameFrozen = false }
                        popupView.MenuButton.setOnClickListener { popupWindow.dismiss(); finish() }
                        popupView.scoreTextView.text = "Score: ${game.score}"
                        val highScore = getSharedPreferences(HIGHSCORE_NAME, PRIVATE_MODE).getInt(GUESSING_HIGHSCORE, 0)
                        popupView.HighscoreTextView.text = "High Score: $highScore"
                        popupWindow.showAtLocation(
                            findViewById(android.R.id.content),
                            Gravity.CENTER,
                            0,
                            0
                        )
                        val sharedPref = getSharedPreferences(HIGHSCORE_NAME, PRIVATE_MODE)
                        val oldHighscore = sharedPref.getInt(GUESSING_HIGHSCORE, 0)
                        if (oldHighscore < game.score) {
                            val editor = sharedPref.edit()
                            editor.putInt(GUESSING_HIGHSCORE, game.score)
                            editor.apply()

                            // TODO display that new highscore was achieved.
                        }
                    } else {
                        updateUI()
                    }
                }
                override fun onAnimationEnd(animation: Animation?) {
                    questionFinish.visibility = View.GONE
                    if(!game.finished)
                        gameFrozen = false
                }

                override fun onAnimationStart(animation: Animation?) {
                    gameFrozen = true
                }
            })
            questionFinish.startAnimation(anim)
        }
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
