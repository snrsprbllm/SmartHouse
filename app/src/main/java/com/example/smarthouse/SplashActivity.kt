package com.example.smarthouse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Находим ImageView в разметке
        val splashImageView: ImageView = findViewById(R.id.splashRoundImageView)

        // Запуск анимации вращения
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
        splashImageView.startAnimation(rotateAnimation)

        // Задержка перед переходом на следующий экран
        Handler().postDelayed({
            // Проверка на наличие пин-кода и регистрации
            val sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)
            val isRegistered = sharedPreferences.getBoolean("isRegistered", false)
            val savedPinCode = sharedPreferences.getString("pin_code", null)

            if (isRegistered && savedPinCode != null) {
                // Если пользователь зарегистрирован и есть пин-код, переходим на PinCodeActivity
                val intent = Intent(this, PinCodeActivity::class.java)
                startActivity(intent)
            } else if (isRegistered) {
                // Если пользователь зарегистрирован, но нет пин-кода, переходим на PinCodeActivity
                val intent = Intent(this, PinCodeActivity::class.java)
                startActivity(intent)
            } else {
                // Иначе переходим на RegistrationActivity
                val intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 4000) // 4000 миллисекунд = 4 секунды
    }
}