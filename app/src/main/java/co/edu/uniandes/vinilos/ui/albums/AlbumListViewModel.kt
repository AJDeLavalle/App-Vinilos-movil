package co.edu.uniandes.vinilos.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

sealed interface AlbumListUiState {
    data object Loading : AlbumListUiState
    data class Success(val albums: List<Album>) : AlbumListUiState
    data object Empty : AlbumListUiState
    data class Error(val type: ErrorType) : AlbumListUiState
}

class AlbumListViewModel(private val repository: AlbumRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumListUiState>(AlbumListUiState.Loading)
    val uiState: StateFlow<AlbumListUiState> = _uiState.asStateFlow()

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        _uiState.value = AlbumListUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val albums = repository.getAlbums()
                if (albums.isEmpty()) AlbumListUiState.Empty else AlbumListUiState.Success(albums)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AlbumListUiState.Error(e.toErrorType())
            }
        }
    }

    companion object {
        fun factory(repository: AlbumRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AlbumListViewModel(repository) }
        }
    }
}
