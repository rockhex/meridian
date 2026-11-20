package com.meridian.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.meridian.app.di.AppContainer
import com.meridian.app.presentation.home.HomeViewModel
import com.meridian.app.presentation.login.LoginViewModel
import com.meridian.app.presentation.office.OfficeViewModel
import com.meridian.app.presentation.profile.ProfileViewModel
import com.meridian.app.presentation.team.TeamViewModel

/** DI manual: arma cada ViewModel pasando repositorios y casos de uso por constructor. */
class ViewModelFactory(private val c: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(c.signInWithGoogle, c.googleIdTokenProvider, c.connectivity)
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(
                c.authRepository, c.preferencesRepository, c.clockRepository, c.teamRepository, c.connectivity,
                c.registerClockEvent, c.undoClockEvent, c.computeAvailability, c.clock,
            )
        modelClass.isAssignableFrom(TeamViewModel::class.java) ->
            TeamViewModel(
                c.authRepository, c.preferencesRepository, c.teamRepository, c.connectivity,
                c.syncRepository, c.syncTrigger, c.observeOwnAvailability, c.clock,
            )
        modelClass.isAssignableFrom(OfficeViewModel::class.java) ->
            OfficeViewModel(c.authRepository, c.preferencesRepository, c.connectivity, c.observeOfficeWeek, c.toggleOfficeDay, c.clock)
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(
                c.authRepository, c.preferencesRepository, c.teamRepository, c.googleIdTokenProvider, c.syncTrigger,
            )
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}
