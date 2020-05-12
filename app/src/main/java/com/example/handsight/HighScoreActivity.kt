package com.example.handsight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_high_score.*

class HighScoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_score)
        GuessingModeText.text = "Guessing Mode:" + "" //get from local storage
        ImitationModeText.text = "Imitation Mode" + ""
        ChallengeModeText.text = "Challenge Mode:" + ""
        WordModeText.text = "Word Mode:" + ""
    }
}
