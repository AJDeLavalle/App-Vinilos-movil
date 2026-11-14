package co.edu.uniandes.vinilos.ui.tracks

import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import co.edu.uniandes.vinilos.domain.model.InvalidTrackException
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AssociateTrackTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAlbumRepository()

    private fun viewModel(albumId: Int = 100) = AssociateTrackViewModel(
        SavedStateHandle(mapOf(AlbumDetailViewModel.ALBUM_ID_ARG to albumId)),
        repository,
    )

    @Test
    fun duracion_aceptaFormatosValidos() {
        listOf("5:05", "05:05", "0:01", "12:59", "100:00").forEach { duration ->
            assertNull(duration, TrackForm(duration = duration).validate(TrackField.DURATION))
        }
    }

    @Test
    fun duracion_rechazaFormatosInvalidos() {
        listOf("abc", "5", "5:5", "5:60", ":30", "5:05:01", "-1:00", "0:00", "00:00").forEach { duration ->
            assertEquals(
                duration,
                TrackFieldError.DURATION_INVALID,
                TrackForm(duration = duration).validate(TrackField.DURATION),
            )
        }
    }

    @Test
    fun campos_vacios_sonObligatorios() {
        assertEquals(
            mapOf(
                TrackField.NAME to TrackFieldError.NAME_REQUIRED,
                TrackField.DURATION to TrackFieldError.DURATION_REQUIRED,
            ),
            TrackForm(name = "  ").validateAll(),
        )
    }

    @Test
    fun nombre_respetaElLimiteDeCaracteres() {
        assertNull(TrackForm(name = "a".repeat(100)).validate(TrackField.NAME))
        assertEquals(TrackFieldError.NAME_TOO_LONG, TrackForm(name = "a".repeat(101)).validate(TrackField.NAME))
    }

    @Test
    fun alAbrir_cargaElAlbumYSusTracks() {
        val album = (viewModel().uiState.value.album as AlbumLoad.Loaded).album

        assertEquals("Buscando América", album.name)
        assertEquals(2, album.tracks.size)
    }

    @Test
    fun albumInexistente_estadoErrorNotFound() {
        assertEquals(AlbumLoad.Error(ErrorType.NOT_FOUND), viewModel(albumId = 999).uiState.value.album)
    }

    @Test
    fun guardarVacio_noEnvia_yPideFocoEnElNombre() {
        val viewModel = viewModel()

        viewModel.save()

        assertEquals(0, repository.addTrackCalls)
        assertEquals(TrackField.NAME, viewModel.uiState.value.focusRequest)
        assertEquals(2, viewModel.uiState.value.errors.size)
    }

    @Test
    fun guardarValido_enviaSinEspaciosSobrantes() {
        val viewModel = viewModel()
        viewModel.onNameChange("  Nuevo  ")
        viewModel.onDurationChange(" 3:45 ")

        viewModel.save()

        assertEquals(TrackSubmission.Saved, viewModel.uiState.value.submission)
        assertEquals("Nuevo", repository.lastNewTrack?.name)
        assertEquals("3:45", repository.lastNewTrack?.duration)
    }

    @Test
    fun alCorregirUnCampoConError_elErrorDesaparece() {
        val viewModel = viewModel()
        viewModel.onDurationChange("abc")
        viewModel.onFieldBlur(TrackField.DURATION)
        assertEquals(TrackFieldError.DURATION_INVALID, viewModel.uiState.value.errors[TrackField.DURATION])

        viewModel.onDurationChange("3:45")

        assertTrue(viewModel.uiState.value.errors.isEmpty())
    }

    @Test
    fun falloDeRed_conservaLosDatos_yPermiteReintentar() {
        repository.trackError = IOException("sin red")
        val viewModel = viewModel()
        viewModel.onNameChange("Nuevo")
        viewModel.onDurationChange("3:45")

        viewModel.save()

        assertEquals(TrackSubmission.Failed(TrackSubmitError.NETWORK), viewModel.uiState.value.submission)
        assertEquals("Nuevo", viewModel.uiState.value.form.name)

        repository.trackError = null
        viewModel.save()

        assertEquals(TrackSubmission.Saved, viewModel.uiState.value.submission)
    }

    @Test
    fun erroresDelBackend_seDistinguen() {
        val viewModel = viewModel()
        viewModel.onNameChange("Nuevo")
        viewModel.onDurationChange("3:45")

        repository.trackError = InvalidTrackException()
        viewModel.save()
        assertEquals(TrackSubmission.Failed(TrackSubmitError.INVALID), viewModel.uiState.value.submission)

        repository.trackError = AlbumNotFoundException(100)
        viewModel.save()
        assertEquals(TrackSubmission.Failed(TrackSubmitError.ALBUM_NOT_FOUND), viewModel.uiState.value.submission)
    }
}
