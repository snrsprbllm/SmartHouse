package com.example.smarthouse

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
            // Переход на RegistrationActivity
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }, 4000) // 4000 миллисекунд = 4 секунды
    }
}


//ЕСЛИ ЗАПУСК НЕ ПЕРВЫЙ, ТО НУЖНО ДОДЕЛАТЬ ЧТОБЫ КИДАЛО НА ВВОД ПИНКОДА