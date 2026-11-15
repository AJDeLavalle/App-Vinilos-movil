package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.domain.model.CollectorNotFoundException
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.FakeCollectorRepository
import co.edu.uniandes.vinilos.testutil.grantLocalNetworkPermission
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailTags
import co.edu.uniandes.vinilos.ui.collectors.CollectorDetailTags
import co.edu.uniandes.vinilos.ui.collectors.CollectorListTags
import co.edu.uniandes.vinilos.ui.common.StateTags
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailTags
import co.edu.uniandes.vinilos.ui.navigation.TopLevelDestination
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** HU05 y HU06 — Listado y detalle de coleccionistas (CP-30 a CP-36). */
@RunWith(AndroidJUnit4::class)
class CollectorTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun openCollectorsTab(repository: FakeCollectorRepository = FakeCollectorRepository()) {
        grantLocalNetworkPermission()
        composeRule.setContent {
            VinilosTheme {
                VinilosApp(
                    repository = FakeAlbumRepository(),
                    artistRepository = FakeArtistRepository(),
                    collectorRepository = repository,
                )
            }
        }
        composeRule.onNodeWithTag(TopLevelDestination.COLLECTORS.testTag).performClick()
    }

    @Test
    fun cp30_conColeccionistas_muestraNombreYCantidadDeAlbumes() {
        openCollectorsTab()

        composeRule.onNodeWithTag(CollectorListTags.LIST).assertIsDisplayed()
        composeRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeRule.onNodeWithText("1 álbum").assertIsDisplayed()
        composeRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeRule.onNodeWithText("0 álbumes").assertIsDisplayed()
    }

    @Test
    fun cp31_sinColeccionistas_muestraEstadoVacio() {
        openCollectorsTab(FakeCollectorRepository(collectors = emptyList()))

        composeRule.onNodeWithText("Aún no hay coleccionistas registrados.").assertIsDisplayed()
    }

    @Test
    fun cp32_apiInaccesible_muestraError_yReintentarCargaLaLista() {
        val repository = FakeCollectorRepository(error = IOException("sin red"))
        openCollectorsTab(repository)

        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()

        repository.error = null
        composeRule.onNodeWithTag(StateTags.RETRY).performClick()

        composeRule.onNodeWithTag(CollectorListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun cp33_seleccionarColeccionista_muestraContactoGustosYColeccion_yAtrasRegresa() {
        openCollectorsTab()

        composeRule.onNodeWithTag(CollectorListTags.item(100)).performClick()

        composeRule.onNodeWithTag(CollectorDetailTags.TITLE).assertTextEquals("Manolo Bellon")
        composeRule.onNodeWithText("Teléfono: 3502457896").assertIsDisplayed()
        composeRule.onNodeWithText("Correo: manollo@caracol.com.co").assertIsDisplayed()
        composeRule.onNodeWithTag(CollectorDetailTags.CONTENT)
            .performScrollToNode(hasText("Rubén Blades Bellido de Luna"))
        composeRule.onNodeWithTag(CollectorDetailTags.artist(1)).assertIsDisplayed()
        composeRule.onNodeWithTag(CollectorDetailTags.CONTENT).performScrollToNode(hasText("Buscando América"))
        composeRule.onNodeWithTag(CollectorDetailTags.album(100)).assertIsDisplayed()

        Espresso.pressBack()

        composeRule.onNodeWithTag(CollectorListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun cp34_sinGustosNiColeccion_muestraMensajesVacios() {
        openCollectorsTab()

        composeRule.onNodeWithTag(CollectorListTags.item(101)).performClick()

        composeRule.onNodeWithText("Este coleccionista no tiene artistas favoritos.").assertIsDisplayed()
        composeRule.onNodeWithText("Este coleccionista no tiene álbumes en su colección.").assertIsDisplayed()
    }

    @Test
    fun cp35_coleccionistaInexistente_informaYPermiteVolver() {
        openCollectorsTab(FakeCollectorRepository(detailError = CollectorNotFoundException(100)))

        composeRule.onNodeWithTag(CollectorListTags.item(100)).performClick()

        composeRule.onNodeWithText("El coleccionista que buscas no existe.").assertIsDisplayed()
        composeRule.onNodeWithTag(StateTags.RETRY).assertTextEquals("Volver")
        composeRule.onNodeWithTag(StateTags.RETRY).performClick()

        composeRule.onNodeWithTag(CollectorListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun cp36_desdeElDetalle_abreArtistaFavorito_yAlbumDeLaColeccion() {
        openCollectorsTab()
        composeRule.onNodeWithTag(CollectorListTags.item(100)).performClick()

        composeRule.onNodeWithTag(CollectorDetailTags.CONTENT)
            .performScrollToNode(hasText("Rubén Blades Bellido de Luna"))
        composeRule.onNodeWithTag(CollectorDetailTags.artist(1)).performClick()
        composeRule.onNodeWithTag(ArtistDetailTags.TITLE).assertTextEquals("Rubén Blades Bellido de Luna")

        composeRule.onNodeWithTag(ArtistDetailTags.BACK).performClick()
        composeRule.onNodeWithTag(CollectorDetailTags.CONTENT).performScrollToNode(hasText("Buscando América"))
        composeRule.onNodeWithTag(CollectorDetailTags.album(100)).performClick()

        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Buscando América")
    }
}
