package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.cache.MemoryCache
import co.edu.uniandes.vinilos.data.cache.getOrLoad
import co.edu.uniandes.vinilos.data.remote.ArtistServiceAdapter
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.repository.ArtistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lecturas con caché en memoria; ver [AlbumRepositoryImpl]. */
class ArtistRepositoryImpl(
    private val serviceAdapter: ArtistServiceAdapter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val listCache: MemoryCache<Unit, List<Artist>> = MemoryCache(maxEntries = 1, ttlMillis = CACHE_TTL_MILLIS),
    private val detailCache: MemoryCache<Pair<Int, ArtistType>, Artist> =
        MemoryCache(maxEntries = 20, ttlMillis = CACHE_TTL_MILLIS),
) : ArtistRepository {

    override suspend fun getArtists(): List<Artist> = withContext(ioDispatcher) {
        listCache.getOrLoad(Unit) { serviceAdapter.getArtists() }
    }

    override suspend fun getArtist(artistId: Int, type: ArtistType): Artist = withContext(ioDispatcher) {
        detailCache.getOrLoad(artistId to type) { serviceAdapter.getArtist(artistId, type) }
    }

    fun clearCache() {
        listCache.clear()
        detailCache.clear()
    }
}
