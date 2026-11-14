package co.edu.uniandes.vinilos.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.InvalidAlbumException
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import co.edu.uniandes.vinilos.domain.repository.ArtistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Date

sealed interface ArtistOptions {
    data object Loading : ArtistOptions
    data class Loaded(val artists: List<Artist>) : ArtistOptions
    data object Error : ArtistOptions
}

enum class SubmitError { NETWORK, INVALID, UNKNOWN, ARTIST_LINK }

sealed interface Submission {
    data object Idle : Submission
    data object Sending : Submission
    data object Saved : Submission
    data class Failed(val error: SubmitError) : Submission
}

data class CreateAlbumUiState(
    val form: AlbumForm = AlbumForm(),
    val errors: Map<AlbumField, AlbumFieldError> = emptyMap(),
    val artistOptions: ArtistOptions = ArtistOptions.Loading,
    val submission: Submission = Submission.Idle,
    /** Campo al que la pantalla debe llevar el foco tras un intento de guardar con errores. */
    val focusRequest: AlbumField? = null,
    /** El álbum ya existe y solo falta asociar el artista: el formulario deja de ser editable. */
    val albumCreated: Boolean = false,
)

class CreateAlbumViewModel(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val today: () -> Date = { Date() },
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAlbumUiState())
    val uiState: StateFlow<CreateAlbumUiState> = _uiState.asStateFlow()

    /** Se conserva para que reintentar no repita `POST /albums` y duplique el álbum. */
    private var createdAlbumId: Int? = null

    init {
        loadArtists()
    }

    fun loadArtists() {
        _uiState.update { it.copy(artistOptions = ArtistOptions.Loading) }
        viewModelScope.launch {
            val options = try {
                ArtistOptions.Loaded(artistRepository.getArtists())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ArtistOptions.Error
            }
            _uiState.update { it.copy(artistOptions = options) }
        }
    }

    fun onNameChange(value: String) = updateForm(AlbumField.NAME) { copy(name = value) }
    fun onArtistChange(value: Artist) = updateForm(AlbumField.ARTIST) { copy(artist = value) }
    fun onGenreChange(value: String) = updateForm(AlbumField.GENRE) { copy(genre = value) }
    fun onRecordLabelChange(value: String) = updateForm(AlbumField.RECORD_LABEL) { copy(recordLabel = value) }
    fun onReleaseDateChange(value: String) = updateForm(AlbumField.RELEASE_DATE) { copy(releaseDate = value) }
    fun onCoverChange(value: String) = updateForm(AlbumField.COVER) { copy(cover = value) }
    fun onDescriptionChange(value: String) = updateForm(AlbumField.DESCRIPTION) { copy(description = value) }

    /** Mientras no haya error en el campo no se valida al escribir, para no regañar a medias. */
    private fun updateForm(field: AlbumField, change: AlbumForm.() -> AlbumForm) {
        _uiState.update { state ->
            val form = state.form.change()
            val errors = if (field in state.errors) state.errors.withResult(field, form) else state.errors
            state.copy(form = form, errors = errors)
        }
    }

    fun onFieldBlur(field: AlbumField) {
        _uiState.update { it.copy(errors = it.errors.withResult(field, it.form)) }
    }

    private fun Map<AlbumField, AlbumFieldError>.withResult(field: AlbumField, form: AlbumForm) =
        form.validate(field, today())?.let { this + (field to it) } ?: (this - field)

    fun onFocusHandled() {
        _uiState.update { it.copy(focusRequest = null) }
    }

    fun save() {
        val state = _uiState.value
        if (state.submission == Submission.Sending) return
        val errors = state.form.validateAll(today())
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors, focusRequest = errors.keys.minOrNull()) }
            return
        }
        val artist = checkNotNull(state.form.artist)
        _uiState.update { it.copy(errors = emptyMap(), submission = Submission.Sending) }
        viewModelScope.launch {
            val albumId = createdAlbumId ?: createAlbum(state.form) ?: return@launch
            linkArtist(albumId, artist)
        }
    }

    private suspend fun createAlbum(form: AlbumForm): Int? = try {
        albumRepository.createAlbum(form.toNewAlbum()).id.also { id ->
            createdAlbumId = id
            _uiState.update { it.copy(albumCreated = true) }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val error = when (e) {
            is InvalidAlbumException -> SubmitError.INVALID
            is IOException -> SubmitError.NETWORK
            else -> SubmitError.UNKNOWN
        }
        _uiState.update { it.copy(submission = Submission.Failed(error)) }
        null
    }

    private suspend fun linkArtist(albumId: Int, artist: Artist) {
        val submission = try {
            albumRepository.addArtistToAlbum(albumId, artist.id, artist.type)
            Submission.Saved
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Submission.Failed(SubmitError.ARTIST_LINK)
        }
        _uiState.update { it.copy(submission = submission) }
    }

    companion object {
        fun factory(
            albumRepository: AlbumRepository,
            artistRepository: ArtistRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { CreateAlbumViewModel(albumRepository, artistRepository) }
        }
    }
}
