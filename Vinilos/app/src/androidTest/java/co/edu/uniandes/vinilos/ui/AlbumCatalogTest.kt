package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.ui.albums.AlbumListTags
import co.edu.uniandes.vinilos.ui.common.StateTags
import co.edu.uniandes.vinilos.ui.navigation.TopLevelDestination
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** HU01 — Catálogo de álbumes (CP-01 a CP-04). */
@RunWith(AndroidJUnit4::class)
class AlbumCatalogTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun launch(repository: FakeAlbumRepository) {
        composeRule.setContent { VinilosTheme { VinilosApp(repository = repository) } }
    }

    @Test
    fun cp01_conAlbumes_muestraListaConNombreYArtista() {
        launch(FakeAlbumRepository())

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
        composeRule.onNodeWithTag(AlbumListTags.item(100)).assertIsDisplayed()
        composeRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeRule.onNodeWithText("Rubén Blades Bellido de Luna · 1984").assertIsDisplayed()
        composeRule.onNodeWithText("Poeta del pueblo").assertIsDisplayed()
    }

    @Test
    fun barraInferior_pestanaArtistas_yRegresoAAlbumes() {
        launch(FakeAlbumRepository())

        composeRule.onNodeWithTag(TopLevelDestination.ARTISTS.testTag).performClick()
        composeRule.onNodeWithText("Esta sección estará disponible en un próximo sprint.").assertIsDisplayed()

        composeRule.onNodeWithTag(TopLevelDestination.ALBUMS.testTag).performClick()
        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun cp02_sinAlbumes_muestraEstadoVacio() {
        launch(FakeAlbumRepository(albums = emptyList()))

        composeRule.onNodeWithTag(StateTags.EMPTY).assertIsDisplayed()
        composeRule.onNodeWithText("Aún no hay álbumes registrados.").assertIsDisplayed()
    }

    @Test
    fun cp03_apiInaccesible_muestraError_yReintentarCargaLaLista() {
        val repository = FakeAlbumRepository(error = IOException("sin red"))
        launch(repository)

        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()

        repository.error = null
        composeRule.onNodeWithTag(StateTags.RETRY).performClick()

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun cp04_respuestaDemorada_muestraIndicadorDeCarga() {
        val gate = CompletableDeferred<Unit>()
        launch(FakeAlbumRepository(gate = gate))

        composeRule.onNodeWithTag(StateTags.LOADING).assertIsDisplayed()

        gate.complete(Unit)
        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
    }
}
