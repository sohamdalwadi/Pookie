package com.pookie.game.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pookie.game.R
import com.pookie.game.di.ServiceLocator
import com.pookie.game.presentation.viewmodel.SplashDestination
import com.pookie.game.presentation.viewmodel.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val viewModel by lazy {
        SplashViewModel(ServiceLocator.preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            viewModel.navigationEvent.collect { destination ->
                delay(600) // Smooth entrance
                when (destination) {
                    SplashDestination.BackendSetup -> {
                        startActivity(Intent(this@SplashActivity, BackendSetupActivity::class.java))
                        finish()
                    }
                    SplashDestination.ProfileSetup -> {
                        startActivity(Intent(this@SplashActivity, ProfileSetupActivity::class.java))
                        finish()
                    }
                    SplashDestination.Lobby -> {
                        startActivity(Intent(this@SplashActivity, LobbyActivity::class.java))
                        finish()
                    }
                }
            }
        }

        viewModel.checkInitialRoute()
    }
}
