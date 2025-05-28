package com.lifecoin.mentalhealthcompanionapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.tv_register)
        val tvForgot = findViewById<TextView>(R.id.tv_forgot)

        sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            editEmail.error = null
            editPassword.error = null

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = "Enter a valid email address"
                editEmail.requestFocus()
                return@setOnClickListener
            }

            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")
            if (!passwordRegex.matches(password)) {
                editPassword.error =
                    "Password must be 8+ chars, include uppercase, lowercase, digit & special char"
                editPassword.requestFocus()
                return@setOnClickListener
            }

            val savedEmail = sharedPrefs.getString("user_email", null)
            val savedPassword = sharedPrefs.getString("user_password", null)

            if (email == savedEmail && password == savedPassword) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MoodHistoryActivity::class.java)
                intent.putExtra("user_name", email.substringBefore("@"))
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
