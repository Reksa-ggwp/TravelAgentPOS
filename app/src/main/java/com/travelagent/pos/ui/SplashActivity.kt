package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.travelagent.pos.MainActivity
import com.travelagent.pos.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before setContentView
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start animations
        startAnimations()

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, splashDuration)
    }

    private fun startAnimations() {
        // Logo fade in and scale
        binding.ivLogo.alpha = 0f
        binding.ivLogo.scaleX = 0.5f
        binding.ivLogo.scaleY = 0.5f

        binding.ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // App name slide up
        binding.tvAppName.translationY = 100f
        binding.tvAppName.alpha = 0f

        binding.tvAppName.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Tagline fade in
        binding.tvTagline.alpha = 0f

        binding.tvTagline.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(600)
            .start()

        // Progress bar animate
        binding.progressBar.alpha = 0f

        binding.progressBar.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(900)
            .start()
    }
}