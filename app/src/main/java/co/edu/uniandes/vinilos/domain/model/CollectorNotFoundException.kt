package co.edu.uniandes.vinilos.domain.model

/** El backend respondió 404: no existe un coleccionista con el identificador solicitado. */
class CollectorNotFoundException(val collectorId: Int) :
    Exception("No existe el coleccionista con id $collectorId")
