package co.edu.uniandes.vinilos.ui.collectors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

sealed interface CollectorListUiState {
    data object Loading : CollectorListUiState
    data class Success(val collectors: List<Collector>) : CollectorListUiState
    data object Empty : CollectorListUiState
    data class Error(val type: ErrorType) : CollectorListUiState
}

class CollectorListViewModel(private val repository: CollectorRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectorListUiState>(CollectorListUiState.Loading)
    val uiState: StateFlow<CollectorListUiState> = _uiState.asStateFlow()

    init {
        loadCollectors()
    }

    fun loadCollectors() {
        _uiState.value = CollectorListUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val collectors = repository.getCollectors()
                if (collectors.isEmpty()) CollectorListUiState.Empty else CollectorListUiState.Success(collectors)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CollectorListUiState.Error(e.toErrorType())
            }
        }
    }

    companion object {
        fun factory(repository: CollectorRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CollectorListViewModel(repository) }
        }
    }
}
