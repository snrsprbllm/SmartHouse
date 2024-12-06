package com.example.smarthouse

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
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationActivity : AppCompatActivity() {

    private lateinit var usernameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        usernameLayout = findViewById(R.id.usernameLayout)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        setupTextWatchers()

        registerButton.setOnClickListener {
            if (validateFields()) {
                val username = usernameLayout.editText?.text.toString().trim()
                val email = emailLayout.editText?.text.toString().trim()
                val password = passwordLayout.editText?.text.toString().trim()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        SB.getSb().auth.signUpWith(Email,password) {
                           this.email = email
                           this.password = password

                       }
                        val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)

                        withContext(Dispatchers.Main) {
                            val sharedPreferences = getSharedPreferences("SmartHomePrefs", MODE_PRIVATE)
                            sharedPreferences.edit().putBoolean("isRegistered", true).apply()

                            Toast.makeText(this@RegistrationActivity, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegistrationActivity, PinCodeActivity::class.java))
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
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setupTextWatchers() {
        val usernameEditText = usernameLayout.editText as TextInputEditText
        val emailEditText = emailLayout.editText as TextInputEditText
        val passwordEditText = passwordLayout.editText as TextInputEditText

        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateUsername()
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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
            registerButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonColor1)
        } else {
            registerButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonColor2)
        }
    }

    private fun validateFields(): Boolean {
        return validateUsername() && validateEmail() && validatePassword()
    }

    private fun validateUsername(): Boolean {
        val username = usernameLayout.editText?.text.toString().trim()
        if (username.isEmpty()) {
            usernameLayout.error = "Имя пользователя не может быть пустым"
            return false
        } else {
            usernameLayout.error = null
            return true
        }
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
//Доделать флаги с переходом на вход, создание кода и т.д