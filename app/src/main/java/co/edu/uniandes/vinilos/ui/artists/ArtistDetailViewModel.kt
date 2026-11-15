package co.edu.uniandes.vinilos.ui.artists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.repository.ArtistRepository
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.toErrorType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ArtistDetailUiState {
    data object Loading : ArtistDetailUiState
    data class Success(val artist: Artist) : ArtistDetailUiState
    data class Error(val type: ErrorType) : ArtistDetailUiState
}

class ArtistDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ArtistRepository,
) : ViewModel() {

    /** Los provee Navigation a partir de la ruta `artists/{artistType}/{artistId}`. */
    private val artistId: Int = checkNotNull(savedStateHandle[ARTIST_ID_ARG])
    private val artistType: ArtistType =
        ArtistType.valueOf(checkNotNull(savedStateHandle[ARTIST_TYPE_ARG]))

    private val _uiState = MutableStateFlow<ArtistDetailUiState>(ArtistDetailUiState.Loading)
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        loadArtist()
    }

    fun loadArtist() {
        _uiState.value = ArtistDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                ArtistDetailUiState.Success(repository.getArtist(artistId, artistType))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ArtistDetailUiState.Error(e.toErrorType())
            }
        }
    }

    companion object {
        const val ARTIST_ID_ARG = "artistId"
        const val ARTIST_TYPE_ARG = "artistType"

        fun factory(repository: ArtistRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ArtistDetailViewModel(createSavedStateHandle(), repository) }
        }
    }
}
