package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.di.createAlbumApiService
import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

/** Pruebas de integración HTTP: respuestas con la forma real de BackVynils servidas por MockWebServer. */
class AlbumServiceAdapterTest {

    private lateinit var server: MockWebServer
    private lateinit var adapter: AlbumServiceAdapter

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
    fun getAlbums_consultaGetAlbums_yMapeaCampos() = runTest {
        server.enqueue(MockResponse().setBody(ALBUMS_JSON))

        val albums = adapter.getAlbums()

        assertEquals("/albums", server.takeRequest().path)
        assertEquals(2, albums.size)
        val first = albums.first()
        assertEquals(100, first.id)
        assertEquals("Buscando América", first.name)
        assertEquals("1984", first.releaseYear)
        assertEquals("Rubén Blades Bellido de Luna", first.performerNames)
        assertEquals("Decisiones", first.tracks.single().name)
    }

    @Test
    fun getAlbums_descartaRegistrosSinId_yToleraCamposNulos() = runTest {
        server.enqueue(MockResponse().setBody("""[{"id":null,"name":"x"},{"id":7}]"""))

        val albums = adapter.getAlbums()

        assertEquals(1, albums.size)
        assertEquals(7, albums.single().id)
        assertEquals("", albums.single().name)
        assertTrue(albums.single().tracks.isEmpty())
    }

    @Test
    fun getAlbums_listaVacia() = runTest {
        server.enqueue(MockResponse().setBody("[]"))

        assertTrue(adapter.getAlbums().isEmpty())
    }

    @Test(expected = AlbumNotFoundException::class)
    fun getAlbum_404_lanzaAlbumNotFound() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(404)
                .setBody("""{"statusCode":404,"message":"The album with the given id was not found"}""")
        )

        adapter.getAlbum(999)
    }

    @Test(expected = HttpException::class)
    fun getAlbums_500_propagaHttpException() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        adapter.getAlbums()
    }

    companion object {
        private val ALBUMS_JSON = """
            [
              {
                "id": 100,
                "name": "Buscando América",
                "cover": "https://example.com/buscando.jpg",
                "releaseDate": "1984-08-01T00:00:00.000Z",
                "description": "Primer álbum de la banda Seis del Solar.",
                "genre": "Salsa",
                "recordLabel": "Elektra",
                "tracks": [{ "id": 1, "name": "Decisiones", "duration": "5:05" }],
                "performers": [{ "id": 1, "name": "Rubén Blades Bellido de Luna", "image": "", "description": "", "birthDate": "1948-07-16T00:00:00.000Z" }],
                "comments": []
              },
              {
                "id": 101,
                "name": "Poeta del pueblo",
                "cover": "",
                "releaseDate": "1984-08-01T00:00:00.000Z",
                "description": "Recopilación",
                "genre": "Salsa",
                "recordLabel": "EMI",
                "tracks": [],
                "performers": [],
                "comments": []
              }
            ]
        """.trimIndent()
    }
}
