package co.edu.uniandes.vinilos.ui

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.FakeAlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeArtistRepository
import co.edu.uniandes.vinilos.testutil.FakeCollectorRepository
import co.edu.uniandes.vinilos.testutil.enableVinilosAccessibilityChecks
import co.edu.uniandes.vinilos.testutil.grantLocalNetworkPermission
import co.edu.uniandes.vinilos.ui.albums.AlbumListContent
import co.edu.uniandes.vinilos.ui.albums.AlbumListTags
import co.edu.uniandes.vinilos.ui.albums.AlbumListUiState
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailContent
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailTags
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailUiState
import co.edu.uniandes.vinilos.ui.artists.ArtistListContent
import co.edu.uniandes.vinilos.ui.artists.ArtistListTags
import co.edu.uniandes.vinilos.ui.artists.ArtistListUiState
import co.edu.uniandes.vinilos.ui.collectors.CollectorDetailContent
import co.edu.uniandes.vinilos.ui.collectors.CollectorDetailTags
import co.edu.uniandes.vinilos.ui.collectors.CollectorDetailUiState
import co.edu.uniandes.vinilos.ui.collectors.CollectorListContent
import co.edu.uniandes.vinilos.ui.collectors.CollectorListTags
import co.edu.uniandes.vinilos.ui.collectors.CollectorListUiState
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.StateTags
import co.edu.uniandes.vinilos.ui.create.AlbumField
import co.edu.uniandes.vinilos.ui.create.AlbumFieldError
import co.edu.uniandes.vinilos.ui.create.ArtistOptions
import co.edu.uniandes.vinilos.ui.create.CreateAlbumActions
import co.edu.uniandes.vinilos.ui.create.CreateAlbumContent
import co.edu.uniandes.vinilos.ui.create.CreateAlbumTags
import co.edu.uniandes.vinilos.ui.create.CreateAlbumUiState
import co.edu.uniandes.vinilos.ui.create.Submission
import co.edu.uniandes.vinilos.ui.create.SubmitError
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailContent
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailTags
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailUiState
import co.edu.uniandes.vinilos.ui.navigation.TopLevelDestination
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme
import co.edu.uniandes.vinilos.ui.tracks.AlbumLoad
import co.edu.uniandes.vinilos.ui.tracks.AssociateTrackContent
import co.edu.uniandes.vinilos.ui.tracks.AssociateTrackTags
import co.edu.uniandes.vinilos.ui.tracks.AssociateTrackUiState
import co.edu.uniandes.vinilos.ui.tracks.TrackField
import co.edu.uniandes.vinilos.ui.tracks.TrackFieldError
import co.edu.uniandes.vinilos.ui.tracks.TrackSubmission
import co.edu.uniandes.vinilos.ui.tracks.TrackSubmitError
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityViewCheckException
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityChecksTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val emptyCreateActions = CreateAlbumActions(
        onNameChange = {},
        onArtistChange = {},
        onGenreChange = {},
        onRecordLabelChange = {},
        onReleaseDateChange = {},
        onCoverChange = {},
        onDescriptionChange = {},
        onFieldBlur = {},
        onFocusHandled = {},
        onRetryArtists = {},
        onSave = {},
        onBack = {},
        onFinish = {},
    )

    @Test
    fun pantalla01_catalogoDeAlbumes_todosLosEstados() {
        composeRule.enableVinilosAccessibilityChecks()
        var state: AlbumListUiState by mutableStateOf(AlbumListUiState.Loading)
        composeRule.setContent {
            VinilosTheme {
                AlbumListContent(state = state, onRetry = {}, onAlbumClick = {}, onCreateAlbum = {})
            }
        }

        composeRule.onNodeWithTag(StateTags.LOADING).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AlbumListUiState.Empty }
        composeRule.onNodeWithTag(StateTags.EMPTY).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AlbumListUiState.Error(ErrorType.NETWORK) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AlbumListUiState.Success(FakeAlbumRepository.sampleAlbums) }
        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun pantalla02_detalleDeAlbum_todosLosEstados() {
        composeRule.enableVinilosAccessibilityChecks()
        var state: AlbumDetailUiState by mutableStateOf(AlbumDetailUiState.Loading)
        composeRule.setContent {
            VinilosTheme {
                AlbumDetailContent(
                    state = state,
                    onRetry = {},
                    onBack = {},
                    onArtistClick = {},
                    onAddTrack = {},
                )
            }
        }

        composeRule.onNodeWithTag(StateTags.LOADING).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AlbumDetailUiState.Error(ErrorType.NETWORK) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AlbumDetailUiState.Error(ErrorType.NOT_FOUND) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AlbumDetailUiState.Success(FakeAlbumRepository.sampleAlbums[0]) }
        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(AlbumDetailTags.comment(1)))
        composeRule.onNodeWithTag(AlbumDetailTags.comment(1)).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun pantalla03_listadoDeArtistas_todosLosEstados() {
        composeRule.enableVinilosAccessibilityChecks()
        var state: ArtistListUiState by mutableStateOf(ArtistListUiState.Loading)
        composeRule.setContent {
            VinilosTheme {
                ArtistListContent(state = state, onRetry = {}, onArtistClick = {})
            }
        }

        composeRule.onNodeWithTag(StateTags.LOADING).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = ArtistListUiState.Empty }
        composeRule.onNodeWithTag(StateTags.EMPTY).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = ArtistListUiState.Error(ErrorType.NETWORK) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = ArtistListUiState.Success(FakeArtistRepository.sampleArtists) }
        composeRule.onNodeWithTag(ArtistListTags.LIST).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun pantalla04_detalleDeArtista_todosLosEstados() {
        composeRule.enableVinilosAccessibilityChecks()
        var state: ArtistDetailUiState by mutableStateOf(ArtistDetailUiState.Loading)
        composeRule.setContent {
            VinilosTheme {
                ArtistDetailContent(state = state, onRetry = {}, onBack = {}, onAlbumClick = {})
            }
        }

        composeRule.onNodeWithTag(StateTags.LOADING).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = ArtistDetailUiState.Error(ErrorType.NETWORK) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = ArtistDetailUiState.Error(ErrorType.NOT_FOUND) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = ArtistDetailUiState.Success(FakeArtistRepository.sampleArtists[0]) }
        composeRule.onNodeWithTag(ArtistDetailTags.TITLE).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(ArtistDetailTags.prize(100)))
        composeRule.onNodeWithTag(ArtistDetailTags.prize(100)).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun pantalla05_listadoDeColeccionistas_todosLosEstados() {
        composeRule.enableVinilosAccessibilityChecks()
        var state: CollectorListUiState by mutableStateOf(CollectorListUiState.Loading)
        composeRule.setContent {
            VinilosTheme {
                CollectorListContent(state = state, onRetry = {}, onCollectorClick = {})
            }
        }

        composeRule.onNodeWithTag(StateTags.LOADING).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = CollectorListUiState.Empty }
        composeRule.onNodeWithTag(StateTags.EMPTY).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = CollectorListUiState.Error(ErrorType.NETWORK) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = CollectorListUiState.Success(FakeCollectorRepository.sampleCollectors) }
        composeRule.onNodeWithTag(CollectorListTags.LIST).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun pantalla06_detalleDeColeccionista_todosLosEstados() {
        composeRule.enableVinilosAccessibilityChecks()
        var state: CollectorDetailUiState by mutableStateOf(CollectorDetailUiState.Loading)
        composeRule.setContent {
            VinilosTheme {
                CollectorDetailContent(
                    state = state,
                    onRetry = {},
                    onBack = {},
                    onArtistClick = {},
                    onAlbumClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(StateTags.LOADING).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = CollectorDetailUiState.Error(ErrorType.NETWORK) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = CollectorDetailUiState.Error(ErrorType.NOT_FOUND) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread {
            state = CollectorDetailUiState.Success(FakeCollectorRepository.sampleCollectors[0])
        }
        composeRule.onNodeWithTag(CollectorDetailTags.TITLE).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.onNodeWithTag(CollectorDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(CollectorDetailTags.album(100)))
        composeRule.onNodeWithTag(CollectorDetailTags.album(100)).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun pantalla07_crearAlbum_todosLosEstados() {
        composeRule.enableVinilosAccessibilityChecks()
        val loadedArtists = ArtistOptions.Loaded(FakeArtistRepository.sampleArtists)
        var state by mutableStateOf(CreateAlbumUiState(artistOptions = loadedArtists))
        composeRule.setContent {
            VinilosTheme {
                CreateAlbumContent(state = state, actions = emptyCreateActions)
            }
        }

        composeRule.onNodeWithTag(CreateAlbumTags.field(AlbumField.NAME)).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = CreateAlbumUiState(artistOptions = ArtistOptions.Loading) }
        composeRule.onNodeWithText("Cargando artistas…").assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = CreateAlbumUiState(artistOptions = ArtistOptions.Error) }
        composeRule.onNodeWithTag(CreateAlbumTags.ARTISTS_RETRY).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread {
            state = CreateAlbumUiState(
                artistOptions = loadedArtists,
                errors = mapOf(
                    AlbumField.NAME to AlbumFieldError.NAME_REQUIRED,
                    AlbumField.ARTIST to AlbumFieldError.ARTIST_REQUIRED,
                    AlbumField.GENRE to AlbumFieldError.GENRE_REQUIRED,
                    AlbumField.RECORD_LABEL to AlbumFieldError.RECORD_LABEL_REQUIRED,
                    AlbumField.RELEASE_DATE to AlbumFieldError.DATE_INVALID,
                    AlbumField.COVER to AlbumFieldError.COVER_REQUIRED,
                    AlbumField.DESCRIPTION to AlbumFieldError.DESCRIPTION_REQUIRED,
                ),
            )
        }
        composeRule.onNodeWithTag(CreateAlbumTags.field(AlbumField.NAME)).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
        composeRule.onNodeWithTag(CreateAlbumTags.SAVE).performScrollTo().assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread {
            state = CreateAlbumUiState(artistOptions = loadedArtists, submission = Submission.Sending)
        }
        composeRule.onNodeWithTag(CreateAlbumTags.SAVE).performScrollTo().assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread {
            state = CreateAlbumUiState(
                artistOptions = loadedArtists,
                submission = Submission.Failed(SubmitError.NETWORK),
            )
        }
        composeRule.onNodeWithTag(CreateAlbumTags.SUBMIT_ERROR).performScrollTo().assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread {
            state = CreateAlbumUiState(
                artistOptions = loadedArtists,
                albumCreated = true,
                submission = Submission.Failed(SubmitError.ARTIST_LINK),
            )
        }
        composeRule.onNodeWithTag(CreateAlbumTags.BACK_TO_CATALOG).performScrollTo().assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun pantalla08_asociarTrack_todosLosEstados() {
        composeRule.enableVinilosAccessibilityChecks()
        val loadedAlbum = AlbumLoad.Loaded(FakeAlbumRepository.sampleAlbums[0])
        var state by mutableStateOf(AssociateTrackUiState(album = AlbumLoad.Loading))
        composeRule.setContent {
            VinilosTheme {
                AssociateTrackContent(
                    state = state,
                    onNameChange = {},
                    onDurationChange = {},
                    onFieldBlur = {},
                    onFocusHandled = {},
                    onRetryAlbum = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithTag(StateTags.LOADING).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AssociateTrackUiState(album = AlbumLoad.Error(ErrorType.NETWORK)) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AssociateTrackUiState(album = AlbumLoad.Error(ErrorType.NOT_FOUND)) }
        composeRule.onNodeWithTag(StateTags.ERROR).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread { state = AssociateTrackUiState(album = loadedAlbum) }
        composeRule.onNodeWithTag(AssociateTrackTags.ALBUM).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread {
            state = AssociateTrackUiState(
                album = loadedAlbum,
                errors = mapOf(
                    TrackField.NAME to TrackFieldError.NAME_REQUIRED,
                    TrackField.DURATION to TrackFieldError.DURATION_INVALID,
                ),
            )
        }
        composeRule.onNodeWithTag(AssociateTrackTags.ALBUM).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
        composeRule.onNodeWithTag(AssociateTrackTags.SAVE).performScrollTo().assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread {
            state = AssociateTrackUiState(album = loadedAlbum, submission = TrackSubmission.Sending)
        }
        composeRule.onNodeWithTag(AssociateTrackTags.SAVE).performScrollTo().assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.runOnUiThread {
            state = AssociateTrackUiState(
                album = loadedAlbum,
                submission = TrackSubmission.Failed(TrackSubmitError.NETWORK),
            )
        }
        composeRule.onNodeWithTag(AssociateTrackTags.SUBMIT_ERROR).performScrollTo().assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun navegacionBarraInferior_todasLasPestanas() {
        grantLocalNetworkPermission()
        composeRule.enableVinilosAccessibilityChecks()
        composeRule.setContent {
            VinilosTheme {
                VinilosApp(
                    repository = FakeAlbumRepository(),
                    artistRepository = FakeArtistRepository(),
                    collectorRepository = FakeCollectorRepository(),
                )
            }
        }

        composeRule.onNodeWithTag(AlbumListTags.LIST).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.onNodeWithTag(TopLevelDestination.ARTISTS.testTag).performClick()
        composeRule.onNodeWithTag(ArtistListTags.LIST).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()

        composeRule.onNodeWithTag(TopLevelDestination.COLLECTORS.testTag).performClick()
        composeRule.onNodeWithTag(CollectorListTags.LIST).assertIsDisplayed()
        composeRule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun canario_elementoInteractivoSinEtiqueta_fallaConAtf() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        composeRule.enableVinilosAccessibilityChecks()
        composeRule.setContent {
            VinilosTheme {
                Box(Modifier.size(48.dp).clickable {})
            }
        }

        val failure = runCatching { composeRule.onRoot().tryPerformAccessibilityChecks() }.exceptionOrNull()
        val hasAtfError = failure != null &&
            generateSequence(failure) { it.cause }.any { it is AccessibilityViewCheckException }
        assertTrue("Se esperaba AccessibilityViewCheckException, se obtuvo: $failure", hasAtfError)
    }
}
