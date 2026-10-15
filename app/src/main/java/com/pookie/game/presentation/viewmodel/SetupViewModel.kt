package com.pookie.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.game.domain.repository.IGameRepository
import com.pookie.game.domain.repository.IPreferencesRepository
import com.pookie.game.presentation.common.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupViewModel(
    private val preferencesRepository: IPreferencesRepository,
    private val gameRepository: IGameRepository
) : ViewModel() {

    private val _connectionState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val connectionState: StateFlow<Resource<Unit>> = _connectionState.asStateFlow()

    fun testAndSaveBackend(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            _connectionState.value = Resource.Error("Please enter a valid Deployment ID or Web App URL.")
            return
        }

        viewModelScope.launch {
            _connectionState.value = Resource.Loading

            // Save temporarily to test
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                preferencesRepository.saveBackendUrl(trimmed)
            } else {
                preferencesRepository.saveBackendDeploymentId(trimmed)
            }

            val result = gameRepository.checkHealth()
            if (result.isSuccess) {
                _connectionState.value = Resource.Success(Unit)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Connection failed"
                _connectionState.value = Resource.Error(errorMsg)
            }
        }
    }
}
