package com.example.smarthouse

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthouse.SB.Device
import com.example.smarthouse.SB.DeviceType
import com.example.smarthouse.SB.Room
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var deviceNameEditText: EditText
    private lateinit var roomNameAutoCompleteTextView: AutoCompleteTextView
    private lateinit var deviceTypesRecyclerView: RecyclerView
    private lateinit var deviceTypesAdapter: DeviceTypesAdapter
    private lateinit var deviceTypesList: MutableList<DeviceType>
    private var roomsList: MutableList<Room> = mutableListOf() // Инициализация здесь

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        deviceNameEditText = findViewById(R.id.deviceNameEditText)
        roomNameAutoCompleteTextView = findViewById(R.id.roomNameAutoCompleteTextView)
        deviceTypesRecyclerView = findViewById(R.id.deviceTypesRecyclerView)
        deviceTypesList = mutableListOf()
        deviceTypesAdapter = DeviceTypesAdapter(deviceTypesList)
        deviceTypesRecyclerView.adapter = deviceTypesAdapter
        deviceTypesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Скрытие чекбокса на экране добавления устройства
        deviceTypesRecyclerView.visibility = View.GONE

        loadDeviceTypes()
        loadRooms()

        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            saveDevice()
        }
    }

    private fun loadDeviceTypes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceTypesResult = SB.getSb().postgrest["device_types"].select().decodeList<DeviceType>()
                withContext(Dispatchers.Main) {
                    deviceTypesList.clear()
                    deviceTypesList.addAll(deviceTypesResult)
                    deviceTypesAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddDeviceActivity, "Ошибка загрузки типов устройств: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddDeviceActivity", "Ошибка загрузки типов устройств", e)
                }
            }
        }
    }

    private fun loadRooms() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получение текущего пользователя
                val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                    ?: throw Exception("Пользователь не авторизован")

                // Получение комнат, принадлежащих текущему пользователю
                val roomsResult = SB.getSb().postgrest["rooms"].select {
                    filter {
                        eq("home_id", user.id.toInt()) // Фильтр по home_id, который связан с пользователем
                    }
                }.decodeList<Room>()

                withContext(Dispatchers.Main) {
                    roomsList.clear()
                    roomsList.addAll(roomsResult)

                    // Настройка AutoCompleteTextView
                    val roomNames = roomsList.map { it.name }.toTypedArray()
                    val adapter = ArrayAdapter(this@AddDeviceActivity, android.R.layout.simple_dropdown_item_1line, roomNames)
                    roomNameAutoCompleteTextView.setAdapter(adapter)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddDeviceActivity, "Ошибка загрузки комнат: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddDeviceActivity", "Ошибка загрузки комнат", e)
                }
            }
        }
    }

    private fun saveDevice() {
        val deviceName = deviceNameEditText.text.toString().trim()
        val roomName = roomNameAutoCompleteTextView.text.toString().trim()
        if (deviceName.isNotEmpty() && roomName.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val selectedDeviceType = deviceTypesList[deviceTypesAdapter.selectedPosition]
                    val selectedRoom = roomsList.find { it.name.equals(roomName, ignoreCase = true) }
                    if (selectedRoom != null) {
                        val device = Device(
                            id = generateRandomId(), // Генерируем случайный ID для устройства
                            device_id = "device_id", // Пример значения
                            name = deviceName,
                            device_type = selectedDeviceType.id,
                            room_id = selectedRoom.id,
                            home_id = selectedRoom.home_id,
                            ison = false, // Устанавливаем состояние устройства
                            value = 100 // Устанавливаем значение по умолчанию
                        )

                        SB.getSb().postgrest["devices"].insert(device)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddDeviceActivity, "Устройство добавлено", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddDeviceActivity, "Комната не найдена", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddDeviceActivity, "Ошибка при добавлении устройства: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("AddDeviceActivity", "Ошибка при добавлении устройства", e)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Название устройства и комнаты не могут быть пустыми", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateRandomId(): Int {
        return Random.nextInt(100000, 999999)
    }

    fun onBackClick(view: View) {
        finish()
    }
}