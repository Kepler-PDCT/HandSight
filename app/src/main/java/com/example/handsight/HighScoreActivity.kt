package com.example.handsight

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.handsight.Constants.CHALLENGE_HIGHSCORE
import com.example.handsight.Constants.GUESSING_HIGHSCORE
import com.example.handsight.Constants.HIGHSCORE_NAME
import com.example.handsight.Constants.IMITATION_HIGHSCORE
import com.example.handsight.Constants.PRIVATE_MODE
import com.example.handsight.Constants.WORD_HIGHSCORE
import kotlinx.android.synthetic.main.activity_high_score.*

class HighScoreActivity : AppCompatActivity() {
    var guessingHighscore = 0F
    var imitationHighscore = 0F
    var challengeHighscore = 0F
    var wordHighscore = 0F

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_score)
        val sharedPref: SharedPreferences = getSharedPreferences(HIGHSCORE_NAME, PRIVATE_MODE)
        guessingHighscore = sharedPref.getFloat(GUESSING_HIGHSCORE, 0F)
        imitationHighscore = sharedPref.getFloat(IMITATION_HIGHSCORE, 0F)
        challengeHighscore = sharedPref.getFloat(CHALLENGE_HIGHSCORE, 0F)
        wordHighscore = sharedPref.getFloat(WORD_HIGHSCORE, 0F)
        guessing_highscore_score_text.text = "$wordHighscore" //get from local storage
        imitation_highscore_score_text.text = "$imitationHighscore"
        challenge_highscore_score_text.text = "$challengeHighscore"
        word_highscore_score_text.text = "$wordHighscore"
    }
}
