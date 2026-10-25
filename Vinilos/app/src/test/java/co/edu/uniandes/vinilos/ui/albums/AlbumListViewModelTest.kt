package co.edu.uniandes.vinilos.ui.albums

import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.ui.common.ErrorType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AlbumListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cargaAlbumes_estadoSuccessConLaLista() {
        val repository = FakeAlbumRepository()

        val viewModel = AlbumListViewModel(repository)

        assertEquals(
            AlbumListUiState.Success(FakeAlbumRepository.sampleAlbums),
            viewModel.uiState.value,
        )
    }

    @Test
    fun listaVacia_estadoEmpty() {
        val viewModel = AlbumListViewModel(FakeAlbumRepository(albums = emptyList()))

        assertEquals(AlbumListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun falloDeRed_estadoErrorNetwork() {
        val viewModel = AlbumListViewModel(FakeAlbumRepository(error = IOException("sin red")))

        assertEquals(AlbumListUiState.Error(ErrorType.NETWORK), viewModel.uiState.value)
    }

    @Test
    fun errorInesperado_estadoErrorUnknown() {
        val viewModel = AlbumListViewModel(FakeAlbumRepository(error = IllegalStateException()))

        assertEquals(AlbumListUiState.Error(ErrorType.UNKNOWN), viewModel.uiState.value)
    }

    @Test
    fun reintentarTrasError_cargaLosAlbumes() {
        val repository = FakeAlbumRepository(error = IOException("sin red"))
        val viewModel = AlbumListViewModel(repository)

        repository.error = null
        viewModel.loadAlbums()

        assertEquals(2, repository.getAlbumsCalls)
        assertEquals(
            AlbumListUiState.Success(FakeAlbumRepository.sampleAlbums),
            viewModel.uiState.value,
        )
    }
}
