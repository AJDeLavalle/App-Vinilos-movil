package co.edu.uniandes.vinilos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.uniandes.vinilos.ui.navigation.VinilosApp
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as VinilosApplication).container
        // Solo en debug, para los scripts de Monkey (docs/pruebas/sprint-3/monkey):
        // `am start --es start_route <ruta>` abre una pantalla interna y cada pantalla visitada
        // queda en logcat con la etiqueta VinilosNav.
        // Al recrear la actividad (rotación) no se vuelve a navegar: el NavController restaura su pila.
        val startRoute = if (BuildConfig.DEBUG && savedInstanceState == null) {
            intent.getStringExtra(EXTRA_START_ROUTE)
        } else {
            null
        }
        val onRouteChange: ((String) -> Unit)? = if (BuildConfig.DEBUG) this::logRoute else null
        setContent {
            VinilosTheme {
                VinilosApp(
                    repository = container.albumRepository,
                    artistRepository = container.artistRepository,
                    collectorRepository = container.collectorRepository,
                    startRoute = startRoute,
                    onRouteChange = onRouteChange,
                )
            }
        }
    }

    private fun logRoute(route: String) {
        if (BuildConfig.DEBUG) Log.i(NAV_LOG_TAG, "destino=$route")
    }

    companion object {
        /** Extra (solo debug) con la ruta de navegación que se abre al arrancar. */
        const val EXTRA_START_ROUTE = "start_route"

        /** Etiqueta de logcat (solo debug) con cada pantalla visitada; la usan los scripts de Monkey. */
        const val NAV_LOG_TAG = "VinilosNav"
    }
}
