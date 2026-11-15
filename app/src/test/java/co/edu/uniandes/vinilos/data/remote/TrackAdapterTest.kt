package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.di.createAlbumApiService
import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import co.edu.uniandes.vinilos.domain.model.InvalidTrackException
import co.edu.uniandes.vinilos.domain.model.NewTrack
import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Pruebas de integración HTTP de la asociación de tracks, con el contrato de BackVynils. */
class TrackAdapterTest {

    private lateinit var server: MockWebServer
    private lateinit var adapter: AlbumServiceAdapter

    private val newTrack = NewTrack(name = "Decisiones", duration = "5:05")

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        adapter = AlbumServiceAdapter(createAlbumApiService(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun asociar_enviaNombreYDuracion_yDevuelveElTrack() = runTest {
        server.enqueue(MockResponse().setBody("""{"id":7,"name":"Decisiones","duration":"5:05"}"""))

        val track = adapter.addTrack(100, newTrack)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/albums/100/tracks", request.path)
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(setOf("name", "duration"), body.keySet())
        assertEquals(7, track.id)
    }

    @Test(expected = InvalidTrackException::class)
    fun asociar400_lanzaInvalidTrack() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"statusCode":400}"""))

        adapter.addTrack(100, newTrack)
    }

    @Test(expected = AlbumNotFoundException::class)
    fun asociar404_lanzaAlbumNotFound() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"statusCode":404}"""))

        adapter.addTrack(999, newTrack)
    }
}
