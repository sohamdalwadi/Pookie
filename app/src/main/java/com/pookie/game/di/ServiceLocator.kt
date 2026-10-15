package com.pookie.game.di

import android.content.Context
import com.pookie.game.data.api.GoogleAppsScriptApi
import com.pookie.game.data.repository.GameRepositoryImpl
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

    val otaRepository: IOtaRepository by lazy {
        OtaRepositoryImpl()
    }

    // Use cases
    val createRoomUseCase: CreateRoomUseCase by lazy { CreateRoomUseCase(gameRepository) }
    val joinRoomUseCase: JoinRoomUseCase by lazy { JoinRoomUseCase(gameRepository) }
    val getGameStateUseCase: GetGameStateUseCase by lazy { GetGameStateUseCase(gameRepository) }
    val makeMoveUseCase: MakeMoveUseCase by lazy { MakeMoveUseCase(gameRepository) }
    val sendReactionUseCase: SendReactionUseCase by lazy { SendReactionUseCase(gameRepository) }
    val requestRematchUseCase: RequestRematchUseCase by lazy { RequestRematchUseCase(gameRepository) }
    val checkOtaUpdateUseCase: CheckOtaUpdateUseCase by lazy { CheckOtaUpdateUseCase(otaRepository) }
}
