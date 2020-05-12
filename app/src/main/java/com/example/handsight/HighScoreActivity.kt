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
        guessingHighscore = sharedPref.getInt(GUESSING_HIGHSCORE, 0F)
        imitationHighscore = sharedPref.getInt(IMITATION_HIGHSCORE, 0F)
        challengeHighscore = sharedPref.getInt(CHALLENGE_HIGHSCORE, 0F)
        wordHighscore = sharedPref.getInt(WORD_HIGHSCORE, 0F)
        GuessingModeText.text = "Guessing Mode: $wordHighscore" //get from local storage
        ImitationModeText.text = "Imitation Mode: $imitationHighscore"
        ChallengeModeText.text = "Challenge Mode: $challengeHighscore"
        WordModeText.text = "Word Mode: $wordHighscore"
    }
}
