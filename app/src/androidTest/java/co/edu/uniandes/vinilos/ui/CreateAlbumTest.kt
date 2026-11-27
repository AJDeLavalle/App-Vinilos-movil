package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.FakeCollectorRepository
import co.edu.uniandes.vinilos.testutil.enableVinilosAccessibilityChecks
import co.edu.uniandes.vinilos.testutil.grantLocalNetworkPermission
import co.edu.uniandes.vinilos.ui.albums.AlbumListTags
import co.edu.uniandes.vinilos.ui.create.AlbumField
import co.edu.uniandes.vinilos.ui.create.CreateAlbumTags
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesElements
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement
import kotlinx.coroutines.CompletableDeferred
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** HU07 — Crear álbum (CP-21 a CP-29 de docs/diseno/sprint-2/crear-album.md, y CP-37). */
@RunWith(AndroidJUnit4::class)
class CreateAlbumTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val formFieldTags = AlbumField.entries.map(CreateAlbumTags::field).toSet()

    private val imeClippedFieldMatcher = object : TypeSafeMatcher<ViewHierarchyElement>() {
        override fun describeTo(description: Description) {
            description.appendText("EditText de CreateAlbumScreen recortado por el teclado sin hijos visibles")
        }

        override fun matchesSafely(item: ViewHierarchyElement): Boolean =
            item.className?.toString() == "android.widget.EditText" &&
                item.isEditable == true &&
                item.childViewCount == 0 &&
                item.testTag?.toString() in formFieldTags
    }

    private val suppressImeClippedField: AccessibilityValidator.() -> Unit = {
        setSuppressingResultMatcher(
            allOf(
                matchesCheck(SpeakableTextPresentCheck::class.java),
                matchesElements(imeClippedFieldMatcher),
            ),
        )
    }

    private fun openForm(
        albums: FakeAlbumRepository = FakeAlbumRepository(),
        artists: FakeArtistRepository = FakeArtistRepository(),
        configure: AccessibilityValidator.() -> Unit = {},
    ) {
        grantLocalNetworkPermission()
        composeRule.enableVinilosAccessibilityChecks(configure)
        composeRule.setContent {
            VinilosTheme {
                VinilosApp(
                    repository = albums,
                    artistRepository = artists,
                    collectorRepository = FakeCollectorRepository(),
                )
            }
        }
        composeRule.onNodeWithTag(AlbumListTags.CREATE).performClick()
    }

    private fun type(field: AlbumField, text: String) {
        composeRule.onNodeWithTag(CreateAlbumTags.field(field)).performScrollTo().performTextInput(text)
    }

    private fun select(field: AlbumField, option: String) {
        composeRule.onNodeWithTag(CreateAlbumTags.field(field)).performScrollTo().performClick()
        composeRule.onNodeWithText(option).performClick()
    }

    private fun fillValidForm(artist: String = "Queen") {
        type(AlbumField.NAME, "Nuevo álbum")
        select(AlbumField.ARTIST, artist)
        select(AlbumField.GENRE, "Rock")
        select(AlbumField.RECORD_LABEL, "EMI")
        type(AlbumField.RELEASE_DATE, "21/11/1975")
        type(AlbumField.COVER, "https://example.com/a.jpg")
        type(AlbumField.DESCRIPTION, "Descripción del álbum")
    }

    // Por acción semántica y no por coordenadas: con el teclado abierto la pantalla se reacomoda
    // y performClick puede caer fuera del botón.
    private fun save() {
        composeRule.onNodeWithTag(CreateAlbumTags.SAVE)
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick)
    }

    private fun assertFieldError(field: AlbumField, message: String) {
        composeRule.onNodeWithTag(CreateAlbumTags.error(field), useUnmergedTree = true)
            .performScrollTo()
            .assertTextContains(message)
    }

    @Test
    fun cp21_guardarVacio_muestraUnErrorPorCampo_yNoEnvia() {
        val albums = FakeAlbumRepository()
        openForm(albums, configure = suppressImeClippedField)

        save()

        assertFieldError(AlbumField.NAME, "Ingresa el nombre del álbum.")
        assertFieldError(AlbumField.ARTIST, "Selecciona un artista.")
        assertFieldError(AlbumField.GENRE, "Selecciona un género.")
        assertFieldError(AlbumField.RECORD_LABEL, "Selecciona un sello discográfico.")
        assertFieldError(AlbumField.RELEASE_DATE, "Ingresa una fecha válida (DD/MM/AAAA).")
        assertFieldError(AlbumField.COVER, "Ingresa la URL de la portada.")
        assertFieldError(AlbumField.DESCRIPTION, "Ingresa una descripción.")
        assertEquals(0, albums.createCalls)
    }

    @Test
    fun cp22_fechaInexistente_muestraError() {
        openForm(configure = suppressImeClippedField)
        type(AlbumField.RELEASE_DATE, "31/02/2020")

        save()

        assertFieldError(AlbumField.RELEASE_DATE, "Ingresa una fecha válida (DD/MM/AAAA).")
    }

    @Test
    fun cp23_fechaFutura_muestraError() {
        openForm(configure = suppressImeClippedField)
        type(AlbumField.RELEASE_DATE, "01/01/2999")

        save()

        assertFieldError(AlbumField.RELEASE_DATE, "La fecha no puede ser futura.")
    }

    @Test
    fun cp24_urlSinHttp_muestraError_yAlCorregirDesaparece() {
        openForm(configure = suppressImeClippedField)
        type(AlbumField.COVER, "portada.jpg")

        save()
        assertFieldError(AlbumField.COVER, "Ingresa una URL válida.")

        composeRule.onNodeWithTag(CreateAlbumTags.field(AlbumField.COVER)).performTextClearance()
        type(AlbumField.COVER, "https://example.com/a.jpg")

        composeRule.onNodeWithTag(CreateAlbumTags.error(AlbumField.COVER), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun cp25_musico_creaElAlbum_loAsocia_yApareceEnElCatalogo() {
        val albums = FakeAlbumRepository()
        openForm(albums, configure = suppressImeClippedField)
        fillValidForm(artist = "Rubén Blades Bellido de Luna")

        save()

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
        composeRule.onNodeWithText("Nuevo álbum").assertIsDisplayed()
        assertEquals(listOf(Triple(500, 1, ArtistType.MUSICIAN)), albums.linkCalls)
    }

    @Test
    fun cp26_banda_asociaConElRecursoDeBandas() {
        val albums = FakeAlbumRepository()
        openForm(albums, configure = suppressImeClippedField)
        fillValidForm(artist = "Queen")

        save()

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
        assertEquals(listOf(Triple(500, 2, ArtistType.BAND)), albums.linkCalls)
    }

    @Test
    fun cp27_falloDeRed_muestraError_yConservaLosDatos() {
        val albums = FakeAlbumRepository(createError = IOException("sin red"))
        openForm(albums, configure = suppressImeClippedField)
        fillValidForm()

        save()

        composeRule.onNodeWithTag(CreateAlbumTags.SUBMIT_ERROR).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(CreateAlbumTags.field(AlbumField.NAME)).assertTextContains("Nuevo álbum")

        albums.createError = null
        save()

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
    }

    @Test
    fun cp28_falloAlAsociar_reintentarNoDuplicaElAlbum() {
        val albums = FakeAlbumRepository(linkError = IOException("sin red"))
        openForm(albums, configure = suppressImeClippedField)
        fillValidForm()

        save()

        composeRule.onNodeWithText("El álbum se creó, pero no pudimos asociar el artista.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag(CreateAlbumTags.BACK_TO_CATALOG).performScrollTo().assertIsDisplayed()

        albums.linkError = null
        save()

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
        assertEquals(1, albums.createCalls)
        assertEquals(2, albums.linkCalls.size)
    }

    @Test
    fun cp29_mientrasEnvia_elBotonSeDeshabilita_ySoloHayUnaSolicitud() {
        val gate = CompletableDeferred<Unit>()
        val albums = FakeAlbumRepository(createGate = gate)
        openForm(albums, configure = suppressImeClippedField)
        fillValidForm()

        save()

        composeRule.onNodeWithTag(CreateAlbumTags.SAVE).assertIsNotEnabled()
        composeRule.onNodeWithTag(CreateAlbumTags.SAVE).performClick()

        gate.complete(Unit)
        composeRule.waitUntil(5_000) { albums.linkCalls.isNotEmpty() }
        assertEquals(1, albums.createCalls)
    }

    @Test
    fun cp37_sinArtistas_guardarNoCierraLaApp_yReintentarLosCarga() {
        val artists = FakeArtistRepository(error = IOException("sin red"))
        val albums = FakeAlbumRepository()
        openForm(albums, artists, configure = suppressImeClippedField)

        save()

        composeRule.onNodeWithText("No pudimos cargar los artistas.").performScrollTo().assertIsDisplayed()
        assertEquals(0, albums.createCalls)

        artists.error = null
        composeRule.onNodeWithTag(CreateAlbumTags.ARTISTS_RETRY).performScrollTo().performClick()

        composeRule.onNodeWithTag(CreateAlbumTags.field(AlbumField.ARTIST)).performScrollTo().assertIsDisplayed()
    }
}
