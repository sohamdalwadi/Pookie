package com.pookie.game.presentation.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.pookie.game.di.ServiceLocator

/**
 * BroadcastReceiver to configure backend settings directly via ADB.
 * Example ADB usage:
 * adb shell am broadcast -a com.pookie.SET_BACKEND_ID --es id "<DEPLOYMENT_ID>"
 */
class BackendConfigReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_SET_BACKEND_ID) {
            val deploymentId = intent.getStringExtra(EXTRA_ID)?.trim()
            val mockMode = intent.getBooleanExtra(EXTRA_MOCK, false)

            if (!deploymentId.isNullOrBlank()) {
                val cleanId = if (deploymentId.startsWith("http")) {
                    deploymentId.substringAfter("macros/s/").substringBefore("/exec").substringBefore("?")
                } else {
                    deploymentId
                }
                ServiceLocator.preferencesRepository.saveBackendDeploymentId(cleanId)
                Toast.makeText(context, "ADB: Backend ID saved: $cleanId", Toast.LENGTH_LONG).show()
            }

            if (intent.hasExtra(EXTRA_MOCK)) {
                ServiceLocator.isMockMode = mockMode
                Toast.makeText(context, "ADB: Mock mode set to: $mockMode", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val ACTION_SET_BACKEND_ID = "com.pookie.SET_BACKEND_ID"
        const val EXTRA_ID = "id"
        const val EXTRA_MOCK = "mock"
    }
}
