package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.FakeCollectorRepository
import co.edu.uniandes.vinilos.testutil.grantLocalNetworkPermission
import co.edu.uniandes.vinilos.ui.albums.AlbumListTags
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailTags
import co.edu.uniandes.vinilos.ui.artists.ArtistListTags
import co.edu.uniandes.vinilos.ui.collectors.CollectorDetailTags
import co.edu.uniandes.vinilos.ui.create.CreateAlbumTags
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailTags
import co.edu.uniandes.vinilos.ui.navigation.Routes
import co.edu.uniandes.vinilos.ui.navigation.TopLevelDestination
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import co.edu.uniandes.vinilos.ui.tracks.AssociateTrackTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T36 (Monkey mejorado): apertura directa de pantallas con una ruta inicial y aviso de cada pantalla
 * visitada.
 */
@RunWith(AndroidJUnit4::class)
class StartRouteTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val visited = mutableListOf<String>()

    private fun openApp(startRoute: String) {
        grantLocalNetworkPermission()
        composeRule.setContent {
            VinilosTheme {
                VinilosApp(
                    repository = FakeAlbumRepository(),
                    artistRepository = FakeArtistRepository(),
                    collectorRepository = FakeCollectorRepository(),
                    startRoute = startRoute,
                    onRouteChange = { visited += it },
                )
            }
        }
    }

    @Test
    fun rutaInicialDetalleDeAlbum_abreElDetalle_yAtrasVuelveAlCatalogo() {
        openApp(Routes.albumDetail(100))

        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Buscando América")
        composeRule.runOnIdle {
            assertTrue(Routes.ALBUM_DETAIL in visited)
            assertTrue(Routes.albumDetail(100) !in visited)
        }
        Espresso.pressBack()
        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun rutaInicialDetalleDeArtista_abreElDetalle_yMarcaLaPestanaArtistas() {
        openApp(Routes.artistDetail(2, ArtistType.BAND))

        composeRule.onNodeWithTag(ArtistDetailTags.TITLE).assertTextEquals("Queen")
        composeRule.onNodeWithTag(TopLevelDestination.ARTISTS.testTag).assertIsSelected()
    }

    @Test
    fun rutaInicialDetalleDeColeccionista_abreElDetalle() {
        openApp(Routes.collectorDetail(100))

        composeRule.onNodeWithTag(CollectorDetailTags.TITLE).assertTextEquals("Manolo Bellon")
    }

    @Test
    fun rutaInicialAsociarTrack_abreElFormularioDelAlbum() {
        openApp(Routes.associateTrack(100))

        // Con substring porque el agente de accesibilidad podría cambiar el prefijo del texto.
        composeRule.onNodeWithTag(AssociateTrackTags.ALBUM)
            .assertTextContains("Buscando América", substring = true)
    }

    @Test
    fun rutaInicialInexistente_seQuedaEnElCatalogo() {
        openApp("ruta/inexistente")

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun rutaInicialPestanaArtistas_abreElListado() {
        openApp(Routes.ARTISTS_TAB)

        composeRule.onNodeWithTag(ArtistListTags.LIST).assertIsDisplayed()
        composeRule.runOnIdle { assertTrue(Routes.ARTISTS in visited) }
    }

    @Test
    fun cadaPantallaVisitada_seNotificaConSuPatronDeRuta() {
        openApp(Routes.CREATE_ALBUM)

        composeRule.onNodeWithTag(CreateAlbumTags.SAVE).assertExists()
        composeRule.runOnIdle { assertEquals(listOf(Routes.ALBUMS, Routes.CREATE_ALBUM), visited.distinct()) }
    }
}
