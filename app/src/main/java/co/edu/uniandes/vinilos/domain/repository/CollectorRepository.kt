package co.edu.uniandes.vinilos.domain.repository

import co.edu.uniandes.vinilos.domain.model.Collector

/**
 * Punto único de acceso a los coleccionistas para la capa de UI.
 *
 * Las implementaciones lanzan [co.edu.uniandes.vinilos.domain.model.CollectorNotFoundException]
 * cuando el coleccionista no existe y [java.io.IOException] ante fallos de red.
 */
interface CollectorRepository {
    suspend fun getCollectors(): List<Collector>
    suspend fun getCollector(collectorId: Int): Collector
}
