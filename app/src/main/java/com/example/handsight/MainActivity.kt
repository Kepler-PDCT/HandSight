package com.example.handsight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun launchLearningMode(view: View) {
        val myIntent = Intent(this,  GuessingGameActivity::class.java)
        startActivity(myIntent)
    }

    fun launchImitationMode(view: View) {
        val myIntent = Intent(this,  ImitationGameActivity::class.java)
        startActivity(myIntent)
    }

    fun launchChallengeMode(view: View) {
        val myIntent = Intent(this,  ChallengeGameActivity::class.java)
        startActivity(myIntent)
    }

    fun launchWordMode(view: View) {
        val myIntent = Intent(this,  WordGameActivity::class.java)
        startActivity(myIntent)
    }

    fun launchHighScore(view: View){
        val myIntent = Intent(this,  HighScoreActivity::class.java)
        startActivity(myIntent)
    }
}
