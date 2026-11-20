package com.meridian.app.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meridian.app.data.auth.GoogleIdTokenProvider
import com.meridian.app.domain.repository.ConnectivityRepository
import com.meridian.app.domain.repository.SignInResult
import com.meridian.app.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class LoginError { WRONG_DOMAIN, OFFLINE, GENERIC }

data class LoginUiState(val isLoading: Boolean = false, val error: LoginError? = null)

class LoginViewModel(
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val tokenProvider: GoogleIdTokenProvider,
    private val connectivity: ConnectivityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onContinueWithGoogle(context: Context) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            if (!connectivity.isOnline.first()) {
                _state.value = LoginUiState(error = LoginError.OFFLINE)
                return@launch
            }
            _state.value = LoginUiState(isLoading = true)
            val token = try {
                tokenProvider.requestIdToken(context)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = LoginUiState(error = LoginError.GENERIC)
                return@launch
            }
            // Si sale bien, la sesión cambia y la navegación pasa sola a Inicio.
            _state.value = when (signInWithGoogle(token)) {
                SignInResult.Success -> LoginUiState()
                SignInResult.WrongDomain -> LoginUiState(error = LoginError.WRONG_DOMAIN)
                SignInResult.Offline -> LoginUiState(error = LoginError.OFFLINE)
                SignInResult.Error -> LoginUiState(error = LoginError.GENERIC)
            }
        }
    }
}
