package co.edu.uniandes.vinilos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import co.edu.uniandes.vinilos.ui.albums.AlbumListScreen
import co.edu.uniandes.vinilos.ui.albums.AlbumListViewModel

object Routes {
    const val ALBUMS = "albums"
}

/** Raíz de la app. Recibe el repositorio para poder inyectar un fake en las pruebas de UI. */
@Composable
fun VinilosApp(repository: AlbumRepository) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.ALBUMS) {
        composable(Routes.ALBUMS) {
            AlbumListScreen(
                viewModel = viewModel(factory = AlbumListViewModel.factory(repository)),
                onAlbumClick = { /* HU02: navegar al detalle */ },
            )
        }
    }
}
