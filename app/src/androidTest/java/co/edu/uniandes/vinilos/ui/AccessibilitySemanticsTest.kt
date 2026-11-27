package co.edu.uniandes.vinilos.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
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
import co.edu.uniandes.vinilos.ui.tracks.TrackSubmission
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilitySemanticsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeRule.enableVinilosAccessibilityChecks()
    }

    @Test
    fun catalogo_tituloEsEncabezado_yTarjetasUnicasConAccion() {
        composeRule.setContent {
            VinilosTheme {
                AlbumListContent(
                    state = AlbumListUiState.Success(FakeAlbumRepository.sampleAlbums),
                    onRetry = {},
                    onAlbumClick = {},
                    onCreateAlbum = {},
                )
            }
        }

        composeRule.onNode(hasText("Álbumes") and isHeading()).assertExists()
        composeRule.onAllNodesWithContentDescription("Portada de Buscando América").assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("Portada de Poeta del pueblo").assertCountEquals(1)
        composeRule.onNodeWithTag(AlbumListTags.item(100))
            .assert(hasText("Buscando América"))
            .assert(onClickLabel("ver detalles del álbum"))
        composeRule.onAllNodesWithText("Ver detalles ›").assertCountEquals(0)
    }

    @Test
    fun artistas_tituloEsEncabezado_yTarjetasUnicasConAccion() {
        composeRule.setContent {
            VinilosTheme {
                ArtistListContent(
                    state = ArtistListUiState.Success(FakeArtistRepository.sampleArtists),
                    onRetry = {},
                    onArtistClick = {},
                )
            }
        }

        composeRule.onNode(hasText("Artistas") and isHeading()).assertExists()
        composeRule.onAllNodesWithContentDescription("Imagen de Rubén Blades Bellido de Luna").assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("Imagen de Queen").assertCountEquals(1)
        composeRule.onNodeWithTag(ArtistListTags.item(1))
            .assert(hasText("Rubén Blades Bellido de Luna"))
            .assert(onClickLabel("ver detalles del artista"))
        composeRule.onAllNodesWithText("Ver detalles ›").assertCountEquals(0)
    }

    @Test
    fun coleccionistas_tituloEsEncabezado_yTarjetasConAccion() {
        composeRule.setContent {
            VinilosTheme {
                CollectorListContent(
                    state = CollectorListUiState.Success(FakeCollectorRepository.sampleCollectors),
                    onRetry = {},
                    onCollectorClick = {},
                )
            }
        }

        composeRule.onNode(hasText("Coleccionistas") and isHeading()).assertExists()
        composeRule.onNodeWithTag(CollectorListTags.item(100))
            .assert(hasText("Manolo Bellon"))
            .assert(onClickLabel("ver detalles del coleccionista"))
        composeRule.onNodeWithTag(CollectorListTags.item(101))
            .assert(hasText("Jaime Monsalve"))
            .assert(onClickLabel("ver detalles del coleccionista"))
        composeRule.onAllNodesWithText("Ver detalles ›").assertCountEquals(0)
    }

    @Test
    fun detalleDeAlbum_encabezadosFilasFusionadasYEnlaceDe48dp() {
        composeRule.setContent {
            VinilosTheme {
                AlbumDetailContent(
                    state = AlbumDetailUiState.Success(FakeAlbumRepository.sampleAlbums[0]),
                    onRetry = {},
                    onBack = {},
                    onArtistClick = {},
                    onAddTrack = {},
                )
            }
        }

        composeRule.onNode(hasText("Detalle del álbum") and isHeading()).assertExists()
        composeRule.onNodeWithTag(AlbumDetailTags.TITLE).assert(isHeading())

        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(AlbumDetailTags.artist(1)))
        composeRule.onNodeWithTag(AlbumDetailTags.artist(1))
            .assertHeightIsAtLeast(48.dp)
            .assert(onClickLabel("ver detalles del artista"))
            .assert(!hasText("›", substring = true))

        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(AlbumDetailTags.track(1)))
        composeRule.onNodeWithTag(AlbumDetailTags.track(1))
            .assert(hasText("1. Decisiones"))
            .assert(hasText("5:05"))

        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(AlbumDetailTags.COMMENTS))
        composeRule.onNodeWithTag(AlbumDetailTags.COMMENTS).assert(isHeading())

        composeRule.onNodeWithTag(AlbumDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(AlbumDetailTags.comment(1)))
        composeRule.onNodeWithTag(AlbumDetailTags.comment(1))
            .assert(hasContentDescription("Calificación: 4 de 5"))
            .assert(hasText("Un clásico de la salsa."))
    }

    @Test
    fun detalleDeArtista_filaDeAlbumMide48dpYTieneAccion() {
        composeRule.setContent {
            VinilosTheme {
                ArtistDetailContent(
                    state = ArtistDetailUiState.Success(FakeArtistRepository.sampleArtists[0]),
                    onRetry = {},
                    onBack = {},
                    onAlbumClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(ArtistDetailTags.album(100)))
        composeRule.onNodeWithTag(ArtistDetailTags.album(100))
            .assertHeightIsAtLeast(48.dp)
            .assert(onClickLabel("ver detalles del álbum"))

        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(ArtistDetailTags.PRIZES))
        composeRule.onNodeWithTag(ArtistDetailTags.PRIZES).assert(isHeading())

        composeRule.onNodeWithTag(ArtistDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(ArtistDetailTags.prize(100)))
        composeRule.onNodeWithTag(ArtistDetailTags.prize(100))
            .assert(hasText("Grammy Award"))
            .assert(hasText("Recording Academy · 1978"))
    }

    @Test
    fun detalleDeColeccionista_enlacesMiden48dp() {
        composeRule.setContent {
            VinilosTheme {
                CollectorDetailContent(
                    state = CollectorDetailUiState.Success(FakeCollectorRepository.sampleCollectors[0]),
                    onRetry = {},
                    onBack = {},
                    onArtistClick = {},
                    onAlbumClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(CollectorDetailTags.TITLE).assert(isHeading())

        composeRule.onNodeWithTag(CollectorDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(CollectorDetailTags.artist(1)))
        composeRule.onNodeWithTag(CollectorDetailTags.artist(1))
            .assertHeightIsAtLeast(48.dp)
            .assert(onClickLabel("ver detalles del artista"))

        composeRule.onNodeWithTag(CollectorDetailTags.CONTENT)
            .performScrollToNode(hasTestTag(CollectorDetailTags.album(100)))
        composeRule.onNodeWithTag(CollectorDetailTags.album(100))
            .assertHeightIsAtLeast(48.dp)
            .assert(onClickLabel("ver detalles del álbum"))
    }

    @Test
    fun estadoDeCarga_esLiveRegion() {
        composeRule.setContent {
            VinilosTheme {
                AlbumListContent(
                    state = AlbumListUiState.Loading,
                    onRetry = {},
                    onAlbumClick = {},
                    onCreateAlbum = {},
                )
            }
        }

        composeRule.onNodeWithTag(StateTags.LOADING).assert(polite)
    }

    @Test
    fun estadoDeError_esLiveRegion_yReintentarSigueSeparado() {
        composeRule.setContent {
            VinilosTheme {
                AlbumListContent(
                    state = AlbumListUiState.Error(ErrorType.NETWORK),
                    onRetry = {},
                    onAlbumClick = {},
                    onCreateAlbum = {},
                )
            }
        }

        composeRule.onNodeWithTag(StateTags.ERROR).assert(polite)
        composeRule.onNodeWithTag(StateTags.RETRY).assertTextEquals("Reintentar")
    }

    @Test
    fun crearAlbum_mientrasEnvia_elBotonAnunciaGuardando() {
        composeRule.setContent {
            VinilosTheme {
                CreateAlbumContent(
                    state = CreateAlbumUiState(
                        artistOptions = ArtistOptions.Loaded(emptyList()),
                        submission = Submission.Sending,
                    ),
                    actions = emptyCreateActions(),
                )
            }
        }

        composeRule.onNodeWithTag(CreateAlbumTags.SAVE)
            .assert(hasContentDescription("Guardando…"))
            .assert(polite)
        composeRule.onNode(hasText("Información del álbum") and isHeading()).assertExists()
    }

    @Test
    fun crearAlbum_errorDeEnvio_esLiveRegion() {
        composeRule.setContent {
            VinilosTheme {
                CreateAlbumContent(
                    state = CreateAlbumUiState(
                        artistOptions = ArtistOptions.Loaded(emptyList()),
                        submission = Submission.Failed(SubmitError.NETWORK),
                    ),
                    actions = emptyCreateActions(),
                )
            }
        }

        composeRule.onNodeWithTag(CreateAlbumTags.SUBMIT_ERROR).assert(polite)
    }

    @Test
    fun asociarTrack_mientrasEnvia_anunciaGuardando_yAlbumEsEncabezado() {
        composeRule.setContent {
            VinilosTheme {
                AssociateTrackContent(
                    state = AssociateTrackUiState(
                        album = AlbumLoad.Loaded(FakeAlbumRepository.sampleAlbums[0]),
                        submission = TrackSubmission.Sending,
                    ),
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

        composeRule.onNodeWithTag(AssociateTrackTags.SAVE)
            .assert(hasContentDescription("Guardando…"))
            .assert(polite)
        composeRule.onNodeWithTag(AssociateTrackTags.ALBUM).assert(isHeading())
        composeRule.onNode(hasText("Decisiones") and hasText("5:05")).assertExists()
    }

    @Test
    fun tarjetaDeAlbum_conFuenteAl200_noRecortaTexto() {
        composeRule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 2f)) {
                VinilosTheme {
                    AlbumListContent(
                        state = AlbumListUiState.Success(FakeAlbumRepository.sampleAlbums),
                        onRetry = {},
                        onAlbumClick = {},
                        onCreateAlbum = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("Rubén Blades Bellido de Luna · 1984", useUnmergedTree = true)
            .assert(sinRecorte())
    }

    @Test
    fun tituloDeBarra_conFuenteAl200_noSeRecorta() {
        composeRule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 2f)) {
                VinilosTheme {
                    CollectorDetailContent(
                        state = CollectorDetailUiState.Loading,
                        onRetry = {},
                        onBack = {},
                        onArtistClick = {},
                        onAlbumClick = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("Detalle del coleccionista", useUnmergedTree = true)
            .assert(sinRecorte())
    }

    @Test
    fun barraInferior_conFuenteAl200_noPartePalabrasEnAncho360() {
        grantLocalNetworkPermission()
        composeRule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 2f)) {
                VinilosTheme {
                    Box(Modifier.width(360.dp)) {
                        VinilosApp(
                            repository = FakeAlbumRepository(),
                            artistRepository = FakeArtistRepository(),
                            collectorRepository = FakeCollectorRepository(),
                        )
                    }
                }
            }
        }

        mapOf(
            TopLevelDestination.ALBUMS to "Álbumes",
            TopLevelDestination.ARTISTS to "Artistas",
            TopLevelDestination.COLLECTORS to "Coleccionistas",
        ).forEach { (destination, nombre) ->
            composeRule.onNode(
                hasText(nombre) and hasAnyAncestor(hasTestTag(destination.testTag)),
                useUnmergedTree = true,
            ).assert(enUnaLinea())
        }
    }

    private fun emptyCreateActions() = CreateAlbumActions(
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

    private fun onClickLabel(label: String) = SemanticsMatcher("onClickLabel=$label") { node ->
        node.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }

    private val polite = SemanticsMatcher.expectValue(
        SemanticsProperties.LiveRegion,
        LiveRegionMode.Polite,
    )

    private fun enUnaLinea() = SemanticsMatcher("texto en una línea sin desbordar") { node ->
        val results = mutableListOf<TextLayoutResult>()
        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
        results.isNotEmpty() && results.all { layout ->
            layout.lineCount == 1 &&
                layout.multiParagraph.maxIntrinsicWidth <= layout.layoutInput.constraints.maxWidth
        }
    }

    private fun sinRecorte() = SemanticsMatcher("texto sin recortar") { node ->
        val results = mutableListOf<TextLayoutResult>()
        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
        results.isNotEmpty() && results.none { layout ->
            layout.didOverflowHeight || (0 until layout.lineCount).any(layout::isLineEllipsized)
        }
    }
}
