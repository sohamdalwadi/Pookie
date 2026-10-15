package com.pookie.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.game.domain.repository.IPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object BackendSetup : SplashDestination()
    object ProfileSetup : SplashDestination()
    object Lobby : SplashDestination()
}

class SplashViewModel(
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashDestination>()
    val navigationEvent: SharedFlow<SplashDestination> = _navigationEvent.asSharedFlow()

    fun checkInitialRoute() {
        viewModelScope.launch {
            val backendId = preferencesRepository.getBackendDeploymentId()
            val backendUrl = preferencesRepository.getBackendUrl()

            if (backendId.isNullOrBlank() && backendUrl.isNullOrBlank()) {
                _navigationEvent.emit(SplashDestination.BackendSetup)
                return@launch
            }

            val profile = preferencesRepository.getCoupleProfile()
            if (profile.myNickname.isBlank()) {
                _navigationEvent.emit(SplashDestination.ProfileSetup)
            } else {
                _navigationEvent.emit(SplashDestination.Lobby)
            }
        }
    }
}
