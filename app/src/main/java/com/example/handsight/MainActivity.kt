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

    fun clicked(view: View) {
        val myIntent = Intent(this,  GuessingGameActivity::class.java)
        startActivity(myIntent)
    }

}
