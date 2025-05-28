package com.lifecoin.mentalhealthcompanionapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ChatbotActivity : AppCompatActivity() {

    private lateinit var tvChatFeedback: TextView
    private lateinit var etUserInput: EditText
    private lateinit var btnTalkToBot: Button
    private lateinit var tvSuggestions: TextView

    private var combinedMoodScore: Int = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        tvChatFeedback = findViewById(R.id.tvChatFeedback)
        etUserInput = findViewById(R.id.etUserInput)
        btnTalkToBot = findViewById(R.id.btnTalkToBot)
        tvSuggestions = findViewById(R.id.tvSuggestions)

        combinedMoodScore = intent.getIntExtra("combinedMoodScore", 50)

        tvChatFeedback.text = when {
            combinedMoodScore >= 75 -> "You're feeling great! Want to talk about what made your day good? 😄"
            combinedMoodScore in 40..74 -> "You're doing okay. Anything on your mind you’d like to chat about? 🤔"
            else -> "You seem low today. I’m here if you want to talk or vent. 💬"
        }


        showMoodBasedSuggestions(combinedMoodScore)

        btnTalkToBot.setOnClickListener {
            val userMessage = etUserInput.text.toString().trim()

            val reply = when {
                userMessage.contains("stress", true) ->
                    "I'm sorry you're feeling stressed. Deep breathing and sharing your thoughts can help. 🌿"
                userMessage.contains("happy", true) ->
                    "That's wonderful! Want to tell me more about what made you feel happy? 😊"
                userMessage.isBlank() ->
                    "Please enter something so I can help."
                else ->
                    "Thanks for sharing that. Do you want to explore more about this?"
            }

            tvChatFeedback.text = reply
        }
    }

    private fun showMoodBasedSuggestions(score: Int) {
        val suggestions = when {
            score >= 75 -> listOf(
                "🎵 Listen to your favorite music",
                "🏃 Go for a walk to maintain your good mood",
                "🧘 Try a quick meditation to stay centered",
                "💌 Write down 3 things you're grateful for"
            )
            score in 40..74 -> listOf(
                "🧘 Do a 5-minute breathing exercise",
                "📓 Write a short gratitude journal",
                "📱 Talk to a friend or family member",
                "🎨 Try a creative hobby like drawing or music"
            )
            else -> listOf(
                "☁️ Take a deep breath and try a guided meditation",
                "📔 Write down what’s bothering you in a journal",
                "☎️ Talk to someone you trust",
                "💬 Consider booking a therapist session"
            )
        }

        val suggestionText = "✨ Suggested Activities:\n" + suggestions.joinToString("\n")
        tvSuggestions.text = suggestionText
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val resultIntent = intent
        resultIntent.putExtra("chatbotEffect", 10)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}

