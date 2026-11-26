package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.BuildConfig
import co.edu.uniandes.vinilos.di.AppContainer
import co.edu.uniandes.vinilos.di.createAlbumApiService
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** DEF-07: las peticiones salen con el User-Agent de la app y no con el de OkHttp. */
class UserAgentInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun peticionesALaApi_salenConElUserAgentDeVinilos() = runTest {
        server.enqueue(MockResponse().setBody("[]"))

        AlbumServiceAdapter(createAlbumApiService(server.url("/").toString())).getAlbums()

        assertVinilosUserAgent(server.takeRequest().getHeader("User-Agent"))
    }

    @Test
    fun clienteCompartidoConCoil_pideImagenesConElUserAgentDeVinilos() {
        server.enqueue(MockResponse().setBody("png"))
        val client = AppContainer(server.url("/").toString()).okHttpClient
        val request = Request.Builder().url(server.url("/Queen_A_Night_At_The_Opera.png")).build()

        client.newCall(request).execute().use { assertEquals(200, it.code) }

        assertVinilosUserAgent(server.takeRequest().getHeader("User-Agent"))
    }

    @Test
    fun userAgent_esGenerico_yValidoComoCabecera() {
        assertEquals("Vinilos/2.0 (Android 17; co.edu.uniandes.vinilos)", vinilosUserAgent("2.0", "17"))

        val limpio = vinilosUserAgent("2.0", "17ñ")

        assertEquals("Vinilos/2.0 (Android 17; co.edu.uniandes.vinilos)", limpio)
        assertEquals(limpio, Headers.Builder().add("User-Agent", limpio).build()["User-Agent"])
    }

    private fun assertVinilosUserAgent(actual: String?) {
        assertEquals(vinilosUserAgent(), actual)
        assertTrue(actual.orEmpty().startsWith("Vinilos/${BuildConfig.VERSION_NAME} (Android "))
        assertFalse(actual.orEmpty().contains("okhttp"))
    }
}
