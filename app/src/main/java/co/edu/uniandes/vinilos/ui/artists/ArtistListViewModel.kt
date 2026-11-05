package co.edu.uniandes.vinilos.ui.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.repository.ArtistRepository
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.toErrorType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ArtistListUiState {
    data object Loading : ArtistListUiState
    data class Success(val artists: List<Artist>) : ArtistListUiState
    data object Empty : ArtistListUiState
    data class Error(val type: ErrorType) : ArtistListUiState
}

class ArtistListViewModel(private val repository: ArtistRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ArtistListUiState>(ArtistListUiState.Loading)
    val uiState: StateFlow<ArtistListUiState> = _uiState.asStateFlow()

    init {
        loadArtists()
    }

    fun loadArtists() {
        _uiState.value = ArtistListUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val artists = repository.getArtists()
                if (artists.isEmpty()) ArtistListUiState.Empty else ArtistListUiState.Success(artists)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ArtistListUiState.Error(e.toErrorType())
            }
        }
    }

    companion object {
        fun factory(repository: ArtistRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ArtistListViewModel(repository) }
        }
    }
}
