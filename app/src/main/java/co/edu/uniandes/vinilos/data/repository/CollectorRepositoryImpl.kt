package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.cache.MemoryCache
import co.edu.uniandes.vinilos.data.cache.getOrLoad
import co.edu.uniandes.vinilos.data.remote.CollectorServiceAdapter
import co.edu.uniandes.vinilos.domain.model.Collector
import co.edu.uniandes.vinilos.domain.repository.CollectorRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lecturas con caché en memoria; ver [AlbumRepositoryImpl]. */
class CollectorRepositoryImpl(
    private val serviceAdapter: CollectorServiceAdapter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val listCache: MemoryCache<Unit, List<Collector>> = MemoryCache(maxEntries = 1, ttlMillis = CACHE_TTL_MILLIS),
    private val detailCache: MemoryCache<Int, Collector> = MemoryCache(maxEntries = 20, ttlMillis = CACHE_TTL_MILLIS),
) : CollectorRepository {

    override suspend fun getCollectors(): List<Collector> = withContext(ioDispatcher) {
        listCache.getOrLoad(Unit) { serviceAdapter.getCollectors() }
    }

    override suspend fun getCollector(collectorId: Int): Collector = withContext(ioDispatcher) {
        detailCache.getOrLoad(collectorId) { serviceAdapter.getCollector(collectorId) }
    }

    fun clearCache() {
        listCache.clear()
        detailCache.clear()
    }
}
