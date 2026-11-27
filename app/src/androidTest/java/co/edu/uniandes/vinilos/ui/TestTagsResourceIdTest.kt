package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.FakeCollectorRepository
import co.edu.uniandes.vinilos.testutil.grantLocalNetworkPermission
import co.edu.uniandes.vinilos.ui.create.AlbumField
import co.edu.uniandes.vinilos.ui.create.CreateAlbumTags
import co.edu.uniandes.vinilos.ui.navigation.Routes
import co.edu.uniandes.vinilos.ui.navigation.TopLevelDestination
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import co.edu.uniandes.vinilos.ui.tracks.AssociateTrackTags
import co.edu.uniandes.vinilos.ui.tracks.TrackField
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Los testTag se exponen como resource-id para uiautomator y Firebase Robo.
 */
@RunWith(AndroidJUnit4::class)
class TestTagsResourceIdTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private fun openApp(startRoute: String) {
        grantLocalNetworkPermission()
        composeRule.setContent {
            VinilosTheme {
                VinilosApp(
                    repository = FakeAlbumRepository(),
                    artistRepository = FakeArtistRepository(),
                    collectorRepository = FakeCollectorRepository(),
                    startRoute = startRoute,
                )
            }
        }
    }

    private fun assertResourceId(id: String) {
        assertTrue("No hay un elemento con resource-id $id", device.wait(Until.hasObject(By.res(id)), TIMEOUT_MS))
    }

    @Test
    fun crearAlbum_exponeCamposYBotonComoResourceId() {
        openApp(Routes.CREATE_ALBUM)

        assertResourceId(CreateAlbumTags.field(AlbumField.NAME))
        composeRule.onNodeWithTag(CreateAlbumTags.SAVE).performScrollTo()
        assertResourceId(CreateAlbumTags.SAVE)
    }

    @Test
    fun asociarTrack_exponeCamposYBotonComoResourceId() {
        openApp(Routes.associateTrack(100))

        assertResourceId(AssociateTrackTags.field(TrackField.NAME))
        composeRule.onNodeWithTag(AssociateTrackTags.SAVE).performScrollTo()
        assertResourceId(AssociateTrackTags.SAVE)
    }

    @Test
    fun barraDeNavegacion_exponeSusPestanasComoResourceId() {
        openApp(Routes.ALBUMS_TAB)

        TopLevelDestination.entries.forEach { assertResourceId(it.testTag) }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
