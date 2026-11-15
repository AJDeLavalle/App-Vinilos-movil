package co.edu.uniandes.vinilos.ui.common

import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import co.edu.uniandes.vinilos.domain.model.ArtistNotFoundException
import co.edu.uniandes.vinilos.domain.model.CollectorNotFoundException
import java.io.IOException

/** Categorías de error que la UI sabe presentar; evita filtrar excepciones a las pantallas. */
enum class ErrorType { NETWORK, NOT_FOUND, UNKNOWN }

fun Throwable.toErrorType(): ErrorType = when (this) {
    is AlbumNotFoundException, is ArtistNotFoundException, is CollectorNotFoundException -> ErrorType.NOT_FOUND
    is IOException -> ErrorType.NETWORK
    else -> ErrorType.UNKNOWN
}
