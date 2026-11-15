package co.edu.uniandes.vinilos.testutil

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

/**
 * Responde según la ruta y no según el orden de llegada: con peticiones en paralelo el orden no
 * es determinista y una cola de respuestas podría cruzarlas. Una ruta sin respuesta da 404.
 */
fun MockWebServer.respondByPath(vararg routes: Pair<String, MockResponse>) {
    val byPath = routes.toMap()
    dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse =
            byPath[request.path] ?: MockResponse().setResponseCode(404)
    }
}

/** Rutas de todas las peticiones recibidas, sin importar el orden. */
fun MockWebServer.requestedPaths(): Set<String?> = List(requestCount) { takeRequest().path }.toSet()
