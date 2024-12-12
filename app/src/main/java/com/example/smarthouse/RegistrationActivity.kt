package com.example.smarthouse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationActivity : AppCompatActivity() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        usernameLayout = findViewById(R.id.usernameLayout)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        setupTextWatchers()

        registerButton.setOnClickListener {
            if (validateFields()) {
                val email = emailLayout.editText?.text.toString().trim()
                val password = passwordLayout.editText?.text.toString().trim()
                val username = usernameLayout.editText?.text.toString().trim()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Регистрация пользователя
                        SB.getSb().auth.signUpWith(Email) {
                            this.email = email
                            this.password = password
                        }

                        // Получение текущего пользователя
                        val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                        Log.e("!!!!!", "" + user.id)

                        // Добавление имени пользователя в таблицу
                        val userData = mapOf(
                            "id" to user.id,
                            "username" to username
                        )
                        SB.getSb().postgrest["users"].insert(userData)

                        // Сохранение флага isRegistered в SharedPreferences
                        withContext(Dispatchers.Main) {
                            val sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit().putBoolean("isRegistered", true).apply()

                            Toast.makeText(this@RegistrationActivity, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegistrationActivity, PinCodeActivity::class.java))
                            finish()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegistrationActivity, "Ошибка регистрации: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Ошибка валидации", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
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
            registerButton.isEnabled = true
        } else {
            registerButton.isEnabled = false
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
        } else {
            passwordLayout.error = null
            return true
        }
    }
}