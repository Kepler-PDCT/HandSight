package com.example.handsight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView


class LearningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)
        setQuestions()
    }

    private val numberOfQuestion = 10

    private var count = 1
    private var current = ""
    private var correctGuessed = 0

    private fun nextQuestion(): Pair<List<Char>, Char> {
        var questionArray = (0..3).map { ((0..25).random() + 65).toChar() }.toList()
        val right = questionArray[(0..3).random()]
        return Pair(questionArray, right)
    }

    fun madeGuess(view: View) {
        val button = findViewById<Button>(view.id)
        if (button.text == current) {
            correctGuessed++
        }
        count++
        if (count > numberOfQuestion) {
            count = 1
            correctGuessed = 0
        }
        setQuestions()
    }

    private fun updateScore() {
        findViewById<TextView>(R.id.score).setText(
            "$correctGuessed / $count out of $numberOfQuestion"
        )
    }

    private fun setQuestions() {
        var questions = nextQuestion()
        updateScore()
        
        findViewById<Button>(R.id.guess1).setText(questions.first[0].toString())
        findViewById<Button>(R.id.guess2).setText(questions.first[1].toString())
        findViewById<Button>(R.id.guess3).setText(questions.first[2].toString())
        findViewById<Button>(R.id.guess4).setText(questions.first[3].toString())
        current = questions.second.toString()

        val uri = "@drawable/" + current.toLowerCase()
        val imageResource = resources.getIdentifier(uri, null, packageName) //get image  resource
        val res = resources.getDrawable(imageResource)

        findViewById<ImageView>(R.id.imageView).setImageDrawable(res); // set as image
    }

    fun updateUI(button: Button) {
        count++


    }
}
