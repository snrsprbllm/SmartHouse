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

        Handler().postDelayed({
            val sharedPreferences = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)
            val isRegistered = sharedPreferences.getBoolean("isRegistered", false)
            val hasPinCode = sharedPreferences.getBoolean("hasPinCode", false)
            val hasAddress = sharedPreferences.getBoolean("hasAddress", false)

            val intent = when {
                !isRegistered -> Intent(this, RegistrationActivity::class.java)
                !hasPinCode -> Intent(this, PinCodeActivity::class.java).apply {
                    putExtra("hasAddress", hasAddress)
                }
                !hasAddress -> Intent(this, AddressInputActivity::class.java)
                else -> Intent(this, PinCodeActivity::class.java).apply {
                    putExtra("hasAddress", hasAddress)
                }
            }
            startActivity(intent)
            finish()
        }, 4000) // 4000 миллисекунд = 4 секунды
    }
}