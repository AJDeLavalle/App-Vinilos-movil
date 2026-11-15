package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.data.remote.dto.CollectorAlbumDto
import co.edu.uniandes.vinilos.data.remote.dto.CollectorDto
import co.edu.uniandes.vinilos.domain.model.Collector
import co.edu.uniandes.vinilos.domain.model.CollectorNotFoundException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException

/**
 * Service Adapter: traduce el contrato HTTP de BackVynils al modelo de dominio. El coleccionista
 * solo trae el id de sus álbumes, así que el detalle los completa con un segundo recurso.
 */
class CollectorServiceAdapter(private val api: CollectorApiService) {

    suspend fun getCollectors(): List<Collector> =
        api.getCollectors().mapNotNull { it.toDomain(emptyList()) }

    /**
     * El coleccionista y sus álbumes se piden en paralelo: casi todos tienen colección, así que
     * esperar al primero para decidir si pedir el segundo solo suma una ida y vuelta.
     */
    suspend fun getCollector(collectorId: Int): Collector = coroutineScope {
        val albumsRequest = async { runCatching { api.getCollectorAlbums(collectorId) } }
        val dto = try {
            api.getCollector(collectorId)
        } catch (e: HttpException) {
            albumsRequest.cancel()
            if (e.code() == 404) throw CollectorNotFoundException(collectorId) else throw e
        }
        val albums = if (dto.collectorAlbums.isNullOrEmpty()) emptyList() else albumsRequest.await().getOrThrow()
        dto.toDomain(albums) ?: throw CollectorNotFoundException(collectorId)
    }
}

/** Descarta registros sin id, que no se pueden navegar ni identificar. */
internal fun CollectorDto.toDomain(albums: List<CollectorAlbumDto>): Collector? {
    val collectorId = id ?: return null
    return Collector(
        id = collectorId,
        name = name.orEmpty(),
        telephone = telephone.orEmpty(),
        email = email.orEmpty(),
        favoritePerformers = favoritePerformers.orEmpty().mapNotNull { it.toDomain() },
        albumCount = collectorAlbums.orEmpty().size,
        albums = albums.mapNotNull { it.album?.toDomain() },
    )
}
