package com.pookie.game.presentation.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.pookie.game.R
import com.pookie.game.di.ServiceLocator
import com.pookie.game.presentation.viewmodel.ProfileViewModel

class ProfileSetupActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ProfileViewModel(ServiceLocator.preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        val editMyNickname = findViewById<EditText>(R.id.editMyNickname)
        val btnDiceNickname = findViewById<ImageView>(R.id.btnDiceNickname)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

        val initialProfile = viewModel.profileState.value
        if (initialProfile.myNickname.isNotBlank()) {
            editMyNickname.setText(initialProfile.myNickname)
        }

        btnDiceNickname.setOnClickListener {
            // Dice rotation animation
            ObjectAnimator.ofFloat(btnDiceNickname, "rotation", 0f, 360f).apply {
                duration = 350
                start()
            }
            editMyNickname.setText(viewModel.rollRandomNickname())
        }

        btnSaveProfile.setOnClickListener {
            val myName = editMyNickname.text.toString().trim()

            if (myName.isBlank()) {
                Toast.makeText(this, "Please enter your nickname or roll the dice!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateProfile(
                myNickname = myName,
                partnerNickname = initialProfile.partnerNickname,
                vibeTag = initialProfile.vibeTag
            )
            startActivity(Intent(this, LobbyActivity::class.java))
            finish()
        }
    }
}
