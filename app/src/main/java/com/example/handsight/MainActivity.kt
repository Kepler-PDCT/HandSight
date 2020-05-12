package com.example.handsight

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import com.example.handsight.Constants.SOUND_NAME


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun loadSoundOption(): Boolean {
        val pref = getSharedPreferences(SOUND_NAME, Context.MODE_PRIVATE)
        return pref.getBoolean(SOUND_NAME, true)
    }

    fun toggleSoundOption(): Boolean {
        val pref = getSharedPreferences(SOUND_NAME, Context.MODE_PRIVATE)
        val state = pref.getBoolean(SOUND_NAME, true).not()
        val editor = pref.edit()
        editor.putBoolean(SOUND_NAME, state)
        editor.apply()
        return state
    }

    fun launchLearningMode(view: View) {
        val myIntent = Intent(this, GuessingGameActivity::class.java)
        startActivity(myIntent)
    }

    fun launchImitationMode(view: View) {
        val myIntent = Intent(this, ImitationGameActivity::class.java)
        startActivity(myIntent)
    }

    fun launchChallengeMode(view: View) {
        val myIntent = Intent(this, ChallengeGameActivity::class.java)
        startActivity(myIntent)
    }

    fun launchWordMode(view: View) {
        val myIntent = Intent(this, WordGameActivity::class.java)
        startActivity(myIntent)
    }
}
