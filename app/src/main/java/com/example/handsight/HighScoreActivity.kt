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
    var guessingHighscore = 0
    var imitationHighscore = 0
    var challengeHighscore = 0
    var wordHighscore = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_score)
        val sharedPref: SharedPreferences = getSharedPreferences(HIGHSCORE_NAME, PRIVATE_MODE)
        guessingHighscore = sharedPref.getInt(GUESSING_HIGHSCORE, 0)
        imitationHighscore = sharedPref.getInt(IMITATION_HIGHSCORE, 0)
        challengeHighscore = sharedPref.getInt(CHALLENGE_HIGHSCORE, 0)
        wordHighscore = sharedPref.getInt(WORD_HIGHSCORE, 0)
        guessing_highscore_score_text.text = "$guessingHighscore" //get from local storage
        imitation_highscore_score_text.text = "$imitationHighscore"
        challenge_highscore_score_text.text = "$challengeHighscore"
        word_highscore_score_text.text = "$wordHighscore"
    }
}
