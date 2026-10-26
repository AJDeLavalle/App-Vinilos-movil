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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import co.edu.uniandes.vinilos.ui.albums.AlbumListScreen
import co.edu.uniandes.vinilos.ui.albums.AlbumListViewModel
import co.edu.uniandes.vinilos.ui.common.ComingSoonScreen
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailScreen
import co.edu.uniandes.vinilos.ui.detail.AlbumDetailViewModel

object Routes {
    const val ALBUMS_TAB = "albums_tab"
    const val ALBUMS = "albums"
    const val ALBUM_DETAIL = "albums/{${AlbumDetailViewModel.ALBUM_ID_ARG}}"
    const val ARTISTS = "artists"
    const val COLLECTORS = "collectors"

    fun albumDetail(albumId: Int) = "albums/$albumId"
}

/** Pestañas de la barra inferior de la maqueta. */
enum class TopLevelDestination(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    ALBUMS(Routes.ALBUMS_TAB, R.string.nav_albums, R.drawable.ic_nav_album),
    ARTISTS(Routes.ARTISTS, R.string.nav_artists, R.drawable.ic_nav_artist),
    COLLECTORS(Routes.COLLECTORS, R.string.nav_collectors, R.drawable.ic_nav_collectors);

    val testTag: String get() = "nav_$route"
}

/** Raíz de la app. Recibe el repositorio para poder inyectar un fake en las pruebas de UI. */
@Composable
fun VinilosApp(repository: AlbumRepository) {
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
                composable(Routes.ALBUMS) {
                    AlbumListScreen(
                        viewModel = viewModel(factory = AlbumListViewModel.factory(repository)),
                        onAlbumClick = { albumId -> navController.navigate(Routes.albumDetail(albumId)) },
                    )
                }
                composable(
                    route = Routes.ALBUM_DETAIL,
                    arguments = listOf(navArgument(AlbumDetailViewModel.ALBUM_ID_ARG) { type = NavType.IntType }),
                ) {
                    AlbumDetailScreen(
                        viewModel = viewModel(factory = AlbumDetailViewModel.factory(repository)),
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable(Routes.ARTISTS) { ComingSoonScreen(stringResource(R.string.nav_artists)) }
            composable(Routes.COLLECTORS) { ComingSoonScreen(stringResource(R.string.nav_collectors)) }
        }
    }
}
