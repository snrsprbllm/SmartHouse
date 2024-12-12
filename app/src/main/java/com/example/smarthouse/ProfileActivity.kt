package com.example.smarthouse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var userImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userImageView = findViewById(R.id.userImageView)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        addressEditText = findViewById(R.id.addressEditText)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)

        // Загрузка данных пользователя
        loadUserData()

        // Настройка кнопки "Сохранить"
        saveButton.setOnClickListener {
            saveUserData()
        }

        // Настройка кнопки "Удалить"
        deleteButton.setOnClickListener {
            deleteAccount()
        }

        // Настройка кнопки "Назад"
        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                    ?: throw Exception("Пользователь не авторизован")

                val userData = SB.getSb().postgrest["users"]
                    .select {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeSingleOrNull<SB.User>()

                val address = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE).getString("userAddress", "")

                withContext(Dispatchers.Main) {
                    usernameEditText.setText(userData?.username ?: "")
                    emailEditText.setText(user.email)
                    addressEditText.setText(address)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileActivity", "Ошибка загрузки данных", e)
                }
            }
        }
    }

    private fun saveUserData() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()

        if (username.isNotEmpty() && email.isNotEmpty() && address.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                        ?: throw Exception("Пользователь не авторизован")

                    // Обновление данных в таблице users
                    SB.getSb().postgrest["users"]
                        .update({
                            set("username", username)
                        }) {
                            filter {
                                eq("id", user.id)
                            }
                        }

                    // Обновление адреса в таблице homes
                    SB.getSb().postgrest["homes"]
                        .update({
                            set("address", address)
                        }) {
                            filter {
                                eq("user_id", user.id)
                            }
                        }

                    // Сохранение адреса в SharedPreferences
                    getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE).edit().apply {
                        putString("userAddress", address)
                        apply()
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Данные успешно сохранены", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Ошибка сохранения данных: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileActivity", "Ошибка сохранения данных", e)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAccount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                    ?: throw Exception("Пользователь не авторизован")

                // Удаляем пользователя из таблицы auth.users
                SB.getSb().auth.admin.deleteUser(user.id)

                // Удаляем все связанные данные
                val homes = SB.getSb().postgrest["homes"]
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<SB.Home>()

                homes.forEach { home ->
                    SB.getSb().postgrest["devices"]
                        .delete {
                            filter {
                                eq("home_id", home.id)
                            }
                        }
                    SB.getSb().postgrest["rooms"]
                        .delete {
                            filter {
                                eq("home_id", home.id)
                            }
                        }
                    SB.getSb().postgrest["homes"]
                        .delete {
                            filter {
                                eq("id", home.id)
                            }
                        }
                }

                // Удаляем пользователя из таблицы users
                SB.getSb().postgrest["users"]
                    .delete {
                        filter {
                            eq("id", user.id)
                        }
                    }

                // Сбрасываем флаги в SharedPreferences
                getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE).edit().apply {
                    putBoolean("isRegistered", false)
                    putBoolean("hasPinCode", false)
                    putBoolean("hasAddress", false)
                    apply()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Аккаунт успешно удален", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ProfileActivity, RegistrationActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Ошибка удаления аккаунта: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileActivity", "Ошибка удаления аккаунта", e)
                }
            }
        }
    }
}