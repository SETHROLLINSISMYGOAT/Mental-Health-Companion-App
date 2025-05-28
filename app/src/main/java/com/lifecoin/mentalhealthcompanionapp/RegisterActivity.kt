package com.lifecoin.mentalhealthcompanionapp

import android.content.Context
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private var isPasswordVisible = false

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidStrongPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")
        return passwordRegex.matches(password)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editEmail = findViewById(R.id.editRegisterEmail)
        editPassword = findViewById(R.id.editRegisterPassword)
        btnRegister = findViewById(R.id.btn_register)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)


        passwordLayout.setEndIconOnClickListener {
            isPasswordVisible = !isPasswordVisible
            editPassword.transformationMethod = if (isPasswordVisible)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
            editPassword.setSelection(editPassword.text?.length ?: 0)
        }

        btnRegister.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isEmpty()) {
                emailLayout.error = "Email is required"
                return@setOnClickListener
            } else if (!isValidEmail(email)) {
                emailLayout.error = "Enter a valid email"
                return@setOnClickListener
            } else {
                emailLayout.error = null
            }

            if (password.isEmpty()) {
                passwordLayout.error = "Password is required"
                return@setOnClickListener
            } else if (!isValidStrongPassword(password)) {
                passwordLayout.error =
                    "Use 8+ chars with uppercase, lowercase, digit & special char"
                return@setOnClickListener
            } else {
                passwordLayout.error = null
            }

            val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val savedEmail = sharedPrefs.getString("user_email", null)

            if (savedEmail != null && savedEmail == email) {
                Toast.makeText(this, "Already registered with this email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPrefs.edit().apply {
                putString("user_email", email)
                putString("user_password", password)
                apply()
            }

            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

