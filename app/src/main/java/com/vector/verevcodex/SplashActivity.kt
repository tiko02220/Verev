package com.vector.verevcodex

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.vector.verevcodex.presentation.app.VerevSplashScreen
import com.vector.verevcodex.presentation.theme.VerevMerchantTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            VerevMerchantTheme {
                VerevSplashScreen()
            }
        }
        lifecycleScope.launch {
            delay(SPLASH_DURATION_MS)
            startActivity(
                Intent(this@SplashActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
            )
            finish()
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 3_000L
    }
}
