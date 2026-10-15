package com.pookie.game.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pookie.game.R
import com.pookie.game.di.ServiceLocator
import com.pookie.game.presentation.common.Resource
import com.pookie.game.presentation.viewmodel.SetupViewModel
import kotlinx.coroutines.launch

class BackendSetupActivity : AppCompatActivity() {

    private val viewModel by lazy {
        SetupViewModel(ServiceLocator.preferencesRepository, ServiceLocator.gameRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backend_setup)

        val editDeploymentId = findViewById<EditText>(R.id.editDeploymentId)
        val btnTestAndSave = findViewById<Button>(R.id.btnTestAndSave)
        val btnHowToSetup = findViewById<Button>(R.id.btnHowToSetup)
        val progressLoading = findViewById<ProgressBar>(R.id.progressLoading)
        val textStatusMessage = findViewById<TextView>(R.id.textStatusMessage)

        // Pre-fill existing if any
        val existingId = ServiceLocator.preferencesRepository.getBackendDeploymentId()
            ?: ServiceLocator.preferencesRepository.getBackendUrl()
        if (!existingId.isNullOrBlank()) {
            editDeploymentId.setText(existingId)
        }

        btnTestAndSave.setOnClickListener {
            val input = editDeploymentId.text.toString()
            viewModel.testAndSaveBackend(input)
        }

        btnHowToSetup.setOnClickListener {
            showHowToSetupDialog()
        }

        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                when (state) {
                    is Resource.Idle -> {
                        progressLoading.visibility = View.GONE
                        btnTestAndSave.isEnabled = true
                    }
                    is Resource.Loading -> {
                        progressLoading.visibility = View.VISIBLE
                        btnTestAndSave.isEnabled = false
                        textStatusMessage.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        progressLoading.visibility = View.GONE
                        btnTestAndSave.isEnabled = true
                        Toast.makeText(this@BackendSetupActivity, R.string.backend_connected_success, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@BackendSetupActivity, ProfileSetupActivity::class.java))
                        finish()
                    }
                    is Resource.Error -> {
                        progressLoading.visibility = View.GONE
                        btnTestAndSave.isEnabled = true
                        textStatusMessage.visibility = View.VISIBLE
                        textStatusMessage.text = state.message
                    }
                }
            }
        }
    }

    private fun showHowToSetupDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.backend_setup_title)
            .setMessage(
                "How to get your Deployment ID:\n\n" +
                "1. Open your Google Sheet in a browser.\n" +
                "2. Click Extensions > Apps Script.\n" +
                "3. In the top right, click Deploy > Manage deployments.\n" +
                "4. Copy the 'Deployment ID' (e.g. AKfycbx...).\n" +
                "5. Paste it here to link your backend!"
            )
            .setPositiveButton("Got It", null)
            .show()
    }
}
