package com.lifecoin.mentalhealthcompanionapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MoodHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_history)

        val analyzeButton = findViewById<Button>(R.id.btn_analyze)

        analyzeButton.setOnClickListener {
            val intent = Intent(this, MoodDetectActivity::class.java)
            startActivity(intent)
        }
    }
}
