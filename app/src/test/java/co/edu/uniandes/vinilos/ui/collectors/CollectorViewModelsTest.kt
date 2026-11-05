package co.edu.uniandes.vinilos.ui.collectors

import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.testutil.FakeCollectorRepository
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.ui.common.ErrorType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class CollectorViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun detailViewModel(collectorId: Int, repository: FakeCollectorRepository) =
        CollectorDetailViewModel(
            SavedStateHandle(mapOf(CollectorDetailViewModel.COLLECTOR_ID_ARG to collectorId)),
            repository,
        )

    @Test
    fun listado_cargaColeccionistas_estadoSuccess() {
        val viewModel = CollectorListViewModel(FakeCollectorRepository())

        assertEquals(
            CollectorListUiState.Success(FakeCollectorRepository.sampleCollectors),
            viewModel.uiState.value,
        )
    }

    @Test
    fun listado_vacio_estadoEmpty() {
        val viewModel = CollectorListViewModel(FakeCollectorRepository(collectors = emptyList()))

        assertEquals(CollectorListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun listado_falloDeRed_yReintentoRecupera() {
        val repository = FakeCollectorRepository(error = IOException("sin red"))
        val viewModel = CollectorListViewModel(repository)

        assertEquals(CollectorListUiState.Error(ErrorType.NETWORK), viewModel.uiState.value)

        repository.error = null
        viewModel.loadCollectors()

        assertEquals(2, repository.getCollectorsCalls)
        assertEquals(
            CollectorListUiState.Success(FakeCollectorRepository.sampleCollectors),
            viewModel.uiState.value,
        )
    }

    @Test
    fun detalle_existente_estadoSuccessConGustosYColeccion() {
        val viewModel = detailViewModel(100, FakeCollectorRepository())

        val collector = (viewModel.uiState.value as CollectorDetailUiState.Success).collector
        assertEquals("Manolo Bellon", collector.name)
        assertEquals("Rubén Blades", collector.favoritePerformers.single().name)
        assertEquals("Buscando América", collector.albums.single().name)
    }

    @Test
    fun detalle_idInexistente_estadoErrorNotFound() {
        val viewModel = detailViewModel(999, FakeCollectorRepository())

        assertEquals(CollectorDetailUiState.Error(ErrorType.NOT_FOUND), viewModel.uiState.value)
    }

    @Test
    fun detalle_falloDeRed_yReintentoRecupera() {
        val repository = FakeCollectorRepository(error = IOException("sin red"))
        val viewModel = detailViewModel(101, repository)

        assertEquals(CollectorDetailUiState.Error(ErrorType.NETWORK), viewModel.uiState.value)

        repository.error = null
        viewModel.loadCollector()

        assertEquals(101, (viewModel.uiState.value as CollectorDetailUiState.Success).collector.id)
    }
}
