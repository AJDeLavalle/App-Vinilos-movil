package co.edu.uniandes.vinilos.ui.artists

import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.ui.common.ErrorType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ArtistListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cargaArtistas_estadoSuccessConLaLista() {
        val viewModel = ArtistListViewModel(FakeArtistRepository())

        assertEquals(
            ArtistListUiState.Success(FakeArtistRepository.sampleArtists),
            viewModel.uiState.value,
        )
    }

    @Test
    fun listaVacia_estadoEmpty() {
        val viewModel = ArtistListViewModel(FakeArtistRepository(artists = emptyList()))

        assertEquals(ArtistListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun falloDeRed_estadoErrorNetwork() {
        val viewModel = ArtistListViewModel(FakeArtistRepository(error = IOException("sin red")))

        assertEquals(ArtistListUiState.Error(ErrorType.NETWORK), viewModel.uiState.value)
    }

    @Test
    fun reintentarTrasError_cargaLosArtistas() {
        val repository = FakeArtistRepository(error = IOException("sin red"))
        val viewModel = ArtistListViewModel(repository)

        repository.error = null
        viewModel.loadArtists()

        assertEquals(2, repository.getArtistsCalls)
        assertEquals(
            ArtistListUiState.Success(FakeArtistRepository.sampleArtists),
            viewModel.uiState.value,
        )
    }
}
