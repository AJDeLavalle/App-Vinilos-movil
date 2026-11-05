package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.remote.CollectorServiceAdapter
import co.edu.uniandes.vinilos.domain.model.Collector
import co.edu.uniandes.vinilos.domain.repository.CollectorRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollectorRepositoryImpl(
    private val serviceAdapter: CollectorServiceAdapter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CollectorRepository {

    override suspend fun getCollectors(): List<Collector> = withContext(ioDispatcher) {
        serviceAdapter.getCollectors()
    }

    override suspend fun getCollector(collectorId: Int): Collector = withContext(ioDispatcher) {
        serviceAdapter.getCollector(collectorId)
    }
}
