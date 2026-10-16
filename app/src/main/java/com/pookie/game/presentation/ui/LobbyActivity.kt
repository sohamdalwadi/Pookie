package com.pookie.game.presentation.ui

import android.content.Intent
import android.os.Bundle
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
import com.google.gson.Gson
import com.pookie.game.R
import com.pookie.game.di.ServiceLocator
import com.pookie.game.domain.model.GameType
import com.pookie.game.presentation.common.Resource
import com.pookie.game.presentation.viewmodel.LobbyViewModel
import kotlinx.coroutines.launch

class LobbyActivity : AppCompatActivity() {

    private val viewModel by lazy {
        LobbyViewModel(
            createRoomUseCase = ServiceLocator.createRoomUseCase,
            joinRoomUseCase = ServiceLocator.joinRoomUseCase,
            checkOtaUpdateUseCase = ServiceLocator.checkOtaUpdateUseCase,
            preferencesRepository = ServiceLocator.preferencesRepository
        )
    }

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        val textCoupleGreetings = findViewById<TextView>(R.id.textCoupleGreetings)
        val textCoupleVibe = findViewById<TextView>(R.id.textCoupleVibe)
        val btnSettings = findViewById<ImageView>(R.id.btnSettings)

        val cardGameTicTacToe = findViewById<View>(R.id.cardGameTicTacToe)
        val cardGameDotsBoxes = findViewById<View>(R.id.cardGameDotsBoxes)
        val cardGameQuiz = findViewById<View>(R.id.cardGameQuiz)

        val editRoomCode = findViewById<EditText>(R.id.editRoomCode)
        val btnJoinRoom = findViewById<Button>(R.id.btnJoinRoom)
        val progressLobby = findViewById<ProgressBar>(R.id.progressLobby)

        // Setup couple header
        val profile = viewModel.profile
        textCoupleGreetings.text = if (profile.partnerNickname.isNotBlank()) {
            "${profile.myNickname} & ${profile.partnerNickname}"
        } else {
            "Welcome, ${profile.myNickname}!"
        }
        textCoupleVibe.text = profile.vibeTag

        // Game Card Clicks
        cardGameTicTacToe.setOnClickListener {
            viewModel.createRoom(GameType.TIC_TAC_TOE)
        }
        cardGameDotsBoxes.setOnClickListener {
            viewModel.createRoom(GameType.DOTS_AND_BOXES)
        }
        cardGameQuiz.setOnClickListener {
            viewModel.createRoom(GameType.COUPLES_QUIZ)
        }

        // Join Room Click
        btnJoinRoom.setOnClickListener {
            val rawInput = editRoomCode.text.toString().trim()
            if (rawInput.isBlank()) {
                Toast.makeText(this, "Please enter room code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Extract backend ID if partner shared full payload
            val backendMatch = Regex("AKfycb[a-zA-Z0-9_-]+").find(rawInput)
            if (backendMatch != null) {
                val extractedBackendId = backendMatch.value
                ServiceLocator.preferencesRepository.saveBackendDeploymentId(extractedBackendId)
                Toast.makeText(this, "Partner Backend ID automatically configured!", Toast.LENGTH_SHORT).show()
            }

            // Extract and validate room code format (e.g. PK-1234 or PKABCD)
            val codeMatch = Regex("(PK-?[A-Za-z0-9]{4}|[A-Za-z0-9]{6})", RegexOption.IGNORE_CASE).find(rawInput)
            if (codeMatch == null) {
                AlertDialog.Builder(this)
                    .setTitle("Invalid Room Code")
                    .setMessage("Room codes must follow the format PK-XXXX (e.g., PK-1234 or PKABCD).")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            val cleanCode = codeMatch.value.replace("-", "").uppercase()
            viewModel.joinRoom(cleanCode)
        }

        btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        // Observe Room Actions (Create / Join)
        lifecycleScope.launch {
            viewModel.roomActionState.collect { state ->
                when (state) {
                    is Resource.Idle -> {
                        progressLobby.visibility = View.GONE
                        enableGameControls(true)
                    }
                    is Resource.Loading -> {
                        progressLobby.visibility = View.VISIBLE
                        enableGameControls(false)
                    }
                    is Resource.Success -> {
                        progressLobby.visibility = View.GONE
                        enableGameControls(true)
                        val room = state.data
                        viewModel.resetActionState()

                        val intent = Intent(this@LobbyActivity, GameArenaActivity::class.java).apply {
                            putExtra("ROOM_JSON", gson.toJson(room))
                        }
                        startActivity(intent)
                    }
                    is Resource.Error -> {
                        progressLobby.visibility = View.GONE
                        enableGameControls(true)
                        val errorMsg = state.message
                        val displayMsg = if (errorMsg.contains("ROOM_NOT_FOUND", ignoreCase = true) || errorMsg.contains("does not exist", ignoreCase = true)) {
                            "Room was not found on your backend. Please make sure your partner has already created the room and the code is correct."
                        } else if (errorMsg.contains("ROOM_FULL", ignoreCase = true) || errorMsg.contains("two active players", ignoreCase = true)) {
                            "This room is already full with two active players."
                        } else {
                            errorMsg
                        }
                        AlertDialog.Builder(this@LobbyActivity)
                            .setTitle("Could Not Join Room")
                            .setMessage(displayMsg)
                            .setPositiveButton("OK", null)
                            .show()
                        viewModel.resetActionState()
                    }
                }
            }
        }

        // Check for OTA update on launch
        viewModel.checkForOtaUpdates("1.0.0")
    }

    private fun enableGameControls(enabled: Boolean) {
        findViewById<View>(R.id.cardGameTicTacToe).isEnabled = enabled
        findViewById<View>(R.id.cardGameDotsBoxes).isEnabled = enabled
        findViewById<View>(R.id.cardGameQuiz).isEnabled = enabled
        findViewById<Button>(R.id.btnJoinRoom).isEnabled = enabled
    }

    private fun showSettingsDialog() {
        val soundEnabled = ServiceLocator.preferencesRepository.isSoundEnabled()
        val optionsList = mutableListOf(
            if (soundEnabled) "Disable Sound Effects" else "Enable Sound Effects",
            "Reconfigure Backend Deployment ID",
            "Edit Your Nickname",
            "Check for GitHub OTA Updates"
        )
        if (ServiceLocator.isDebugUnlockedInSession) {
            optionsList.add("Developer Debug Menu")
        }
        val options = optionsList.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_title)
            .setItems(options) { _, which ->
                when (options[which]) {
                    "Disable Sound Effects", "Enable Sound Effects" -> {
                        ServiceLocator.preferencesRepository.setSoundEnabled(!soundEnabled)
                        Toast.makeText(this, "Sound updated", Toast.LENGTH_SHORT).show()
                    }
                    "Reconfigure Backend Deployment ID" -> {
                        startActivity(Intent(this, BackendSetupActivity::class.java))
                    }
                    "Edit Your Nickname" -> {
                        startActivity(Intent(this, ProfileSetupActivity::class.java))
                    }
                    "Check for GitHub OTA Updates" -> {
                        viewModel.checkForOtaUpdates("1.0.0")
                        Toast.makeText(this, "Checking GitHub releases...", Toast.LENGTH_SHORT).show()
                    }
                    "Developer Debug Menu" -> {
                        com.pookie.game.presentation.ui.dialogs.DebugMenuDialog.show(this)
                    }
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
