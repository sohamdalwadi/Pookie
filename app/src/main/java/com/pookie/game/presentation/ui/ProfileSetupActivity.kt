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
        val editPartnerNickname = findViewById<EditText>(R.id.editPartnerNickname)
        val btnDiceNickname = findViewById<ImageView>(R.id.btnDiceNickname)
        val chipGroupVibes = findViewById<ChipGroup>(R.id.chipGroupVibes)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

        val initialProfile = viewModel.profileState.value
        if (initialProfile.myNickname.isNotBlank()) {
            editMyNickname.setText(initialProfile.myNickname)
        }
        if (initialProfile.partnerNickname.isNotBlank()) {
            editPartnerNickname.setText(initialProfile.partnerNickname)
        }

        // Set default vibe chip
        val defaultChip = when (initialProfile.vibeTag) {
            getString(R.string.vibe_binge) -> R.id.chipBinge
            getString(R.string.vibe_travelers) -> R.id.chipTravelers
            getString(R.string.vibe_gamers) -> R.id.chipGamers
            getString(R.string.vibe_coffee) -> R.id.chipCoffee
            else -> R.id.chipFoodies
        }
        chipGroupVibes.check(defaultChip)

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
            val partnerName = editPartnerNickname.text.toString().trim()

            if (myName.isBlank()) {
                Toast.makeText(this, "Please enter your nickname or roll the dice!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedChipId = chipGroupVibes.checkedChipId
            val selectedVibe = if (selectedChipId != -1) {
                findViewById<Chip>(selectedChipId).text.toString()
            } else {
                getString(R.string.vibe_foodies)
            }

            viewModel.updateProfile(myName, partnerName, selectedVibe)
            startActivity(Intent(this, LobbyActivity::class.java))
            finish()
        }
    }
}
