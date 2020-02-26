package com.example.handsight

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import java.util.*

class LearningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)
    }

    val QUESTIONS = 10
    val questionArray  = (0..QUESTIONS).map { Random().nextInt() }

    var count = 0
    var correctGuessed = 0

    fun nextQuestion(): Pair<List<Int>, Int>{
        var questionArray  = (0..4).map { Random().nextInt(27) }.toList()
        val right = questionArray[0]
        return return Pair(questionArray, right)
    }

    fun switchImage(view: View) {
        count++
        if (count > QUESTIONS){
            val myIntent = Intent(this,  MainActivity::class.java)
            startActivity(myIntent)
        }

        var a = findViewById<ImageView>(R.id.imageView)
        a.setImageResource(R.drawable.hand2)
    }
}
