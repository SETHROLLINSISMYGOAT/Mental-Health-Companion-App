package com.lifecoin.mentalhealthcompanionapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MoodDetectActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvMoodResult: TextView
    private lateinit var etJournal: EditText
    private lateinit var btnAnalyzeJournal: Button
    private lateinit var circularProgress: CircularProgressIndicator
    private lateinit var tvMoodScorePercent: TextView
    private lateinit var btnChatbot: Button
    private lateinit var btnMeditate: Button

    private var faceMoodScore = 0
    private var textMoodScore = 0

    private val database = FirebaseDatabase.getInstance()
    private val moodLogRef = database.getReference("moodLogs")

    private val chatbotLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val chatbotEffect = data?.getIntExtra("chatbotEffect", 0) ?: 0
            faceMoodScore += chatbotEffect
            updateCombinedMoodScore()
            saveMoodToFirebase()
        }
    }

    private val meditationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val meditationEffect = data?.getIntExtra("meditationEffect", 0) ?: 0
            textMoodScore += meditationEffect
            updateCombinedMoodScore()
            saveMoodToFirebase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_detect)

        previewView = findViewById(R.id.previewView)
        tvMoodResult = findViewById(R.id.tvMoodResult)
        etJournal = findViewById(R.id.etJournal)
        btnAnalyzeJournal = findViewById(R.id.btnAnalyzeJournal)
        circularProgress = findViewById(R.id.circularProgress)
        tvMoodScorePercent = findViewById(R.id.tvMoodScorePercent)
        btnChatbot = findViewById(R.id.btn_chatbot)
        btnMeditate = findViewById(R.id.btn_meditate)

        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Auth", "Signed in anonymously")
            } else {
                Log.e("Auth", "Sign-in failed", task.exception)
            }
        }

        requestCameraPermissionAndStart()

        btnAnalyzeJournal.setOnClickListener {
            val text = etJournal.text.toString()
            if (text.isNotBlank()) {
                analyzeTextMood(text)
            } else {
                Toast.makeText(this, "Please enter some journal text", Toast.LENGTH_SHORT).show()
            }
        }

        btnChatbot.setOnClickListener {
            val intent = Intent(this, ChatbotActivity::class.java)
            val combinedScore = ((faceMoodScore * 0.6) + (textMoodScore * 0.4)).toInt()
            intent.putExtra("combinedMoodScore", combinedScore)
            chatbotLauncher.launch(intent)
        }

        btnMeditate.setOnClickListener {
            Toast.makeText(this, "Starting meditation session...", Toast.LENGTH_SHORT).show()
            meditationLauncher.launch(Intent(this, MeditationActivity::class.java))
        }

        updateCombinedMoodScore()
    }

    private fun requestCameraPermissionAndStart() {
        val cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                tvMoodResult.text = "Camera permission denied"
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), FaceAnalyzer { moodScore, moodLabel ->
                        runOnUiThread {
                            faceMoodScore = moodScore
                            tvMoodResult.text = "Face mood detected: $moodLabel"
                            updateCombinedMoodScore()
                            saveMoodToFirebase()
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("MoodDetect", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeTextMood(text: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val score = simpleSentimentAnalysis(text)
            textMoodScore = score
            runOnUiThread {
                tvMoodResult.text = "Journal mood score: $textMoodScore%"
                updateCombinedMoodScore()
                saveMoodToFirebase()
            }
        }
    }

    private fun updateCombinedMoodScore() {
        val combinedScore = ((faceMoodScore * 0.6) + (textMoodScore * 0.4)).toInt()
        circularProgress.progress = combinedScore
        tvMoodScorePercent.text = "$combinedScore%"

        val baseText = tvMoodResult.text.toString().substringBefore('\n')
        val moodMessage = when {
            combinedScore >= 75 -> "You're feeling great! Keep it up! 🎉"
            combinedScore in 40..74 -> "You're feeling okay. Meditation or chatting may help."
            else -> "Mood still low. Consider booking a session with a therapist."
        }

        tvMoodResult.text = "$baseText\n$moodMessage"



        btnMeditate.isEnabled = combinedScore < 75
        btnChatbot.isEnabled = true
    }

    private fun saveMoodToFirebase() {
        val moodData = HashMap<String, Any>()
        val timestamp = System.currentTimeMillis()

        moodData["timestamp"] = timestamp
        moodData["faceMoodScore"] = faceMoodScore
        moodData["textMoodScore"] = textMoodScore
        moodData["combinedMoodScore"] = ((faceMoodScore * 0.6) + (textMoodScore * 0.4)).toInt()

        moodLogRef.push().setValue(moodData)
    }

    private fun simpleSentimentAnalysis(text: String): Int {
        val positiveWords = listOf("happy", "amazing", "good", "great", "fantastic", "joy", "love", "excited", "well", "awesome")
        val negativeWords = listOf("sad", "tired", "anxious", "bad", "angry", "depressed", "hate", "worried", "upset", "stress")

        var score = 50
        val words = text.lowercase(Locale.getDefault()).split("\\s+".toRegex())
        val positiveCount = words.count { it in positiveWords }
        val negativeCount = words.count { it in negativeWords }

        score += positiveCount * 10
        score -= negativeCount * 10

        return score.coerceIn(0, 100)
    }

    private class FaceAnalyzer(
        private val onMoodDetected: (moodScore: Int, moodLabel: String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        private val detector = FaceDetection.getClient(options)

        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val smileProb = face.smilingProbability ?: -1f

                        val (moodScore, moodLabel) = when {
                            smileProb >= 0.7 -> 90 to "Happy"
                            smileProb >= 0.3 -> 60 to "Neutral"
                            smileProb >= 0f -> 30 to "Sad"
                            else -> 50 to "Unknown"
                        }

                        onMoodDetected(moodScore, moodLabel)
                    }
                    imageProxy.close()
                }
                .addOnFailureListener {
                    imageProxy.close()
                }
        }
    }
}
