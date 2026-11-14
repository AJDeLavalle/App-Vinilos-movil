package co.edu.uniandes.vinilos.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.toErrorType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState
    data class Success(val album: Album) : AlbumDetailUiState
    data class Error(val type: ErrorType) : AlbumDetailUiState
}

class AlbumDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: AlbumRepository,
) : ViewModel() {

    /** Lo provee Navigation a partir del argumento de la ruta `albums/{albumId}`. */
    private val albumId: Int = checkNotNull(savedStateHandle[ALBUM_ID_ARG])

    private val _uiState = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        loadAlbum()
    }

    fun loadAlbum() {
        _uiState.value = AlbumDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                AlbumDetailUiState.Success(repository.getAlbum(albumId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AlbumDetailUiState.Error(e.toErrorType())
            }
        }
    }

    /**
     * Vuelve a consultar el álbum sin pasar por Loading, para que la lista siga en pantalla y
     * conserve su posición (por ejemplo, al volver de asociar un track). Si falla, se queda con
     * lo que ya mostraba.
     */
    fun refresh() {
        if (_uiState.value !is AlbumDetailUiState.Success) return loadAlbum()
        viewModelScope.launch {
            try {
                _uiState.value = AlbumDetailUiState.Success(repository.getAlbum(albumId))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        const val ALBUM_ID_ARG = "albumId"

        fun factory(repository: AlbumRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AlbumDetailViewModel(createSavedStateHandle(), repository) }
        }
    }
}
