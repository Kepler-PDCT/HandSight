package com.example.handsight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import logic.GuessingGame


class GuessingGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)
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
        findViewById<TextView>(R.id.score).setText(
            "${game.score} / ${game.count} out of ${game.numberOfQuestions}"
        )
    }

    private fun updateUI() {
        setScore()

        val alternatives = game.getQuestion().alternatives!!

        findViewById<Button>(R.id.guess1).text = alternatives[0].toString()
        findViewById<Button>(R.id.guess2).text = alternatives[1].toString()
        findViewById<Button>(R.id.guess3).text = alternatives[2].toString()
        findViewById<Button>(R.id.guess4).text = alternatives[3].toString()

        val uri = "@drawable/" + game.getQuestion().correctAnswer.toString().toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)

        findViewById<ImageView>(R.id.imageView).setImageDrawable(res); // set as image
    }
}
