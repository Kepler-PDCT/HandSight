package com.example.handsight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import logic.GuessingGame


class GuessingGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guessing_mode)
        updateUI()
    }

    private val game = GuessingGame()

    fun madeGuess(view: View) {
        val text = findViewById<Button>(view.id).text
        game.makeGuess(text.single())

        if (game.finished) {
            // Redirect to Score view.
            // Until then just reset game.
            game.reset()
        }
        updateUI()
    }

    private fun setScore() {
        findViewById<TextView>(R.id.questionTextView).setText(
            "Question ${game.count} of ${game.numberOfQuestions}"
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
