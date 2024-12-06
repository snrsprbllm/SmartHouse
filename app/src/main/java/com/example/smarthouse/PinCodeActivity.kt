package com.example.smarthouse

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PinCodeActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PIN_CODE_KEY = "pin_code"
    private val PIN_CODE_LENGTH = 4
    private var pinCode = ""

    private lateinit var titleTextView: TextView
    private lateinit var indicator1: ImageView
    private lateinit var indicator2: ImageView
    private lateinit var indicator3: ImageView
    private lateinit var indicator4: ImageView
    private lateinit var exitButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_code)

        sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)

        titleTextView = findViewById(R.id.titleTextView)
        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        indicator3 = findViewById(R.id.indicator3)
        indicator4 = findViewById(R.id.indicator4)
        exitButton = findViewById(R.id.exitButton)

        val savedPinCode = sharedPreferences.getString(PIN_CODE_KEY, null)
        if (savedPinCode != null) {
            // Если пин-код уже есть, обновляем надпись и вид кнопки
            titleTextView.text = "Умный дом"
            exitButton.visibility = MaterialButton.VISIBLE
        } else {
            titleTextView.text = "Создайте пин-код"
            exitButton.visibility = MaterialButton.GONE
        }

        val buttons = listOf(
            findViewById<MaterialButton>(R.id.button1),
            findViewById<MaterialButton>(R.id.button2),
            findViewById<MaterialButton>(R.id.button3),
            findViewById<MaterialButton>(R.id.button4),
            findViewById<MaterialButton>(R.id.button5),
            findViewById<MaterialButton>(R.id.button6),
            findViewById<MaterialButton>(R.id.button7),
            findViewById<MaterialButton>(R.id.button8),
            findViewById<MaterialButton>(R.id.button9)
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                onNumberButtonClick(button.text.toString())
            }
        }

        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun onNumberButtonClick(number: String) {
        if (pinCode.length < PIN_CODE_LENGTH) {
            pinCode += number
            updateIndicators()

            if (pinCode.length == PIN_CODE_LENGTH) {
                if (sharedPreferences.getString(PIN_CODE_KEY, null) == null) {
                    savePinCode()
                } else {
                    checkPinCode()
                }
            }
        }
    }

    private fun updateIndicators() {
        val indicators = listOf(indicator1, indicator2, indicator3, indicator4)
        for (i in 0 until PIN_CODE_LENGTH) {
            indicators[i].setImageResource(
                if (i < pinCode.length) R.drawable.indicator_filled else R.drawable.indicator_empty
            )
        }
    }

    private fun savePinCode() {
        sharedPreferences.edit().putString(PIN_CODE_KEY, pinCode).apply()
        pinCode = ""
        updateIndicators()
        startActivity(Intent(this, AddressInputActivity::class.java))
        finish()
    }

    private fun checkPinCode() {
        if (pinCode == sharedPreferences.getString(PIN_CODE_KEY, "")) {
            // Переход на главный экран
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Ошибка ввода пин-кода
            Toast.makeText(this, "Неверный пин-код", Toast.LENGTH_SHORT).show()
            pinCode = ""
            updateIndicators()
        }
    }
}