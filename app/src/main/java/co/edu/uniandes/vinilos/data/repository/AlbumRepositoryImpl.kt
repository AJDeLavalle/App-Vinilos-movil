package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.cache.MemoryCache
import co.edu.uniandes.vinilos.data.cache.getOrLoad
import co.edu.uniandes.vinilos.data.remote.AlbumServiceAdapter
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.model.NewAlbum
import co.edu.uniandes.vinilos.domain.model.NewTrack
import co.edu.uniandes.vinilos.domain.model.Track
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Las lecturas pasan por una caché en memoria: volver a un álbum, abrir el formulario de asociar
 * track o regresar al catálogo no repite la consulta. Cada escritura invalida lo que cambió y
 * avisa con [onAlbumsChanged] a los repositorios que muestran álbumes (artistas, coleccionistas).
 */
class AlbumRepositoryImpl(
    private val serviceAdapter: AlbumServiceAdapter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val listCache: MemoryCache<Unit, List<Album>> = MemoryCache(maxEntries = 1, ttlMillis = CACHE_TTL_MILLIS),
    private val detailCache: MemoryCache<Int, Album> = MemoryCache(maxEntries = 20, ttlMillis = CACHE_TTL_MILLIS),
    private val onAlbumsChanged: () -> Unit = {},
) : AlbumRepository {

    override suspend fun getAlbums(): List<Album> = withContext(ioDispatcher) {
        listCache.getOrLoad(Unit) { serviceAdapter.getAlbums() }
    }

    override suspend fun getAlbum(albumId: Int): Album = withContext(ioDispatcher) {
        detailCache.getOrLoad(albumId) { serviceAdapter.getAlbum(albumId) }
    }

    override suspend fun createAlbum(album: NewAlbum): Album = withContext(ioDispatcher) {
        serviceAdapter.createAlbum(album).also { albumsChanged() }
    }

    override suspend fun addArtistToAlbum(albumId: Int, artistId: Int, type: ArtistType) =
        withContext(ioDispatcher) {
            serviceAdapter.addArtistToAlbum(albumId, artistId, type)
            detailCache.remove(albumId)
            albumsChanged()
        }

    override suspend fun addTrack(albumId: Int, track: NewTrack): Track = withContext(ioDispatcher) {
        serviceAdapter.addTrack(albumId, track).also {
            detailCache.remove(albumId)
            listCache.clear()
        }
    }

    /** Libera la caché, por ejemplo cuando el sistema pide memoria. */
    fun clearCache() {
        listCache.clear()
        detailCache.clear()
    }

    private fun albumsChanged() {
        listCache.clear()
        onAlbumsChanged()
    }
}

/** Los datos los editan otros usuarios del backend: la caché solo evita repetir consultas seguidas. */
internal const val CACHE_TTL_MILLIS = 2 * 60 * 1000L
