package com.pookie.game.presentation.ui.dialogs

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.pookie.game.data.api.GoogleAppsScriptApi
import com.pookie.game.di.ServiceLocator
import com.pookie.game.domain.model.GameType
import com.pookie.game.presentation.ui.BackendSetupActivity
import com.pookie.game.presentation.ui.GameArenaActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DebugMenuDialog {

    private val gson = Gson()

    fun show(activity: Activity) {
        val mockStatus = if (ServiceLocator.isMockMode) "[ACTIVE: Mock Local Mode]" else "[ACTIVE: Google Apps Script]"
        val options = arrayOf(
            "Solo Play / Test: Tic-Tac-Toe",
            "Solo Play / Test: Dots & Boxes",
            "Solo Play / Test: Couples Quiz",
            "Toggle Backend Mode: $mockStatus",
            "Load Demo Backend Key",
            "Inspect Current Config & Deployment ID",
            "Test 401 Error Diagnostic Dialog",
            "Reset App Preferences (Fresh Start)"
        )

        AlertDialog.Builder(activity)
            .setTitle("Pookie Developer Debug Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchSoloTestGame(activity, GameType.TIC_TAC_TOE)
                    1 -> {
                        val variants = arrayOf(
                            "Small (3x3 Grid - 9 Boxes)",
                            "Medium (4x4 Grid - 16 Boxes)",
                            "High (5x5 Grid - 25 Boxes)"
                        )
                        AlertDialog.Builder(activity)
                            .setTitle("Solo Test: Dots and Boxes Size")
                            .setItems(variants) { _, vWhich ->
                                val gridSize = when (vWhich) {
                                    0 -> 3
                                    1 -> 4
                                    2 -> 5
                                    else -> 3
                                }
                                launchSoloTestGame(activity, GameType.DOTS_AND_BOXES, gridSize)
                            }
                            .show()
                    }
                    2 -> launchSoloTestGame(activity, GameType.COUPLES_QUIZ)
                    3 -> toggleBackendMode(activity)
                    4 -> {
                        val demoKey = com.pookie.game.presentation.common.DebugAuthSecurity.getDemoDeploymentId()
                        ServiceLocator.preferencesRepository.saveBackendDeploymentId(demoKey)
                        ServiceLocator.isMockMode = false
                        Toast.makeText(activity, "Demo backend key applied.", Toast.LENGTH_SHORT).show()
                    }
                    5 -> inspectConfig(activity)
                    6 -> show401Diagnostic(activity)
                    7 -> resetPreferences(activity)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun launchSoloTestGame(activity: Activity, gameType: GameType, gridSize: Int = 3) {
        CoroutineScope(Dispatchers.Main).launch {
            ServiceLocator.isMockMode = true
            Toast.makeText(activity, "Starting Solo Test: ${gameType.displayName}...", Toast.LENGTH_SHORT).show()

            val hostId = "P_HostTest"
            val partnerId = "P_PartnerTest"

            val createResult = ServiceLocator.mockGameRepository.createRoom(hostId, "Host (You)", gameType, gridSize)
            if (createResult.isSuccess) {
                val created = createResult.getOrThrow()
                val joinResult = ServiceLocator.mockGameRepository.joinRoom(created.roomCode, partnerId, "Partner (Simulated)")
                val fullRoom = joinResult.getOrDefault(created)

                val intent = Intent(activity, GameArenaActivity::class.java).apply {
                    putExtra("ROOM_JSON", gson.toJson(fullRoom))
                    putExtra("IS_SOLO_TEST_MODE", true)
                }
                activity.startActivity(intent)
            } else {
                Toast.makeText(activity, "Failed to create mock room", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleBackendMode(activity: Activity) {
        ServiceLocator.isMockMode = !ServiceLocator.isMockMode
        val mode = if (ServiceLocator.isMockMode) "Mock In-Memory Mode" else "Live Google Apps Script Mode"
        Toast.makeText(activity, "Switched to: $mode", Toast.LENGTH_LONG).show()
    }

    private fun inspectConfig(activity: Activity) {
        val prefs = ServiceLocator.preferencesRepository
        val rawId = prefs.getBackendDeploymentId() ?: "Not configured"
        val profile = prefs.getCoupleProfile()

        val details = """
            Backend Deployment ID:
            $rawId
            
            Sanitized API URL:
            ${if (rawId.isNotBlank()) GoogleAppsScriptApi.sanitizeEndpoint(rawId) else "N/A"}
            
            My Player ID: ${profile.myPlayerId}
            My Nickname: ${profile.myNickname}
            Partner Nickname: ${profile.partnerNickname}
            Couple Vibe: ${profile.vibeTag}
            Sound Effects: ${if (profile.soundEnabled) "ON" else "OFF"}
            Mock Mode: ${ServiceLocator.isMockMode}
        """.trimIndent()

        AlertDialog.Builder(activity)
            .setTitle("Configuration Inspector")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun show401Diagnostic(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Google Apps Script 401 Diagnostic")
            .setMessage(
                "Why 401 Unauthorized happens:\n\n" +
                "In Google Apps Script, when deploying a Web App:\n" +
                "1. If 'Who has access' is set to 'Only myself' or 'Anyone with Google account', Google redirects the mobile request to accounts.google.com/ServiceLogin and blocks it.\n\n" +
                "How to fix in 30 seconds:\n" +
                "1. Open your Google Sheet > Extensions > Apps Script.\n" +
                "2. Click Deploy (top right) > Manage deployments.\n" +
                "3. Click the Edit (pencil) icon.\n" +
                "4. Change 'Who has access' to 'Anyone'.\n" +
                "5. Click Deploy and re-test in the app!"
            )
            .setPositiveButton("Got It", null)
            .show()
    }

    private fun resetPreferences(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Reset App Preferences?")
            .setMessage("This will clear saved backend ID and couple nicknames.")
            .setPositiveButton("Reset") { _, _ ->
                val prefs = activity.getSharedPreferences("pookie_game_prefs", Activity.MODE_PRIVATE)
                prefs.edit().clear().apply()
                ServiceLocator.isMockMode = false
                Toast.makeText(activity, "Preferences reset. Re-open setup screen.", Toast.LENGTH_SHORT).show()
                activity.startActivity(Intent(activity, BackendSetupActivity::class.java))
                activity.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
