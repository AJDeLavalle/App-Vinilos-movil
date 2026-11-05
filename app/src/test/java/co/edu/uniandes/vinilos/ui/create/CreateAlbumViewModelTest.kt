package co.edu.uniandes.vinilos.ui.create

import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.model.InvalidAlbumException
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Date

class CreateAlbumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albums = FakeAlbumRepository()
    private val artists = FakeArtistRepository()
    private val band = FakeArtistRepository.sampleArtists.first { it.type == ArtistType.BAND }

    private fun viewModel() = CreateAlbumViewModel(albums, artists, today = { Date() })

    private fun CreateAlbumViewModel.fill(artist: Artist = band) {
        onNameChange("Nuevo")
        onArtistChange(artist)
        onGenreChange("Rock")
        onRecordLabelChange("EMI")
        onReleaseDateChange("21/11/1975")
        onCoverChange("https://example.com/a.jpg")
        onDescriptionChange("Descripción")
    }

    @Test
    fun alAbrir_cargaLosArtistas() {
        assertEquals(
            ArtistOptions.Loaded(FakeArtistRepository.sampleArtists),
            viewModel().uiState.value.artistOptions,
        )
    }

    @Test
    fun falloAlCargarArtistas_estadoError_yReintentoRecupera() {
        artists.error = IOException("sin red")
        val viewModel = viewModel()

        assertEquals(ArtistOptions.Error, viewModel.uiState.value.artistOptions)

        artists.error = null
        viewModel.loadArtists()

        assertTrue(viewModel.uiState.value.artistOptions is ArtistOptions.Loaded)
    }

    @Test
    fun guardarVacio_noEnvia_yPideFocoEnElPrimerCampo() {
        val viewModel = viewModel()

        viewModel.save()

        val state = viewModel.uiState.value
        assertEquals(0, albums.createCalls)
        assertEquals(AlbumField.entries.toSet(), state.errors.keys)
        assertEquals(AlbumField.NAME, state.focusRequest)
    }

    @Test
    fun alSalirDeUnCampo_loValida_yAlCorregirloDesapareceElError() {
        val viewModel = viewModel()

        viewModel.onFieldBlur(AlbumField.NAME)
        assertEquals(AlbumFieldError.NAME_REQUIRED, viewModel.uiState.value.errors[AlbumField.NAME])

        viewModel.onNameChange("Nuevo")
        assertNull(viewModel.uiState.value.errors[AlbumField.NAME])
    }

    @Test
    fun alEscribirEnCampoSinError_noMuestraErrorTodavia() {
        val viewModel = viewModel()

        viewModel.onReleaseDateChange("01/0")

        assertTrue(viewModel.uiState.value.errors.isEmpty())
    }

    @Test
    fun guardarValido_creaElAlbum_yAsociaElArtistaConSuTipo() {
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.save()

        assertEquals(Submission.Saved, viewModel.uiState.value.submission)
        assertEquals("1975-11-21", albums.lastNewAlbum?.releaseDate)
        assertEquals(
            listOf(Triple(FakeAlbumRepository.NEW_ALBUM_ID, band.id, ArtistType.BAND)),
            albums.linkCalls,
        )
    }

    @Test
    fun falloDeRedAlCrear_conservaLosDatos_yPermiteReintentar() {
        albums.createError = IOException("sin red")
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.save()

        val state = viewModel.uiState.value
        assertEquals(Submission.Failed(SubmitError.NETWORK), state.submission)
        assertEquals("Nuevo", state.form.name)
        assertFalse(state.albumCreated)

        albums.createError = null
        viewModel.save()

        assertEquals(Submission.Saved, viewModel.uiState.value.submission)
    }

    @Test
    fun backendRechazaLosDatos_estadoInvalid() {
        albums.createError = InvalidAlbumException()
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.save()

        assertEquals(Submission.Failed(SubmitError.INVALID), viewModel.uiState.value.submission)
    }

    @Test
    fun falloAlAsociar_reintentarNoCreaUnAlbumDuplicado() {
        albums.linkError = IOException("sin red")
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.save()

        val state = viewModel.uiState.value
        assertEquals(Submission.Failed(SubmitError.ARTIST_LINK), state.submission)
        assertTrue(state.albumCreated)

        albums.linkError = null
        viewModel.save()

        assertEquals(Submission.Saved, viewModel.uiState.value.submission)
        assertEquals(1, albums.createCalls)
        assertEquals(2, albums.linkCalls.size)
    }
}
