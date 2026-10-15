package com.pookie.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.game.domain.model.CoupleProfile
import com.pookie.game.domain.repository.IPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProfileViewModel(
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {

    private val cuteNicknames = listOf(
        "Pookie", "HoneyBun", "CuddleBug", "Starlight", "Moonbeam",
        "Buttercup", "Marshmallow", "Sweetpea", "Dumpling", "Sunbeam",
        "Boba", "Cupcake", "SnugglePuff", "Cookie", "Pancake"
    )

    private val _profileState = MutableStateFlow(preferencesRepository.getCoupleProfile())
    val profileState: StateFlow<CoupleProfile> = _profileState.asStateFlow()

    fun rollRandomNickname(): String {
        val randomName = cuteNicknames[Random.nextInt(cuteNicknames.size)]
        return randomName
    }

    fun updateProfile(myNickname: String, partnerNickname: String, vibeTag: String) {
        val updated = _profileState.value.copy(
            myNickname = myNickname.trim(),
            partnerNickname = partnerNickname.trim(),
            vibeTag = vibeTag
        )
        preferencesRepository.saveCoupleProfile(updated)
        _profileState.value = updated
    }
}
