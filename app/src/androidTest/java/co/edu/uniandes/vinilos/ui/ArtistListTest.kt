package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.grantLocalNetworkPermission
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailTags
import co.edu.uniandes.vinilos.ui.artists.ArtistListTags
import co.edu.uniandes.vinilos.ui.common.StateTags
import co.edu.uniandes.vinilos.ui.navigation.TopLevelDestination
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** HU03 — Listado de artistas (CP-17 a CP-20). */
@RunWith(AndroidJUnit4::class)
class ArtistListTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun openArtistsTab(repository: FakeArtistRepository = FakeArtistRepository()) {
        grantLocalNetworkPermission()
        composeRule.setContent {
            VinilosTheme { VinilosApp(repository = FakeAlbumRepository(), artistRepository = repository) }
        }
        composeRule.onNodeWithTag(TopLevelDestination.ARTISTS.testTag).performClick()
    }

    @Test
    fun cp17_conArtistas_muestraMusicosYBandas() {
        openArtistsTab()

        composeRule.onNodeWithTag(ArtistListTags.LIST).assertIsDisplayed()
        composeRule.onNodeWithText("Rubén Blades Bellido de Luna").assertIsDisplayed()
        composeRule.onNodeWithText("Músico · 1948").assertIsDisplayed()
        composeRule.onNodeWithText("Queen").assertIsDisplayed()
        composeRule.onNodeWithText("Banda · 1970").assertIsDisplayed()
    }

    @Test
    fun cp18_sinArtistas_muestraEstadoVacio() {
        openArtistsTab(FakeArtistRepository(artists = emptyList()))

        composeRule.onNodeWithTag(StateTags.EMPTY).assertIsDisplayed()
        composeRule.onNodeWithText("Aún no hay artistas registrados.").assertIsDisplayed()
    }

    @Test
    fun cp19_apiInaccesible_muestraError_yReintentarCargaLaLista() {
        val repository = FakeArtistRepository(error = IOException("sin red"))
        openArtistsTab(repository)

        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()

        repository.error = null
        composeRule.onNodeWithTag(StateTags.RETRY).performClick()

        composeRule.onNodeWithTag(ArtistListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun cp20_seleccionarBanda_abreSuDetalle_yAtrasRegresaAlListado() {
        openArtistsTab()

        composeRule.onNodeWithTag(ArtistListTags.item(2)).performClick()
        composeRule.onNodeWithTag(ArtistDetailTags.TITLE).assertTextEquals("Queen")

        Espresso.pressBack()

        composeRule.onNodeWithTag(ArtistListTags.LIST).assertIsDisplayed()
    }
}
