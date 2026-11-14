package co.edu.uniandes.vinilos.testutil

import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.Collector
import co.edu.uniandes.vinilos.domain.model.CollectorNotFoundException
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.domain.repository.CollectorRepository

/**
 * Repositorio determinista para las pruebas E2E de UI (no depende del backend).
 * `detailError` falla solo el detalle, para probarlo después de un listado correcto.
 */
class FakeCollectorRepository(
    var collectors: List<Collector> = sampleCollectors,
    var error: Exception? = null,
    var detailError: Exception? = null,
) : CollectorRepository {

    override suspend fun getCollectors(): List<Collector> {
        error?.let { throw it }
        return collectors
    }

    override suspend fun getCollector(collectorId: Int): Collector {
        detailError?.let { throw it }
        return collectors.firstOrNull { it.id == collectorId } ?: throw CollectorNotFoundException(collectorId)
    }

    companion object {
        /** El artista 1 y el álbum 100 coinciden con [FakeArtistRepository] y [FakeAlbumRepository]. */
        val sampleCollectors = listOf(
            Collector(
                id = 100,
                name = "Manolo Bellon",
                telephone = "3502457896",
                email = "manollo@caracol.com.co",
                favoritePerformers = listOf(Performer(1, "Rubén Blades Bellido de Luna", "")),
                albumCount = 1,
                albums = listOf(Album(100, "Buscando América", "", "1984-08-01T00:00:00.000Z", "", "Salsa", "Elektra")),
            ),
            Collector(id = 101, name = "Jaime Monsalve", telephone = "", email = ""),
        )
    }
}
