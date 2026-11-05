package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.di.createCollectorApiService
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.model.CollectorNotFoundException
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
class CollectorServiceAdapterTest {

    private lateinit var server: MockWebServer
    private lateinit var adapter: CollectorServiceAdapter

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        adapter = CollectorServiceAdapter(createCollectorApiService(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun listado_mapeaColeccionistas_yCuentaSusAlbumes() = runTest {
        server.enqueue(MockResponse().setBody("[$COLLECTOR_JSON]"))

        val collector = adapter.getCollectors().single()

        assertEquals("/collectors", server.takeRequest().path)
        assertEquals(1, server.requestCount)
        assertEquals("Manolo Bellon", collector.name)
        assertEquals(1, collector.albumCount)
        assertTrue(collector.albums.isEmpty())
    }

    @Test
    fun detalle_completaLaColeccionConSusAlbumes() = runTest {
        server.enqueue(MockResponse().setBody(COLLECTOR_JSON))
        server.enqueue(MockResponse().setBody(COLLECTOR_ALBUMS_JSON))

        val collector = adapter.getCollector(100)

        assertEquals("/collectors/100", server.takeRequest().path)
        assertEquals("/collectors/100/albums", server.takeRequest().path)
        assertEquals("3502457896", collector.telephone)
        assertEquals("Buscando América", collector.albums.single().name)
        val favorite = collector.favoritePerformers.single()
        assertEquals("Queen", favorite.name)
        assertEquals(ArtistType.BAND, favorite.type)
    }

    @Test
    fun detalle_sinAlbumes_noConsultaLaColeccion() = runTest {
        server.enqueue(MockResponse().setBody("""{"id":101,"name":"Jaime","collectorAlbums":[]}"""))

        val collector = adapter.getCollector(101)

        assertEquals(1, server.requestCount)
        assertTrue(collector.albums.isEmpty())
    }

    @Test(expected = CollectorNotFoundException::class)
    fun detalle404_lanzaCollectorNotFound() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(404)
                .setBody("""{"statusCode":404,"message":"The collector with the given id was not found"}""")
        )

        adapter.getCollector(999)
    }

    @Test(expected = HttpException::class)
    fun detalle500_propagaHttpException() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        adapter.getCollector(100)
    }

    companion object {
        private val COLLECTOR_JSON = """
            {
              "id": 100,
              "name": "Manolo Bellon",
              "telephone": "3502457896",
              "email": "manollo@caracol.com.co",
              "comments": [],
              "favoritePerformers": [{ "id": 101, "name": "Queen", "image": "", "creationDate": "1970-01-01T00:00:00.000Z" }],
              "collectorAlbums": [{ "id": 100, "price": 35, "status": "Active" }]
            }
        """.trimIndent()

        private val COLLECTOR_ALBUMS_JSON = """
            [
              {
                "id": 100,
                "price": 35,
                "status": "Active",
                "album": { "id": 100, "name": "Buscando América", "cover": "", "releaseDate": "1984-08-01T00:00:00.000Z", "description": "", "genre": "Salsa", "recordLabel": "Elektra" }
              }
            ]
        """.trimIndent()
    }
}
