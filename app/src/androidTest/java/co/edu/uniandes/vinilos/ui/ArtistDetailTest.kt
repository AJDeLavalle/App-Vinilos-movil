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
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.FakeCollectorRepository
import co.edu.uniandes.vinilos.testutil.grantLocalNetworkPermission
import co.edu.uniandes.vinilos.ui.albums.AlbumListTags
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailTags
import co.edu.uniandes.vinilos.ui.common.StateTags
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailTags
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** HU04 — Detalle de artista (CP-12 a CP-16). Se llega desde el detalle de un álbum. */
@RunWith(AndroidJUnit4::class)
class ArtistDetailTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun launch(
        albumRepository: FakeAlbumRepository = FakeAlbumRepository(),
        artistRepository: FakeArtistRepository = FakeArtistRepository(),
    ) {
        grantLocalNetworkPermission()
        composeRule.setContent {
            VinilosTheme {
                VinilosApp(
                    repository = albumRepository,
                    artistRepository = artistRepository,
                    collectorRepository = FakeCollectorRepository(),
                )
            }
        }
    }

    private fun openArtist(artistId: Int = 1) {
        composeRule.onNodeWithTag(AlbumListTags.item(100)).performClick()
        composeRule.onNodeWithTag(AlbumDetailTags.artist(artistId)).performClick()
    }

    @Test
    fun cp12_desdeElAlbum_muestraDatosAlbumesYPremiosDelArtista() {
        launch()

        openArtist()

        composeRule.onNodeWithTag(ArtistDetailTags.TITLE).assertTextEquals("Rubén Blades Bellido de Luna")
        composeRule.onNodeWithText("Músico • Nacido en 1948").assertIsDisplayed()
        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT).performScrollToNode(hasText("Buscando América"))
        composeRule.onNodeWithTag(ArtistDetailTags.album(100)).assertIsDisplayed()
        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT).performScrollToNode(hasText("Grammy Award"))
        composeRule.onNodeWithTag(ArtistDetailTags.prize(100)).assertIsDisplayed()
        composeRule.onNodeWithText("Recording Academy · 1978").assertIsDisplayed()
    }

    @Test
    fun cp13_bandaSinAlbumesNiPremios_muestraMensajesVacios() {
        val album = FakeAlbumRepository.sampleAlbums.first()
            .copy(performers = listOf(Performer(2, "Queen", "", ArtistType.BAND)))
        launch(albumRepository = FakeAlbumRepository(albums = listOf(album)))

        openArtist(artistId = 2)

        composeRule.onNodeWithTag(ArtistDetailTags.TITLE).assertTextEquals("Queen")
        composeRule.onNodeWithText("Banda • Formada en 1970").assertIsDisplayed()
        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT)
            .performScrollToNode(hasText("Este artista no tiene álbumes registrados."))
        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT)
            .performScrollToNode(hasText("Este artista no tiene premios registrados."))
        composeRule.onNodeWithText("Este artista no tiene premios registrados.").assertIsDisplayed()
    }

    @Test
    fun cp14_artistaInexistente_informaYPermiteVolverAlAlbum() {
        launch(artistRepository = FakeArtistRepository(artists = emptyList()))

        openArtist()

        composeRule.onNodeWithText("El artista que buscas no existe.").assertIsDisplayed()
        composeRule.onNodeWithTag(StateTags.RETRY).assertTextEquals("Volver")
        composeRule.onNodeWithTag(StateTags.RETRY).performClick()

        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Buscando América")
    }

    @Test
    fun cp15_errorDeRed_muestraReintentar_yRecupera() {
        val artistRepository = FakeArtistRepository(error = IOException("sin red"))
        launch(artistRepository = artistRepository)

        openArtist()
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()

        artistRepository.error = null
        composeRule.onNodeWithTag(StateTags.RETRY).performClick()

        composeRule.onNodeWithTag(ArtistDetailTags.TITLE).assertTextEquals("Rubén Blades Bellido de Luna")
    }

    @Test
    fun cp16_seleccionarAlbumDelArtista_abreElAlbum_yAtrasRegresa() {
        launch()
        openArtist()

        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT).performScrollToNode(hasText("Buscando América"))
        composeRule.onNodeWithTag(ArtistDetailTags.album(100)).performClick()
        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertTextEquals("Buscando América")

        Espresso.pressBack()

        composeRule.onNodeWithTag(ArtistDetailTags.TITLE).assertTextEquals("Rubén Blades Bellido de Luna")
    }
}
