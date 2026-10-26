package co.edu.uniandes.vinilos.ui.common

import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import java.io.IOException

/** Categorías de error que la UI sabe presentar; evita filtrar excepciones a las pantallas. */
enum class ErrorType { NETWORK, NOT_FOUND, UNKNOWN }

fun Throwable.toErrorType(): ErrorType = when (this) {
    is AlbumNotFoundException -> ErrorType.NOT_FOUND
    is IOException -> ErrorType.NETWORK
    else -> ErrorType.UNKNOWN
}
