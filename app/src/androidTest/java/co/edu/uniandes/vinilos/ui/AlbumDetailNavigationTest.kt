package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.ui.albums.AlbumListTags
import co.edu.uniandes.vinilos.ui.common.StateTags
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailTags
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * HU02 — Detalle de álbum (CP-05, CP-06 y CP-10).
 * La UI es Compose, así que las aserciones usan Compose UI Test; el botón Atrás del sistema
 * se simula con Espresso.pressBack().
 */
@RunWith(AndroidJUnit4::class)
class AlbumDetailNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun launch(repository: FakeAlbumRepository = FakeAlbumRepository()) {
        composeRule.setContent { VinilosTheme { VinilosApp(repository = repository) } }
    }

    @Test
    fun cp05_seleccionarAlbum_muestraDetalleDelMismoAlbum() {
        launch()

        composeRule.onNodeWithTag(AlbumListTags.item(100)).performClick()

        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT).assertIsDisplayed()
        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Buscando América")
        composeRule.onNodeWithText("Rubén Blades Bellido de Luna • 1984").assertIsDisplayed()
        composeRule.onNodeWithText("Género: Salsa").assertIsDisplayed()
        composeRule.onNodeWithText("Sello discográfico: Elektra").assertIsDisplayed()
        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT).performScrollToNode(hasText("1. Decisiones"))
        composeRule.onNodeWithText("1. Decisiones").assertIsDisplayed()
    }

    @Test
    fun cp06_botonAtrasDelSistema_regresaAlCatalogo() {
        launch()
        composeRule.onNodeWithTag(AlbumListTags.item(101)).performClick()
        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Poeta del pueblo")

        Espresso.pressBack()

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
        composeRule.onNodeWithTag(AlbumListTags.item(101)).assertIsDisplayed()
    }

    @Test
    fun cp10_flujoCatalogoDetalleVolver_conBotonDeLaBarra() {
        launch()

        composeRule.onNodeWithTag(AlbumListTags.item(100)).performClick()
        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Buscando América")
        composeRule.onNodeWithTag(AlbumDetailTags.BACK).performClick()
        composeRule.onNodeWithTag(AlbumListTags.item(101)).performClick()
        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Poeta del pueblo")
        composeRule.onNodeWithTag(AlbumDetailTags.BACK).performClick()

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun detalleConErrorDeRed_muestraReintentar_yRecupera() {
        val repository = FakeAlbumRepository()
        launch(repository)
        repository.error = IOException("sin red")

        composeRule.onNodeWithTag(AlbumListTags.item(100)).performClick()
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()

        repository.error = null
        composeRule.onNodeWithTag(StateTags.RETRY).performClick()

        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Buscando América")
    }
}
