package co.edu.uniandes.vinilos.ui.collectors

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniandes.vinilos.domain.model.Collector
import co.edu.uniandes.vinilos.domain.repository.CollectorRepository
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.toErrorType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CollectorDetailUiState {
    data object Loading : CollectorDetailUiState
    data class Success(val collector: Collector) : CollectorDetailUiState
    data class Error(val type: ErrorType) : CollectorDetailUiState
}

class CollectorDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CollectorRepository,
) : ViewModel() {

    /** Lo provee Navigation a partir del argumento de la ruta `collectors/{collectorId}`. */
    private val collectorId: Int = checkNotNull(savedStateHandle[COLLECTOR_ID_ARG])

    private val _uiState = MutableStateFlow<CollectorDetailUiState>(CollectorDetailUiState.Loading)
    val uiState: StateFlow<CollectorDetailUiState> = _uiState.asStateFlow()

    init {
        loadCollector()
    }

    fun loadCollector() {
        _uiState.value = CollectorDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                CollectorDetailUiState.Success(repository.getCollector(collectorId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CollectorDetailUiState.Error(e.toErrorType())
            }
        }
    }

    companion object {
        const val COLLECTOR_ID_ARG = "collectorId"

        fun factory(repository: CollectorRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CollectorDetailViewModel(createSavedStateHandle(), repository) }
        }
    }
}
