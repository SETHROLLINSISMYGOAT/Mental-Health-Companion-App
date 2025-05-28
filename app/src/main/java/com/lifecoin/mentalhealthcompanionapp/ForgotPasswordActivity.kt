package com.lifecoin.mentalhealthcompanionapp

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var editEmail: TextInputEditText
    private lateinit var editNewPassword: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var btnReset: MaterialButton
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        editEmail = findViewById(R.id.editForgotEmail)
        editNewPassword = findViewById(R.id.editNewPassword)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        btnReset = findViewById(R.id.btn_reset_password)

        passwordLayout.setEndIconOnClickListener {
            isPasswordVisible = !isPasswordVisible
            editNewPassword.transformationMethod =
                if (isPasswordVisible)
                    HideReturnsTransformationMethod.getInstance()
                else
                    PasswordTransformationMethod.getInstance()
            editNewPassword.setSelection(editNewPassword.text?.length ?: 0)
        }

        btnReset.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val newPassword = editNewPassword.text.toString().trim()

            emailLayout.error = null
            passwordLayout.error = null

            if (email.isEmpty()) {
                emailLayout.error = "Email is required"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Enter a valid email address"
                return@setOnClickListener
            }

            val passwordRegex =
                Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")
            if (!passwordRegex.matches(newPassword)) {
                passwordLayout.error =
                    "Use 8+ chars with uppercase, lowercase, digit & special char"
                return@setOnClickListener
            }

            val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val savedEmail = sharedPrefs.getString("user_email", null)

            if (email == savedEmail) {
                sharedPrefs.edit().putString("user_password", newPassword).apply()
                Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                emailLayout.error = "Email not found in records"
            }
        }
    }
}
