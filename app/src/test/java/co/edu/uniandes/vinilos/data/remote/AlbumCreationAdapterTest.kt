package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.di.createAlbumApiService
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.model.InvalidAlbumException
import co.edu.uniandes.vinilos.domain.model.NewAlbum
import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

/** Pruebas de integración HTTP de la creación de álbumes, con el contrato de BackVynils. */
class AlbumCreationAdapterTest {

    private lateinit var server: MockWebServer
    private lateinit var adapter: AlbumServiceAdapter

    private val newAlbum = NewAlbum(
        name = "Nuevo",
        cover = "https://example.com/a.jpg",
        releaseDate = "1975-11-21",
        description = "Descripción",
        genre = "Rock",
        recordLabel = "EMI",
    )

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
    fun crear_enviaExactamenteLosSeisCampos_yDevuelveElId() = runTest {
        server.enqueue(MockResponse().setBody("""{"id":500,"name":"Nuevo"}"""))

        val album = adapter.createAlbum(newAlbum)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/albums", request.path)
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(
            setOf("name", "cover", "releaseDate", "description", "genre", "recordLabel"),
            body.keySet(),
        )
        assertEquals("1975-11-21", body["releaseDate"].asString)
        assertEquals(500, album.id)
    }

    @Test(expected = InvalidAlbumException::class)
    fun crear400_lanzaInvalidAlbum() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"statusCode":400}"""))

        adapter.createAlbum(newAlbum)
    }

    @Test(expected = HttpException::class)
    fun crear500_propagaHttpException() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        adapter.createAlbum(newAlbum)
    }

    @Test
    fun asociar_usaElRecursoSegunElTipoDeArtista() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody("{}"))
        server.enqueue(MockResponse().setResponseCode(201).setBody("{}"))

        adapter.addArtistToAlbum(500, 100, ArtistType.MUSICIAN)
        adapter.addArtistToAlbum(500, 101, ArtistType.BAND)

        assertEquals("/albums/500/musicians/100", server.takeRequest().path)
        assertEquals("/albums/500/bands/101", server.takeRequest().path)
    }
}
