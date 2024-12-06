package com.example.smarthouse

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.util.regex.Pattern

class AddressInputActivity : AppCompatActivity() {

    private lateinit var addressLayout: TextInputLayout
    private lateinit var saveButton: Button
/*
    private val supabaseUrl = "https://afgslxlqfpbpctunvood.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFmZ3NseGxxZnBicGN0dW52b29kIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzMxMjQzMzMsImV4cCI6MjA0ODcwMDMzM30.FctIeU0yl4iOYEmdi-Snx5h41-IC6IRQc_7KY_KJulw"

    private val supabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }*/

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

    private fun saveAddress(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получаем текущего пользователя
                val user = SB.getSb().auth.retrieveUserForCurrentSession(updateSession = true)
                    ?: throw Exception("Пользователь не авторизован")

                // Обновляем адрес пользователя в базе данных
                val response = SB.getSb().postgrest["users"]
                    .update({
                        set("address", address)
                    }) {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    // If no error, handle success
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddressInputActivity, "Адрес успешно сохранен", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AddressInputActivity, MainActivity::class.java))

                }

            } catch (e: Exception) {
                // Handle any unexpected errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddressInputActivity, "Ошибка сохранения адреса: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        //Доделать флаги
    }
}