package co.edu.uniandes.vinilos.ui.detail

import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.ui.common.ErrorType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(albumId: Int, repository: FakeAlbumRepository) = AlbumDetailViewModel(
        SavedStateHandle(mapOf(AlbumDetailViewModel.ALBUM_ID_ARG to albumId)),
        repository,
    )

    @Test
    fun albumExistente_estadoSuccessConEseAlbum() {
        val viewModel = viewModel(100, FakeAlbumRepository())

        val state = viewModel.uiState.value as AlbumDetailUiState.Success
        assertEquals(100, state.album.id)
        assertEquals("Buscando América", state.album.name)
    }

    @Test
    fun idInexistente_estadoErrorNotFound() {
        val viewModel = viewModel(999, FakeAlbumRepository())

        assertEquals(AlbumDetailUiState.Error(ErrorType.NOT_FOUND), viewModel.uiState.value)
    }

    @Test
    fun falloDeRed_estadoErrorNetwork_yReintentoRecupera() {
        val repository = FakeAlbumRepository(error = IOException("sin red"))
        val viewModel = viewModel(101, repository)

        assertEquals(AlbumDetailUiState.Error(ErrorType.NETWORK), viewModel.uiState.value)

        repository.error = null
        viewModel.loadAlbum()

        assertEquals(101, (viewModel.uiState.value as AlbumDetailUiState.Success).album.id)
    }
}
