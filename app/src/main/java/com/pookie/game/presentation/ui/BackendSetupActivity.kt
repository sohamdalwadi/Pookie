package com.pookie.game.presentation.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pookie.game.R
import com.pookie.game.di.ServiceLocator
import com.pookie.game.presentation.common.QrCodeGenerator
import com.pookie.game.presentation.common.Resource
import com.pookie.game.presentation.ui.dialogs.DebugMenuDialog
import com.pookie.game.presentation.viewmodel.SetupViewModel
import kotlinx.coroutines.launch

class BackendSetupActivity : AppCompatActivity() {

    private val viewModel by lazy {
        SetupViewModel(ServiceLocator.preferencesRepository, ServiceLocator.gameRepository)
    }

    private lateinit var editDeploymentId: EditText
    private lateinit var btnTestAndSave: Button
    private lateinit var btnHowToSetup: Button
    private lateinit var btnShareBackendQr: Button
    private lateinit var btnOpenDevDebug: Button
    private lateinit var progressLoading: ProgressBar
    private lateinit var textStatusMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backend_setup)

        editDeploymentId = findViewById(R.id.editDeploymentId)
        btnTestAndSave = findViewById(R.id.btnTestAndSave)
        btnHowToSetup = findViewById(R.id.btnHowToSetup)
        btnShareBackendQr = findViewById(R.id.btnShareBackendQr)
        btnOpenDevDebug = findViewById(R.id.btnOpenDevDebug)
        progressLoading = findViewById(R.id.progressLoading)
        textStatusMessage = findViewById(R.id.textStatusMessage)

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

        btnShareBackendQr.setOnClickListener {
            val currentId = editDeploymentId.text.toString().trim().ifBlank {
                ServiceLocator.preferencesRepository.getBackendDeploymentId() ?: ""
            }
            if (currentId.isBlank()) {
                Toast.makeText(this, "Please enter your Deployment ID first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showShareBackendDialog(currentId)
        }

        btnOpenDevDebug.setOnClickListener {
            DebugMenuDialog.show(this)
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

    override fun onResume() {
        super.onResume()
        checkClipboardForBackendId()
    }

    private fun checkClipboardForBackendId() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: return

            if ((clip.startsWith("AKfycb") || clip.contains("macros/s/")) && editDeploymentId.text.isNullOrBlank()) {
                val cleanId = clip.substringAfter("AKfycb").let { "AKfycb$it" }.substringBefore("&").substringBefore("/").substringBefore(" ")
                editDeploymentId.setText(cleanId)
                Toast.makeText(this, "Pasted Deployment ID from clipboard!", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {}
    }

    private fun showShareBackendDialog(deploymentId: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null)
        val textQrRoomCode = dialogView.findViewById<TextView>(R.id.textQrRoomCode)
        val imgQrCode = dialogView.findViewById<ImageView>(R.id.imgQrCode)
        val btnCloseQr = dialogView.findViewById<Button>(R.id.btnCloseQr)

        textQrRoomCode.text = "Partner Backend Setup"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        lifecycleScope.launch {
            val payload = "pookie://backend?id=$deploymentId"
            val bitmap = QrCodeGenerator.generateQrBitmap(payload)
            imgQrCode.setImageBitmap(bitmap)
        }

        btnCloseQr.text = "Copy Share Text"
        btnCloseQr.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Pookie Backend ID", deploymentId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Deployment ID copied! Send this to your partner.", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showHowToSetupDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.backend_setup_title)
            .setMessage(
                "How to get your Deployment ID:\n\n" +
                "1. Open your Google Sheet in a browser.\n" +
                "2. Click Extensions > Apps Script.\n" +
                "3. In the top right, click Deploy > Manage deployments.\n" +
                "4. Copy the 'Deployment ID' (starts with AKfycb...).\n\n" +
                "⚠️ CRITICAL FOR 401 ERROR:\n" +
                "Click the Edit (pencil) icon next to your deployment.\n" +
                "Ensure 'Who has access' is set to 'Anyone' (NOT 'Only myself').\n\n" +
                "5. Paste it here and tap Test & Save!"
            )
            .setPositiveButton("Got It", null)
            .show()
    }
}
