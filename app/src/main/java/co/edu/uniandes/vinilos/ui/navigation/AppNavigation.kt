package co.edu.uniandes.vinilos.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import co.edu.uniandes.vinilos.domain.repository.ArtistRepository
import co.edu.uniandes.vinilos.domain.repository.CollectorRepository
import co.edu.uniandes.vinilos.ui.albums.AlbumListScreen
import co.edu.uniandes.vinilos.ui.albums.AlbumListViewModel
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailScreen
import co.edu.uniandes.vinilos.ui.artists.ArtistDetailViewModel
import co.edu.uniandes.vinilos.ui.artists.ArtistListScreen
import co.edu.uniandes.vinilos.ui.artists.ArtistListViewModel
import co.edu.uniandes.vinilos.ui.collectors.CollectorDetailScreen
import co.edu.uniandes.vinilos.ui.collectors.CollectorDetailViewModel
import co.edu.uniandes.vinilos.ui.collectors.CollectorListScreen
import co.edu.uniandes.vinilos.ui.collectors.CollectorListViewModel
import co.edu.uniandes.vinilos.ui.create.CreateAlbumScreen
import co.edu.uniandes.vinilos.ui.create.CreateAlbumViewModel
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailScreen
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailViewModel
import co.edu.uniandes.vinilos.ui.tracks.AssociateTrackScreen
import co.edu.uniandes.vinilos.ui.tracks.AssociateTrackViewModel

object Routes {
    const val ALBUMS_TAB = "albums_tab"
    const val ALBUMS = "albums"
    const val ALBUM_DETAIL = "albums/{${AlbumDetailViewModel.ALBUM_ID_ARG}}"
    const val CREATE_ALBUM = "create_album"
    const val ASSOCIATE_TRACK = "albums/{${AlbumDetailViewModel.ALBUM_ID_ARG}}/tracks/new"
    const val ARTISTS_TAB = "artists_tab"
    const val ARTISTS = "artists"
    const val ARTIST_DETAIL =
        "artists/{${ArtistDetailViewModel.ARTIST_TYPE_ARG}}/{${ArtistDetailViewModel.ARTIST_ID_ARG}}"
    const val COLLECTORS_TAB = "collectors_tab"
    const val COLLECTORS = "collectors"
    const val COLLECTOR_DETAIL = "collectors/{${CollectorDetailViewModel.COLLECTOR_ID_ARG}}"

    fun albumDetail(albumId: Int) = "albums/$albumId"
    fun associateTrack(albumId: Int) = "albums/$albumId/tracks/new"
    fun artistDetail(artistId: Int, type: ArtistType) = "artists/${type.name}/$artistId"
    fun collectorDetail(collectorId: Int) = "collectors/$collectorId"
}

/** Aviso del formulario al catálogo para que recargue al volver con un álbum nuevo. */
private const val ALBUM_CREATED_KEY = "album_created"

/** Aviso del formulario al detalle del álbum para que muestre el track nuevo. */
private const val TRACK_ADDED_KEY = "track_added"

/** Pestañas de la barra inferior de la maqueta. */
enum class TopLevelDestination(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    ALBUMS(Routes.ALBUMS_TAB, R.string.nav_albums, R.drawable.ic_nav_album),
    ARTISTS(Routes.ARTISTS_TAB, R.string.nav_artists, R.drawable.ic_nav_artist),
    COLLECTORS(Routes.COLLECTORS_TAB, R.string.nav_collectors, R.drawable.ic_nav_collectors);

    val testTag: String get() = "nav_$route"
}

/** Raíz de la app. Recibe los repositorios para poder inyectar fakes en las pruebas de UI. */
@Composable
fun VinilosApp(
    repository: AlbumRepository,
    artistRepository: ArtistRepository,
    collectorRepository: CollectorRepository,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                        label = { Text(stringResource(destination.label)) },
                        modifier = Modifier.testTag(destination.testTag),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ALBUMS_TAB,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            // El catálogo y el detalle comparten pestaña: la barra marca "Álbumes" en ambos.
            navigation(startDestination = Routes.ALBUMS, route = Routes.ALBUMS_TAB) {
                composable(Routes.ALBUMS) { entry ->
                    val listViewModel: AlbumListViewModel =
                        viewModel(factory = AlbumListViewModel.factory(repository))
                    val albumCreated by entry.savedStateHandle
                        .getStateFlow(ALBUM_CREATED_KEY, false)
                        .collectAsStateWithLifecycle()
                    LaunchedEffect(albumCreated) {
                        if (albumCreated) {
                            listViewModel.loadAlbums()
                            entry.savedStateHandle[ALBUM_CREATED_KEY] = false
                        }
                    }
                    AlbumListScreen(
                        viewModel = listViewModel,
                        onAlbumClick = { albumId -> navController.navigate(Routes.albumDetail(albumId)) },
                        onCreateAlbum = { navController.navigate(Routes.CREATE_ALBUM) },
                    )
                }
                composable(Routes.CREATE_ALBUM) {
                    CreateAlbumScreen(
                        viewModel = viewModel(factory = CreateAlbumViewModel.factory(repository, artistRepository)),
                        onBack = { navController.popBackStack() },
                        onSaved = {
                            navController.previousBackStackEntry?.savedStateHandle?.set(ALBUM_CREATED_KEY, true)
                            navController.popBackStack()
                        },
                    )
                }
                composable(
                    route = Routes.ALBUM_DETAIL,
                    arguments = listOf(navArgument(AlbumDetailViewModel.ALBUM_ID_ARG) { type = NavType.IntType }),
                ) { entry ->
                    val detailViewModel: AlbumDetailViewModel =
                        viewModel(factory = AlbumDetailViewModel.factory(repository))
                    val trackAdded by entry.savedStateHandle
                        .getStateFlow(TRACK_ADDED_KEY, false)
                        .collectAsStateWithLifecycle()
                    LaunchedEffect(trackAdded) {
                        if (trackAdded) {
                            detailViewModel.loadAlbum()
                            entry.savedStateHandle[TRACK_ADDED_KEY] = false
                        }
                    }
                    AlbumDetailScreen(
                        viewModel = detailViewModel,
                        onBack = { navController.popBackStack() },
                        onArtistClick = { performer ->
                            navController.navigate(Routes.artistDetail(performer.id, performer.type))
                        },
                        onAddTrack = { albumId -> navController.navigate(Routes.associateTrack(albumId)) },
                    )
                }
                composable(
                    route = Routes.ASSOCIATE_TRACK,
                    arguments = listOf(navArgument(AlbumDetailViewModel.ALBUM_ID_ARG) { type = NavType.IntType }),
                ) {
                    AssociateTrackScreen(
                        viewModel = viewModel(factory = AssociateTrackViewModel.factory(repository)),
                        onBack = { navController.popBackStack() },
                        onSaved = {
                            navController.previousBackStackEntry?.savedStateHandle?.set(TRACK_ADDED_KEY, true)
                            navController.popBackStack()
                        },
                    )
                }
            }
            // Igual que en álbumes, la barra marca "Artistas" en el listado y en el detalle.
            navigation(startDestination = Routes.ARTISTS, route = Routes.ARTISTS_TAB) {
                composable(Routes.ARTISTS) {
                    ArtistListScreen(
                        viewModel = viewModel(factory = ArtistListViewModel.factory(artistRepository)),
                        onArtistClick = { artist ->
                            navController.navigate(Routes.artistDetail(artist.id, artist.type))
                        },
                    )
                }
                composable(
                    route = Routes.ARTIST_DETAIL,
                    arguments = listOf(
                        navArgument(ArtistDetailViewModel.ARTIST_TYPE_ARG) { type = NavType.StringType },
                        navArgument(ArtistDetailViewModel.ARTIST_ID_ARG) { type = NavType.IntType },
                    ),
                ) {
                    ArtistDetailScreen(
                        viewModel = viewModel(factory = ArtistDetailViewModel.factory(artistRepository)),
                        onBack = { navController.popBackStack() },
                        onAlbumClick = { albumId -> navController.navigate(Routes.albumDetail(albumId)) },
                    )
                }
            }
            navigation(startDestination = Routes.COLLECTORS, route = Routes.COLLECTORS_TAB) {
                composable(Routes.COLLECTORS) {
                    CollectorListScreen(
                        viewModel = viewModel(factory = CollectorListViewModel.factory(collectorRepository)),
                        onCollectorClick = { collectorId ->
                            navController.navigate(Routes.collectorDetail(collectorId))
                        },
                    )
                }
                composable(
                    route = Routes.COLLECTOR_DETAIL,
                    arguments = listOf(
                        navArgument(CollectorDetailViewModel.COLLECTOR_ID_ARG) { type = NavType.IntType },
                    ),
                ) {
                    CollectorDetailScreen(
                        viewModel = viewModel(factory = CollectorDetailViewModel.factory(collectorRepository)),
                        onBack = { navController.popBackStack() },
                        onArtistClick = { performer ->
                            navController.navigate(Routes.artistDetail(performer.id, performer.type))
                        },
                        onAlbumClick = { albumId -> navController.navigate(Routes.albumDetail(albumId)) },
                    )
                }
            }
        }
    }
}
