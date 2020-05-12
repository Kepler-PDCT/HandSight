package com.example.handsight

import android.R.attr.button
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import kotlinx.android.synthetic.main.activity_guessing_mode.*
import logic.GuessingGame


class GuessingGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guessing_mode)
        updateUI()
    }

    private val game = GuessingGame()

    fun madeGuess(view: View) {
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
            }

            override fun onAnimationEnd(animation: Animation?) {
                questionFinish.visibility = View.GONE
                if (game.finished) {
                    // Redirect to Score view.
                    // Until then just reset game.
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
