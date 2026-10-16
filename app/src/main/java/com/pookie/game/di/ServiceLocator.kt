package com.pookie.game.di

import android.content.Context
import com.pookie.game.data.api.GoogleAppsScriptApi
import com.pookie.game.data.repository.GameRepositoryImpl
import com.pookie.game.data.repository.MockGameRepository
import com.pookie.game.data.repository.OtaRepositoryImpl
import com.pookie.game.data.repository.PreferencesRepositoryImpl
import com.pookie.game.domain.repository.IGameRepository
import com.pookie.game.domain.repository.IOtaRepository
import com.pookie.game.domain.repository.IPreferencesRepository
import com.pookie.game.domain.usecase.CheckOtaUpdateUseCase
import com.pookie.game.domain.usecase.CreateRoomUseCase
import com.pookie.game.domain.usecase.GetGameStateUseCase
import com.pookie.game.domain.usecase.JoinRoomUseCase
import com.pookie.game.domain.usecase.MakeMoveUseCase
import com.pookie.game.domain.usecase.RequestRematchUseCase
import com.pookie.game.domain.usecase.SendReactionUseCase

object ServiceLocator {

    @Volatile
    private var appContext: Context? = null

    var isMockMode: Boolean = false
    var isDebugUnlockedInSession: Boolean = false

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun requireContext(): Context =
        appContext ?: throw IllegalStateException("ServiceLocator has not been initialized. Call initialize(context) in Application class.")

    val preferencesRepository: IPreferencesRepository by lazy {
        PreferencesRepositoryImpl(requireContext())
    }

    val googleAppsScriptApi: GoogleAppsScriptApi by lazy {
        GoogleAppsScriptApi(
            baseUrlProvider = {
                preferencesRepository.getBackendUrl() ?: preferencesRepository.getBackendDeploymentId()
            }
        )
    }

    val gameRepository: IGameRepository by lazy {
        GameRepositoryImpl(googleAppsScriptApi)
    }

    val mockGameRepository: IGameRepository by lazy {
        MockGameRepository()
    }

    val activeGameRepository: IGameRepository
        get() = if (isMockMode) mockGameRepository else gameRepository

    val otaRepository: IOtaRepository by lazy {
        OtaRepositoryImpl()
    }

    // Dynamic delegate Use Cases that use activeGameRepository
    val createRoomUseCase: CreateRoomUseCase
        get() = CreateRoomUseCase(activeGameRepository)

    val joinRoomUseCase: JoinRoomUseCase
        get() = JoinRoomUseCase(activeGameRepository)

    val getGameStateUseCase: GetGameStateUseCase
        get() = GetGameStateUseCase(activeGameRepository)

    val makeMoveUseCase: MakeMoveUseCase
        get() = MakeMoveUseCase(activeGameRepository)

    val sendReactionUseCase: SendReactionUseCase
        get() = SendReactionUseCase(activeGameRepository)

    val requestRematchUseCase: RequestRematchUseCase
        get() = RequestRematchUseCase(activeGameRepository)

    val submitVerdictUseCase: com.pookie.game.domain.usecase.SubmitVerdictUseCase
        get() = com.pookie.game.domain.usecase.SubmitVerdictUseCase(activeGameRepository)

    val checkOtaUpdateUseCase: CheckOtaUpdateUseCase by lazy {
        CheckOtaUpdateUseCase(otaRepository)
    }
}
