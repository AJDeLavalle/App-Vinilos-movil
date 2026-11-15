package co.edu.uniandes.vinilos.ui.artists

import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.ui.common.ErrorType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ArtistDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(artistId: Int, type: ArtistType, repository: FakeArtistRepository) =
        ArtistDetailViewModel(
            SavedStateHandle(
                mapOf(
                    ArtistDetailViewModel.ARTIST_ID_ARG to artistId,
                    ArtistDetailViewModel.ARTIST_TYPE_ARG to type.name,
                )
            ),
            repository,
        )

    @Test
    fun musicoExistente_estadoSuccessConAlbumesYPremios() {
        val viewModel = viewModel(100, ArtistType.MUSICIAN, FakeArtistRepository())

        val artist = (viewModel.uiState.value as ArtistDetailUiState.Success).artist
        assertEquals("Rubén Blades Bellido de Luna", artist.name)
        assertEquals("1948", artist.year)
        assertEquals("Buscando América", artist.albums.single().name)
        assertEquals("1978", artist.prizes.single().year)
    }

    @Test
    fun bandaExistente_consultaConElTipoDeLaRuta() {
        val viewModel = viewModel(101, ArtistType.BAND, FakeArtistRepository())

        val artist = (viewModel.uiState.value as ArtistDetailUiState.Success).artist
        assertEquals(ArtistType.BAND, artist.type)
        assertEquals("Queen", artist.name)
    }

    @Test
    fun idInexistente_estadoErrorNotFound() {
        val viewModel = viewModel(999, ArtistType.MUSICIAN, FakeArtistRepository())

        assertEquals(ArtistDetailUiState.Error(ErrorType.NOT_FOUND), viewModel.uiState.value)
    }

    @Test
    fun falloDeRed_estadoErrorNetwork_yReintentoRecupera() {
        val repository = FakeArtistRepository(error = IOException("sin red"))
        val viewModel = viewModel(101, ArtistType.BAND, repository)

        assertEquals(ArtistDetailUiState.Error(ErrorType.NETWORK), viewModel.uiState.value)

        repository.error = null
        viewModel.loadArtist()

        assertEquals(101, (viewModel.uiState.value as ArtistDetailUiState.Success).artist.id)
    }
}
