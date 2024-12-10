package com.example.smarthouse

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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.regex.Pattern
import kotlin.random.Random

class AddressInputActivity : AppCompatActivity() {

    private lateinit var addressLayout: TextInputLayout
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_input)

        addressLayout = findViewById(R.id.addressLayout)
        saveButton = findViewById(R.id.saveButton)

        setupTextWatcher()

        saveButton.setOnClickListener {
            val address = addressLayout.editText?.text.toString().trim()
            if (validateAddress(address)) {
                saveAddress(address)
            } else {
                Toast.makeText(this, "Пожалуйста, введите корректный адрес", Toast.LENGTH_SHORT).show()
            }
        }

        // Проверка авторизации пользователя
        checkUserAuthentication()
    }

    private fun setupTextWatcher() {
        val addressEditText = addressLayout.editText as TextInputEditText
        addressEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateAddress(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateAddress(address: String): Boolean {
        val addressPattern = Pattern.compile("г\\. [А-Яа-я]+, ул\\. [А-Яа-я]+, д\\. \\d+")
        return if (address.isEmpty()) {
            addressLayout.error = "Адрес не может быть пустым"
            false
        } else if (!addressPattern.matcher(address).matches()) {
            addressLayout.error = "Неверный формат адреса"
            false
        } else {
            addressLayout.error = null
            true
        }
    }

    private fun generateRandomHomeId(): Int {
        return Random.nextInt(100000, 999999) // Генерируем случайное число от 100000 до 999999
    }

    private fun saveAddress(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                    ?: throw Exception("Пользователь не авторизован")

                // Генерируем случайный UUID для идентификатора дома
                val homeId = generateRandomHomeId()

                // Проверяем, существует ли уже запись в таблице homes для данного пользователя
                val homeExists = SB.getSb().postgrest["homes"]
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeSingleOrNull<SB.Home>() != null

                if (!homeExists) {
                    // Если не существует, создаем новую запись в таблице homes
                    SB.getSb().postgrest["homes"].insert(
                        SB.Home(
                            id = homeId, // Используем сгенерированный UUID
                            user_id = user.id,
                            address = address
                        )
                    )
                } else {
                    // Если существует, обновляем адрес
                    SB.getSb().postgrest["homes"]
                        .update({
                            set("address", address)
                        }) {
                            filter {
                                eq("user_id", user.id)
                            }
                        }
                }

                // Обновляем адрес в таблице users
                SB.getSb().postgrest["users"]
                    .update({
                        set("address", address)
                    }) {
                        filter {
                            eq("id", user.id)
                        }
                    }

                // Сохраняем адрес в SharedPreferences
                withContext(Dispatchers.Main) {
                    getSharedPreferences("SmartHomePrefs", MODE_PRIVATE).edit().apply {
                        putString("userAddress", address)
                        apply()
                    }
                    Toast.makeText(this@AddressInputActivity, "Адрес успешно сохранен", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddressInputActivity, MainActivity::class.java))
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddressInputActivity, "Ошибка сохранения адреса: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddressInputActivity", "Ошибка сохранения адреса", e)
                }
            }
        }
    }

    private fun checkUserAuthentication() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                if (user == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddressInputActivity, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AddressInputActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddressInputActivity, "Ошибка проверки авторизации: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddressInputActivity", "Ошибка проверки авторизации", e)
                }
            }
        }
    }
}//г. Омск, ул. Ленина, д. 2