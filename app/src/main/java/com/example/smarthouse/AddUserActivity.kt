package com.example.smarthouse

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthouse.SB.User
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddUserActivity : AppCompatActivity() {

    private lateinit var userNameEditText: EditText
    private lateinit var userStatusRecyclerView: RecyclerView
    private lateinit var userStatusAdapter: UserStatusAdapter
    private lateinit var userStatusList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        userNameEditText = findViewById(R.id.userNameEditText)
        userStatusRecyclerView = findViewById(R.id.userStatusRecyclerView)
        userStatusList = mutableListOf("Сын", "Дочь", "Отец", "Мать")
        userStatusAdapter = UserStatusAdapter(userStatusList)
        userStatusRecyclerView.adapter = userStatusAdapter
        userStatusRecyclerView.layoutManager = LinearLayoutManager(this)

        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            saveUser()
        }
    }

    private fun saveUser() {
        val userName = userNameEditText.text.toString().trim()
        if (userName.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val selectedStatus = userStatusList[userStatusAdapter.selectedPosition]

                    val user = User(
                        id = "user_id", // Пример значения
                        username = userName,
                        address = "user_address" // Пример значения
                    )

                    SB.getSb().postgrest["users"].insert(user)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddUserActivity, "Пользователь добавлен", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddUserActivity, "Ошибка при добавлении пользователя: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("AddUserActivity", "Ошибка при добавлении пользователя", e)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
        }
    }

    fun onBackClick(view: View) {
        finish()
    }
}