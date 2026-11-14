package co.edu.uniandes.vinilos.ui.tracks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import co.edu.uniandes.vinilos.domain.model.InvalidTrackException
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.toErrorType
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface AlbumLoad {
    data object Loading : AlbumLoad
    data class Loaded(val album: Album) : AlbumLoad
    data class Error(val type: ErrorType) : AlbumLoad
}

enum class TrackSubmitError { NETWORK, INVALID, ALBUM_NOT_FOUND, UNKNOWN }

sealed interface TrackSubmission {
    data object Idle : TrackSubmission
    data object Sending : TrackSubmission
    data object Saved : TrackSubmission
    data class Failed(val error: TrackSubmitError) : TrackSubmission
}

data class AssociateTrackUiState(
    val album: AlbumLoad = AlbumLoad.Loading,
    val form: TrackForm = TrackForm(),
    val errors: Map<TrackField, TrackFieldError> = emptyMap(),
    val submission: TrackSubmission = TrackSubmission.Idle,
    /** Campo al que la pantalla debe llevar el foco tras un intento de guardar con errores. */
    val focusRequest: TrackField? = null,
)

class AssociateTrackViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: AlbumRepository,
) : ViewModel() {

    /** Lo provee Navigation a partir de la ruta `albums/{albumId}/tracks/new`. */
    private val albumId: Int = checkNotNull(savedStateHandle[AlbumDetailViewModel.ALBUM_ID_ARG])

    private val _uiState = MutableStateFlow(AssociateTrackUiState())
    val uiState: StateFlow<AssociateTrackUiState> = _uiState.asStateFlow()

    init {
        loadAlbum()
    }

    fun loadAlbum() {
        _uiState.update { it.copy(album = AlbumLoad.Loading) }
        viewModelScope.launch {
            val album = try {
                AlbumLoad.Loaded(repository.getAlbum(albumId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AlbumLoad.Error(e.toErrorType())
            }
            _uiState.update { it.copy(album = album) }
        }
    }

    fun onNameChange(value: String) = updateForm(TrackField.NAME) { copy(name = value) }
    fun onDurationChange(value: String) = updateForm(TrackField.DURATION) { copy(duration = value) }

    /** Mientras no haya error en el campo no se valida al escribir, para no regañar a medias. */
    private fun updateForm(field: TrackField, change: TrackForm.() -> TrackForm) {
        _uiState.update { state ->
            val form = state.form.change()
            val errors = if (field in state.errors) state.errors.withResult(field, form) else state.errors
            state.copy(form = form, errors = errors)
        }
    }

    fun onFieldBlur(field: TrackField) {
        _uiState.update { it.copy(errors = it.errors.withResult(field, it.form)) }
    }

    private fun Map<TrackField, TrackFieldError>.withResult(field: TrackField, form: TrackForm) =
        form.validate(field)?.let { this + (field to it) } ?: (this - field)

    fun onFocusHandled() {
        _uiState.update { it.copy(focusRequest = null) }
    }

    fun save() {
        val state = _uiState.value
        if (state.submission == TrackSubmission.Sending) return
        val errors = state.form.validateAll()
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors, focusRequest = errors.keys.minOrNull()) }
            return
        }
        _uiState.update { it.copy(errors = emptyMap(), submission = TrackSubmission.Sending) }
        viewModelScope.launch {
            val submission = try {
                repository.addTrack(albumId, state.form.toNewTrack())
                TrackSubmission.Saved
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                TrackSubmission.Failed(
                    when (e) {
                        is InvalidTrackException -> TrackSubmitError.INVALID
                        is AlbumNotFoundException -> TrackSubmitError.ALBUM_NOT_FOUND
                        is IOException -> TrackSubmitError.NETWORK
                        else -> TrackSubmitError.UNKNOWN
                    }
                )
            }
            _uiState.update { it.copy(submission = submission) }
        }
    }

    companion object {
        fun factory(repository: AlbumRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AssociateTrackViewModel(createSavedStateHandle(), repository) }
        }
    }
}
