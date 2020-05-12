package com.example.handsight

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.ImageView
import com.example.handsight.Constants.SOUND_NAME


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadSoundOption()
    }

    override fun onResume() {
        super.onResume()
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
        graphicalSoundToggle(state)
        return state
    }

    fun graphicalSoundToggle(state: Boolean){
        if (state){
            val res = resources.getDrawable(R.drawable.volume_on)
            findViewById<ImageView>(R.id.volumeIcon).setImageDrawable(res)
        }
        else{
            val res = resources.getDrawable(R.drawable.volume_mute)
            findViewById<ImageView>(R.id.volumeIcon).setImageDrawable(res)
        }
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

    fun launchHighScore(view: View){
        val myIntent = Intent(this,  HighScoreActivity::class.java)
        startActivity(myIntent)
    }
}
