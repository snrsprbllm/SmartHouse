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
import com.example.smarthouse.SB.Room
import com.example.smarthouse.SB.RoomType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class AddRoomActivity : AppCompatActivity() {

    private lateinit var roomNameEditText: EditText
    private lateinit var roomTypesRecyclerView: RecyclerView
    private lateinit var roomTypesAdapter: RoomTypesAdapter
    private lateinit var roomTypesList: MutableList<RoomType>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_room)

        roomNameEditText = findViewById(R.id.roomNameEditText)
        roomTypesRecyclerView = findViewById(R.id.roomTypesRecyclerView)
        roomTypesList = mutableListOf()
        roomTypesAdapter = RoomTypesAdapter(roomTypesList)
        roomTypesRecyclerView.adapter = roomTypesAdapter
        roomTypesRecyclerView.layoutManager = LinearLayoutManager(this)

        loadRoomTypes()

        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            saveRoom()
        }
    }

    private fun loadRoomTypes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val roomTypesResult = SB.getSb().postgrest["room_types"].select().decodeList<RoomType>()
                withContext(Dispatchers.Main) {
                    roomTypesList.clear()
                    roomTypesList.addAll(roomTypesResult)
                    roomTypesAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddRoomActivity, "Ошибка загрузки типов комнат: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddRoomActivity", "Ошибка загрузки типов комнат", e)
                }
            }
        }
    }

    private fun saveRoom() {
        val roomName = roomNameEditText.text.toString().trim()
        if (roomName.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val selectedRoomType = roomTypesList[roomTypesAdapter.selectedPosition]

                    // Получаем текущего пользователя
                    val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                        ?: throw Exception("Пользователь не авторизован")

                    // Получаем дома пользователя
                    val homes = SB.getSb().postgrest["homes"]
                        .select {
                            filter {
                                eq("user_id", user.id)
                            }
                        }
                        .decodeList<SB.Home>()

                    val homeId = if (homes.isNotEmpty()) {
                        homes.first().id
                    } else {
                        // Если дома нет, создаем новый дом с рандомным ID
                        val newHome = SB.Home(
                            id = generateRandomId(), // Генерируем случайный ID
                            user_id = user.id,
                            address = "Default Address" // Установите адрес дома по умолчанию
                        )
                        SB.getSb().postgrest["homes"].insert(newHome).decodeSingle<SB.Home>().id
                    }

                    // Проверяем, что homeId не null
                    if (homeId != null) {
                        val room = Room(
                            id = generateRandomId(), // Генерируем случайный ID для комнаты
                            name = roomName,
                            room_type = selectedRoomType.id,
                            home_id = homeId,
                            image_path = selectedRoomType.image_path
                        )

                        SB.getSb().postgrest["rooms"].insert(room)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddRoomActivity, "Комната добавлена", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddRoomActivity, "Ошибка при создании дома", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddRoomActivity, "Ошибка при добавлении комнаты: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("AddRoomActivity", "Ошибка при добавлении комнаты", e)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Название комнаты не может быть пустым", Toast.LENGTH_SHORT).show()
        }
    }

    fun onBackClick(view: View) {
        finish()
    }

    fun generateRandomId(): Int {
        return Random.nextInt(100000, 999999)
    }
}