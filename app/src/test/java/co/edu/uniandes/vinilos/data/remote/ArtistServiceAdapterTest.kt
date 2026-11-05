package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.data.remote.dto.PerformerDto
import co.edu.uniandes.vinilos.di.createArtistApiService
import co.edu.uniandes.vinilos.domain.model.ArtistNotFoundException
import co.edu.uniandes.vinilos.domain.model.ArtistType
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
class ArtistServiceAdapterTest {

    private lateinit var server: MockWebServer
    private lateinit var adapter: ArtistServiceAdapter

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        adapter = ArtistServiceAdapter(createArtistApiService(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun musico_consultaMusicians_yCompletaElPremioDesdePrizes() = runTest {
        server.enqueue(MockResponse().setBody(MUSICIAN_JSON))
        server.enqueue(MockResponse().setBody(PRIZES_JSON))

        val artist = adapter.getArtist(100, ArtistType.MUSICIAN)

        assertEquals("/musicians/100", server.takeRequest().path)
        assertEquals("/prizes", server.takeRequest().path)
        assertEquals("Rubén Blades Bellido de Luna", artist.name)
        assertEquals(ArtistType.MUSICIAN, artist.type)
        assertEquals("1948", artist.year)
        assertEquals("Buscando América", artist.albums.single().name)
        val prize = artist.prizes.single()
        assertEquals("Grammy Award", prize.name)
        assertEquals("National Academy of Recording Arts & Sciences", prize.organization)
        assertEquals("1978", prize.year)
    }

    @Test
    fun banda_consultaBands_yNoPidePremiosSiNoTiene() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"id":101,"name":"Queen","image":"","description":"Banda","creationDate":"1970-01-01T00:00:00.000Z","albums":[],"performerPrizes":[]}"""
            )
        )

        val artist = adapter.getArtist(101, ArtistType.BAND)

        assertEquals("/bands/101", server.takeRequest().path)
        assertEquals(1, server.requestCount)
        assertEquals(ArtistType.BAND, artist.type)
        assertEquals("1970", artist.year)
        assertTrue(artist.prizes.isEmpty())
    }

    @Test
    fun premiacionSinPremioAsociado_seDescarta() = runTest {
        server.enqueue(MockResponse().setBody(MUSICIAN_JSON))
        server.enqueue(MockResponse().setBody("[]"))

        assertTrue(adapter.getArtist(100, ArtistType.MUSICIAN).prizes.isEmpty())
    }

    @Test
    fun interpreteDelAlbum_esBandaSoloSiTraeCreationDate() {
        val band = PerformerDto(101, "Queen", "", creationDate = "1970-01-01T00:00:00.000Z").toDomain()
        val musician = PerformerDto(100, "Rubén Blades", "", creationDate = null).toDomain()

        assertEquals(ArtistType.BAND, band?.type)
        assertEquals(ArtistType.MUSICIAN, musician?.type)
    }

    @Test(expected = ArtistNotFoundException::class)
    fun artista404_lanzaArtistNotFound() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(404)
                .setBody("""{"statusCode":404,"message":"The musician with the given id was not found"}""")
        )

        adapter.getArtist(999, ArtistType.MUSICIAN)
    }

    @Test(expected = HttpException::class)
    fun artista500_propagaHttpException() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        adapter.getArtist(100, ArtistType.BAND)
    }

    companion object {
        private val MUSICIAN_JSON = """
            {
              "id": 100,
              "name": "Rubén Blades Bellido de Luna",
              "image": "https://example.com/ruben.jpg",
              "description": "Cantante panameño.",
              "birthDate": "1948-07-16T00:00:00.000Z",
              "albums": [
                { "id": 100, "name": "Buscando América", "cover": "", "releaseDate": "1984-08-01T00:00:00.000Z", "description": "", "genre": "Salsa", "recordLabel": "Elektra" }
              ],
              "performerPrizes": [{ "id": 100, "premiationDate": "1978-12-10T00:00:00.000Z" }]
            }
        """.trimIndent()

        private val PRIZES_JSON = """
            [
              { "id": 200, "organization": "Univisión", "name": "Premios lo Nuestro", "description": "", "performerPrizes": [{ "id": 101, "premiationDate": "1980-12-10T00:00:00.000Z" }] },
              { "id": 201, "organization": "National Academy of Recording Arts & Sciences", "name": "Grammy Award", "description": "", "performerPrizes": [{ "id": 100, "premiationDate": "1978-12-10T00:00:00.000Z" }] }
            ]
        """.trimIndent()
    }
}
