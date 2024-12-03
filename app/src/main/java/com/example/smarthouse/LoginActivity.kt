package com.example.smarthouse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        loginButton = findViewById(R.id.loginButton)

        setupTextWatchers()

        loginButton.setOnClickListener {
            if (validateFields()) {
                // Здесь будет код для входа через сервер
                // Пока что просто сохраняем флаг регистрации
                val sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isRegistered", true).apply()

                Toast.makeText(this, "Вход успешен", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PinCodeActivity::class.java))
            } else {
                Toast.makeText(this, "Ошибка валидации", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTextWatchers() {
        val emailEditText = emailLayout.editText as TextInputEditText
        val passwordEditText = passwordLayout.editText as TextInputEditText

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmail()
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword()
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateButtonState() {
        if (validateFields()) {
            loginButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonColor1)
        } else {
            loginButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonColor2)
        }
    }

    private fun validateFields(): Boolean {
        return validateEmail() && validatePassword()
    }

    private fun validateEmail(): Boolean {
        val email = emailLayout.editText?.text.toString().trim()
        if (email.isEmpty()) {
            emailLayout.error = "Электронная почта не может быть пустой"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Неверный формат email"
            return false
        } else {
            emailLayout.error = null
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val password = passwordLayout.editText?.text.toString().trim()
        if (password.isEmpty()) {
            passwordLayout.error = "Пароль не может быть пустым"
            return false
        } else if (password.length < 8) {
            passwordLayout.error = "Пароль должен содержать не менее 8 символов"
            return false
        } else {
            passwordLayout.error = null
            return true
        }
    }
}