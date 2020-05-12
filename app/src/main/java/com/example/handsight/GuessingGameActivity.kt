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
import kotlinx.android.synthetic.main.activity_guessing_mode.*
import kotlinx.android.synthetic.main.finish_popup.view.*
import logic.GuessingGame


class GuessingGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guessing_mode)
        updateUI()
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
                    doneSound.start()
                    if (game.finished) {
                        val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val popupView = inflater.inflate(R.layout.finish_popup,null)
                        val width = LinearLayout.LayoutParams.WRAP_CONTENT
                        val height = LinearLayout.LayoutParams.WRAP_CONTENT
                        val focusable = true
                        val popupWindow = PopupWindow(popupView, width, height, focusable)
                        popupView.RestartButton.setOnClickListener {popupWindow.dismiss(); game.reset(); updateUI()}
                        popupView.MenuButton.setOnClickListener {popupWindow.dismiss(); finish()}
                        popupWindow.showAtLocation(findViewById(android.R.id.content) ,Gravity.CENTER, 0, 0)
                    }else {
                        updateUI()
                    }
                }

                override fun onAnimationEnd(animation: Animation?) {
                    questionFinish.visibility = View.GONE
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

        findViewById<View>(R.id.answer1).findViewById<Button>(R.id.button).text = alternatives[0].toString()
        findViewById<View>(R.id.answer2).findViewById<Button>(R.id.button).text = alternatives[1].toString()
        findViewById<View>(R.id.answer3).findViewById<Button>(R.id.button).text = alternatives[2].toString()
        findViewById<View>(R.id.answer4).findViewById<Button>(R.id.button).text = alternatives[3].toString()

        val uri = "@drawable/" + game.getQuestion().correctAnswer.toString().toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)
        findViewById<ImageView>(R.id.handImageView).setImageDrawable(res); // set as image
    }
}
