package com.example.smarthouse

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthouse.SB.Device
import com.example.smarthouse.SB.DeviceType
import com.example.smarthouse.SB.Room
import com.example.smarthouse.SB.RoomType
import com.example.smarthouse.SB.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class MainActivity : AppCompatActivity() {

    private lateinit var roomsRecyclerView: RecyclerView
    private lateinit var mainAdapter: MainAdapter
    private lateinit var roomsList: MutableList<Room>
    private lateinit var devicesList: MutableList<Device>
    private lateinit var usersList: MutableList<User>
    private lateinit var roomTypesMap: MutableMap<Int, RoomType>
    private lateinit var deviceTypesMap: MutableMap<Int, DeviceType>
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedCategory: Int = R.id.roomsCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)

        // Инициализация списков
        roomsList = mutableListOf()
        devicesList = mutableListOf()
        usersList = mutableListOf()
        roomTypesMap = mutableMapOf()
        deviceTypesMap = mutableMapOf()

        // Инициализация RecyclerView
        roomsRecyclerView = findViewById(R.id.roomsRecyclerView)
        mainAdapter = MainAdapter(roomsList, deviceTypesMap)
        roomsRecyclerView.adapter = mainAdapter
        roomsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Добавление отступов между элементами RecyclerView
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        roomsRecyclerView.addItemDecoration(dividerItemDecoration)

        // Загрузка данных из базы данных
        loadRooms()
        loadDevices()
        loadUsers()
        loadRoomTypes()
        loadDeviceTypes()

        // Настройка заголовка и адреса
        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val addressTextView = findViewById<TextView>(R.id.addressTextView)
        titleTextView.text = "Твой дом"

        // Загрузка адреса из SharedPreferences
        loadUserAddress(addressTextView)

        // Настройка иконок и категорий
        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            // Заглушка для перехода в профиль пользователя
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val plusIcon = findViewById<ImageView>(R.id.plusIcon)
        plusIcon.setOnClickListener {
            onAddDeviceClick(it)
        }

        val roomsCategory = findViewById<TextView>(R.id.roomsCategory)
        val devicesCategory = findViewById<TextView>(R.id.devicesCategory)
        val usersCategory = findViewById<TextView>(R.id.usersCategory)

        roomsCategory.setOnClickListener {
            // Переключение на категорию "Комнаты"
            updateCategory(roomsCategory, roomsList)
            selectedCategory = R.id.roomsCategory
        }

        devicesCategory.setOnClickListener {
            // Переключение на категорию "Устройства"
            updateCategory(devicesCategory, devicesList)
            selectedCategory = R.id.devicesCategory
        }

        usersCategory.setOnClickListener {
            // Переключение на категорию "Пользователи"
            updateCategory(usersCategory, usersList)
            selectedCategory = R.id.usersCategory
        }
    }

    private fun loadUserAddress(addressTextView: TextView) {
        val address = sharedPreferences.getString("userAddress", "Адрес не указан")
        addressTextView.text = address
    }

    private fun loadRooms() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val roomsResult = SB.getSb().postgrest["rooms"].select().decodeList<SB.Room>()
                withContext(Dispatchers.Main) {
                    roomsList.clear()
                    roomsList.addAll(roomsResult)
                    mainAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки комнат: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Ошибка загрузки комнат", e)
                }
            }
        }
    }

    private fun loadDevices() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val devicesResult = SB.getSb().postgrest["devices"].select().decodeList<Device>()
                withContext(Dispatchers.Main) {
                    devicesList.clear()
                    devicesList.addAll(devicesResult)
                    mainAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки устройств: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Ошибка загрузки устройств", e)
                }
            }
        }
    }


    private fun getCurrentUserAddress(addressTextView: TextView) {
        addressTextView.text = "Загрузка адреса..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                    ?: throw Exception("Пользователь не авторизован")

                val userData = SB.getSb().postgrest["homes"]
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<JsonObject>() // Десериализуем в JsonObject
                    .map { json ->
                        SB.User(
                            id = json["id"]?.jsonPrimitive?.content ?: "", // Преобразуем число в строку
                            username = json["username"]?.jsonPrimitive?.content ?: "",
                            address = json["address"]?.jsonPrimitive?.content
                        )
                    }

                val address = userData.firstOrNull()?.address

                withContext(Dispatchers.Main) {
                    if (!address.isNullOrEmpty()) {
                        addressTextView.text = address
                    } else {
                        addressTextView.text = "Адрес не указан"
                    }
                }
            } catch (e: Exception) {
                Log.e("!!!!", "Ошибка получения адреса: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка получения адреса: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    addressTextView.text = "Ошибка получения адреса"
                }
            }
        }
    }


    private fun loadUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usersResult = SB.getSb().postgrest["users"].select().decodeList<User>()
                withContext(Dispatchers.Main) {
                    usersList.clear()
                    usersList.addAll(usersResult)
                    mainAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки пользователей: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Ошибка загрузки пользователей", e)
                }
            }
        }
    }



    private fun loadRoomTypes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val roomTypesResult = SB.getSb().postgrest["room_types"].select().decodeList<RoomType>()
                withContext(Dispatchers.Main) {
                    roomTypesMap.clear()
                    roomTypesResult.forEach { roomTypesMap[it.id] = it }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки типов комнат: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Ошибка загрузки типов комнат", e)
                }
            }
        }
    }

    private fun loadDeviceTypes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceTypesResult = SB.getSb().postgrest["device_types"].select().decodeList<DeviceType>()
                withContext(Dispatchers.Main) {
                    deviceTypesMap.clear()
                    deviceTypesResult.forEach { deviceTypesMap[it.id] = it }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки типов устройств: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Ошибка загрузки типов устройств", e)
                }
            }
        }
    }

    private fun loadUserAddress() {
        val address = sharedPreferences.getString("userAddress", "")
        val addressTextView = findViewById<TextView>(R.id.addressTextView)
        addressTextView.text = address
    }

    private fun updateCategory(selectedCategory: TextView, items: List<Any>) {
        // Обновление выбранной категории и отображение соответствующих элементов
        mainAdapter.updateList(items)
    }

    fun onSettingsClick(view: View) {
        // Заглушка для перехода в настройки
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    fun onCategoryClick(view: View) {
        // Обработка клика по категории
        when (view.id) {
            R.id.roomsCategory -> updateCategory(view as TextView, roomsList)
            R.id.devicesCategory -> updateCategory(view as TextView, devicesList)
            R.id.usersCategory -> updateCategory(view as TextView, usersList)
        }
    }

    fun onAddDeviceClick(view: View) {
        when (selectedCategory) {
            R.id.roomsCategory -> startActivity(Intent(this, AddRoomActivity::class.java))
            R.id.devicesCategory -> startActivity(Intent(this, AddDeviceActivity::class.java))
            R.id.usersCategory -> startActivity(Intent(this, AddUserActivity::class.java))
        }
    }
}