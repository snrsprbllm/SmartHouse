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

        val splashImageView: ImageView = findViewById(R.id.splashRoundImageView)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
        splashImageView.startAnimation(rotateAnimation)

        // Задержка для отображения SplashScreen
        Handler().postDelayed({
            // Проверка состояния пользователя
            val sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)
            val isRegistered = sharedPreferences.getBoolean("isRegistered", false)
            val hasPinCode = sharedPreferences.getBoolean("hasPinCode", false)
            val hasAddress = sharedPreferences.getBoolean("hasAddress", false)

            // Определение следующего экрана
            val intent = when {
                !isRegistered -> Intent(this, RegistrationActivity::class.java)
                !hasPinCode -> Intent(this, PinCodeActivity::class.java).apply {
                    putExtra("hasAddress", hasAddress)
                }
                !hasAddress -> Intent(this, AddressInputActivity::class.java)
                else -> Intent(this, PinCodeActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 4000) // 2000 миллисекунд = 2 секунды
    }
}