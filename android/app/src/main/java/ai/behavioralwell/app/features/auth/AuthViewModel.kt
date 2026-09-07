package ai.behavioralwell.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.behavioralwell.app.BehavioralWellApplication
import ai.behavioralwell.app.data.repositories.AuthRepository
import ai.behavioralwell.app.data.repositories.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val userName: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository(BehavioralWellApplication.instance.tokenStorage)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.login(email, password)) {
                is NetworkResult.Success -> {
                    _uiState.value = AuthUiState.Success(result.data.user.name)
                }
                is NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.register(name, email, password)) {
                is NetworkResult.Success -> {
                    _uiState.value = AuthUiState.Success(result.data.user.name)
                }
                is NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
