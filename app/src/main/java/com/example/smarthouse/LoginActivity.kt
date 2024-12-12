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
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        setupTextWatchers()

        loginButton.setOnClickListener {
            if (validateFields()) {
                val email = emailLayout.editText?.text.toString().trim()
                val password = passwordLayout.editText?.text.toString().trim()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Вход пользователя
                        SB.getSb().auth.signInWith(Email) {
                            this.email = email
                            this.password = password
                        }

                        // Получение текущего пользователя
                        val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                        withContext(Dispatchers.Main) {
                            val sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit().putBoolean("isRegistered", true).apply()

                            // Проверка адреса и перенаправление
                            checkAddressAndRedirect(user.id)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Ошибка входа: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Ошибка валидации", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
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
            loginButton.isEnabled = true
        } else {
            loginButton.isEnabled = false
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

    private fun checkAddressAndRedirect(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получение данных пользователя
                val userData = SB.getSb().postgrest["users"]
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingleOrNull<SB.User>()

                val hasAddress = userData?.address?.isNotEmpty() ?: false

                withContext(Dispatchers.Main) {
                    val sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("hasAddress", hasAddress).apply()
                    sharedPreferences.edit().putString("userAddress", userData?.address ?: "").apply()

                    Log.d("LoginActivity", "hasAddress: $hasAddress")

                    val intent = when {
                        !sharedPreferences.getBoolean("hasPinCode", false) -> Intent(this@LoginActivity, PinCodeActivity::class.java)
                        !hasAddress -> Intent(this@LoginActivity, AddressInputActivity::class.java)
                        else -> Intent(this@LoginActivity, MainActivity::class.java)
                    }
                    intent.putExtra("hasAddress", hasAddress)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Ошибка проверки адреса: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}