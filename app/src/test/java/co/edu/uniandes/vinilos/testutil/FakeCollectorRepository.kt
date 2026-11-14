package co.edu.uniandes.vinilos.testutil

import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.Collector
import co.edu.uniandes.vinilos.domain.model.CollectorNotFoundException
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.domain.repository.CollectorRepository

/** Repositorio en memoria con respuestas deterministas; `error` simula un fallo en cada llamada. */
class FakeCollectorRepository(
    var collectors: List<Collector> = sampleCollectors,
    var error: Exception? = null,
) : CollectorRepository {

    var getCollectorsCalls = 0
        private set

    override suspend fun getCollectors(): List<Collector> {
        getCollectorsCalls++
        error?.let { throw it }
        return collectors
    }

    override suspend fun getCollector(collectorId: Int): Collector {
        error?.let { throw it }
        return collectors.firstOrNull { it.id == collectorId } ?: throw CollectorNotFoundException(collectorId)
    }

    companion object {
        val sampleCollectors = listOf(
            Collector(
                id = 100,
                name = "Manolo Bellon",
                telephone = "3502457896",
                email = "manollo@caracol.com.co",
                favoritePerformers = listOf(Performer(100, "Rubén Blades", "")),
                albumCount = 1,
                albums = listOf(Album(100, "Buscando América", "", "1984-08-01T00:00:00.000Z", "", "Salsa", "Elektra")),
            ),
            Collector(id = 101, name = "Jaime Monsalve", telephone = "", email = ""),
        )
    }
}
