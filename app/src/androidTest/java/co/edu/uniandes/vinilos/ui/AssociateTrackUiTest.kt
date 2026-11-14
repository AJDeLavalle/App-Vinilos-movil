package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.FakeCollectorRepository
import co.edu.uniandes.vinilos.testutil.grantLocalNetworkPermission
import co.edu.uniandes.vinilos.ui.albums.AlbumListTags
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailTags
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import co.edu.uniandes.vinilos.ui.tracks.AssociateTrackTags
import co.edu.uniandes.vinilos.ui.tracks.TrackField
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** HU08 — Asociar tracks (CP-38 a CP-43). */
@RunWith(AndroidJUnit4::class)
class AssociateTrackUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun openForm(albums: FakeAlbumRepository = FakeAlbumRepository()) {
        grantLocalNetworkPermission()
        composeRule.setContent {
            VinilosTheme {
                VinilosApp(
                    repository = albums,
                    artistRepository = FakeArtistRepository(),
                    collectorRepository = FakeCollectorRepository(),
                )
            }
        }
        composeRule.onNodeWithTag(AlbumListTags.item(100)).performClick()
        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT).performScrollToNode(hasTestTag(AlbumDetailTags.ADD_TRACK))
        composeRule.onNodeWithTag(AlbumDetailTags.ADD_TRACK).performClick()
    }

    private fun type(field: TrackField, text: String) {
        composeRule.onNodeWithTag(AssociateTrackTags.field(field)).performScrollTo().performTextInput(text)
    }

    // Por acción semántica: con el teclado abierto, performClick puede caer fuera del botón.
    private fun save() {
        composeRule.onNodeWithTag(AssociateTrackTags.SAVE)
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick)
    }

    private fun assertFieldError(field: TrackField, message: String) {
        composeRule.onNodeWithTag(AssociateTrackTags.error(field), useUnmergedTree = true)
            .assertTextContains(message)
    }

    @Test
    fun cp38_desdeElDetalle_muestraElAlbumYSusTracksExistentes() {
        openForm()

        composeRule.onNodeWithTag(AssociateTrackTags.ALBUM)
            .assertTextEquals("Álbum seleccionado: Buscando América")
        composeRule.onNodeWithText("Decisiones").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("6:29").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun cp39_guardarVacio_muestraErrores_yNoEnvia() {
        val albums = FakeAlbumRepository()
        openForm(albums)

        save()

        assertFieldError(TrackField.NAME, "Ingresa el nombre del track.")
        assertFieldError(TrackField.DURATION, "Ingresa la duración.")
        assertEquals(0, albums.addTrackCalls)
    }

    @Test
    fun cp40_duracionInvalida_muestraError() {
        openForm()
        type(TrackField.NAME, "Nuevo track")
        type(TrackField.DURATION, "5:99")

        save()

        assertFieldError(TrackField.DURATION, "Ingresa una duración válida (mm:ss).")
    }

    @Test
    fun cp41_datosValidos_regresaAlDetalle_yMuestraElTrackNuevo() {
        openForm()
        type(TrackField.NAME, "Nuevo track")
        type(TrackField.DURATION, "3:45")

        save()

        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT).performScrollToNode(hasText("3. Nuevo track"))
        composeRule.onNodeWithText("3. Nuevo track").assertIsDisplayed()
        composeRule.onNodeWithText("3:45").assertIsDisplayed()
    }

    @Test
    fun cp42_falloDeRed_muestraError_yConservaLosDatos() {
        val albums = FakeAlbumRepository(trackError = IOException("sin red"))
        openForm(albums)
        type(TrackField.NAME, "Nuevo track")
        type(TrackField.DURATION, "3:45")

        save()

        composeRule.onNodeWithTag(AssociateTrackTags.SUBMIT_ERROR).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(AssociateTrackTags.field(TrackField.NAME)).assertTextContains("Nuevo track")

        albums.trackError = null
        save()

        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT).performScrollToNode(hasText("3. Nuevo track"))
    }

    @Test
    fun cp43_mientrasEnvia_elBotonSeDeshabilita_ySoloHayUnaSolicitud() {
        val gate = CompletableDeferred<Unit>()
        val albums = FakeAlbumRepository(trackGate = gate)
        openForm(albums)
        type(TrackField.NAME, "Nuevo track")
        type(TrackField.DURATION, "3:45")

        save()

        composeRule.onNodeWithTag(AssociateTrackTags.SAVE).assertIsNotEnabled()

        gate.complete(Unit)
        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT).performScrollToNode(hasText("3. Nuevo track"))
        assertEquals(1, albums.addTrackCalls)
    }
}
