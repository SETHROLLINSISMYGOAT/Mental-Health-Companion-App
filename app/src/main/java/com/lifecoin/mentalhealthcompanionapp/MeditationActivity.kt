package com.lifecoin.mentalhealthcompanionapp
import okhttp3.MediaType.Companion.toMediaTypeOrNull

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MeditationActivity : AppCompatActivity() {

    private lateinit var tvMeditationResult: TextView
    private lateinit var btnStart: Button
    private lateinit var etReflection: EditText
    private lateinit var timerBar: ProgressBar
    private lateinit var imageView: ImageView
    private lateinit var aiResponseProgress: ProgressBar

    private lateinit var mediaPlayer: MediaPlayer
    private val meditationDuration = 30

    private val openAIKey = "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"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meditation)

        tvMeditationResult = findViewById(R.id.tvMeditationResult)
        btnStart = findViewById(R.id.btnStartMeditation)
        etReflection = findViewById(R.id.etMoodReflection)
        timerBar = findViewById(R.id.progressBar)
        imageView = findViewById(R.id.ivMeditationPerson)
        aiResponseProgress = findViewById(R.id.aiResponseProgress)

        timerBar.max = meditationDuration
        mediaPlayer = MediaPlayer.create(this, R.raw.calm_music)

        btnStart.setOnClickListener {
            btnStart.isEnabled = false
            startMeditation()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startMeditation() {
        tvMeditationResult.text = "Close your eyes, breathe deeply..."
        imageView.visibility = View.VISIBLE
        aiResponseProgress.visibility = View.GONE

        mediaPlayer.start()

        object : CountDownTimer((meditationDuration * 1000).toLong(), 1000) {
            var timePassed = 0

            override fun onTick(millisUntilFinished: Long) {
                timePassed++
                timerBar.progress = timePassed
            }

            override fun onFinish() {
                mediaPlayer.pause()
                imageView.visibility = View.GONE
                timerBar.progress = 0

                generateAIResponse(etReflection.text.toString())
            }
        }.start()
    }

    private fun generateAIResponse(userReflection: String) {
        if (userReflection.isBlank()) {
            tvMeditationResult.text = "You completed the session. Try to write your feelings next time!"
            finishWithResult(10)
            return
        }

        tvMeditationResult.text = "Analyzing your feelings..."
        aiResponseProgress.visibility = View.VISIBLE


        CoroutineScope(Dispatchers.IO).launch {
            val aiReply = callOpenAIAPI(userReflection)

            withContext(Dispatchers.Main) {
                aiResponseProgress.visibility = View.GONE

                if (aiReply.isNullOrEmpty()) {
                    tvMeditationResult.text = "Sorry, failed to analyze your reflection. Try again later."
                    finishWithResult(5)
                } else {
                    tvMeditationResult.text = aiReply
                    val effectScore = calculateMeditationEffect(aiReply)
                    finishWithResult(effectScore)
                }
            }
        }
    }

    private fun calculateMeditationEffect(aiText: String): Int {

        val positiveKeywords = listOf("peace", "calm", "relax", "better", "focused", "improved", "hope", "bright")
        val isPositive = positiveKeywords.any { aiText.contains(it, ignoreCase = true) }

        return if (isPositive) {
            15 + (0..5).random()
        } else {
            10 + (0..3).random()
        }
    }

    private suspend fun callOpenAIAPI(userReflection: String): String? {
        val client = OkHttpClient()

        val prompt = "You are a compassionate mental health assistant. Respond empathetically to the user's reflection:\n\n\"$userReflection\"\n\nYour response should encourage, support, and motivate the user."

        val jsonBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", listOf(
                mapOf("role" to "system", "content" to "You are a helpful assistant."),
                mapOf("role" to "user", "content" to prompt)
            ))
            put("max_tokens", 150)
            put("temperature", 0.7)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $openAIKey")
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("OpenAI", "Error response: ${response.code}")
                return null
            }
            val jsonResponse = JSONObject(response.body!!.string())
            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message").getString("content")
                message.trim()
            } else null
        } catch (e: Exception) {
            Log.e("OpenAI", "Exception: ${e.message}")
            null
        }
    }

    private fun finishWithResult(meditationEffect: Int) {
        val resultIntent = Intent()
        resultIntent.putExtra("meditationEffect", meditationEffect)
        setResult(Activity.RESULT_OK, resultIntent)


        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 4000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
