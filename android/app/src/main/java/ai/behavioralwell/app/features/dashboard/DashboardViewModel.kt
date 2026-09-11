package ai.behavioralwell.app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.behavioralwell.app.BehavioralWellApplication
import ai.behavioralwell.app.data.models.DashboardResponse
import ai.behavioralwell.app.data.repositories.NetworkResult
import ai.behavioralwell.app.data.repositories.TelemetryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val dashboard: DashboardResponse) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel : ViewModel() {

    private val repository by lazy { TelemetryRepository(BehavioralWellApplication.instance) }

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        repository.schedulePeriodicSync()
    }

    fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = DashboardUiState.Loading
            try {
                repository.collectAndEnqueueAll()
            } catch (_: Exception) {
                // Ignore collection exceptions
            }
            when (val result = repository.getDashboardData()) {
                is NetworkResult.Success -> {
                    _uiState.value = DashboardUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = DashboardUiState.Error(result.message)
                }
            }
        }
    }
}
