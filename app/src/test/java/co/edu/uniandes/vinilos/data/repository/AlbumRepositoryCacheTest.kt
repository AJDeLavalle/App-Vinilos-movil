package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.remote.AlbumServiceAdapter
import co.edu.uniandes.vinilos.di.createAlbumApiService
import co.edu.uniandes.vinilos.domain.model.NewTrack
import co.edu.uniandes.vinilos.testutil.respondByPath
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** La caché del repositorio contra un backend simulado: cuenta las peticiones reales. */
class AlbumRepositoryCacheTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: AlbumRepositoryImpl
    private var albumsChanged = 0

    @Before
    fun setUp() {
        server = MockWebServer()
        server.respondByPath(
            "/albums" to MockResponse().setBody("""[{"id":100,"name":"Buscando América"}]"""),
            "/albums/100" to MockResponse().setBody("""{"id":100,"name":"Buscando América","tracks":[]}"""),
            "/albums/100/tracks" to MockResponse().setBody("""{"id":7,"name":"Decisiones","duration":"5:05"}"""),
            "/albums/100/musicians/1" to MockResponse().setBody("{}"),
        )
        server.start()
        repository = AlbumRepositoryImpl(
            AlbumServiceAdapter(createAlbumApiService(server.url("/").toString())),
            ioDispatcher = UnconfinedTestDispatcher(),
            onAlbumsChanged = { albumsChanged++ },
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun leerDosVeces_consultaElBackendUnaSolaVez() = runTest {
        repository.getAlbums()
        repository.getAlbums()
        repository.getAlbum(100)
        repository.getAlbum(100)

        assertEquals(2, server.requestCount)
    }

    @Test
    fun asociarTrack_invalidaElDetalleYElCatalogo() = runTest {
        repository.getAlbums()
        repository.getAlbum(100)

        repository.addTrack(100, NewTrack(name = "Decisiones", duration = "5:05"))
        repository.getAlbum(100)
        repository.getAlbums()

        // 2 lecturas + POST + 2 lecturas nuevas
        assertEquals(5, server.requestCount)
    }

    @Test
    fun asociarArtista_avisaALosDemasRepositorios() = runTest {
        repository.getAlbums()

        repository.addArtistToAlbum(100, 1, co.edu.uniandes.vinilos.domain.model.ArtistType.MUSICIAN)
        repository.getAlbums()

        assertEquals(1, albumsChanged)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun clearCache_obligaAConsultarDeNuevo() = runTest {
        repository.getAlbum(100)
        repository.clearCache()
        repository.getAlbum(100)

        assertEquals(2, server.requestCount)
    }
}
