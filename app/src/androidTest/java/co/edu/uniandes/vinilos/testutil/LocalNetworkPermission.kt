package co.edu.uniandes.vinilos.testutil

import android.Manifest
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Concede ACCESS_LOCAL_NETWORK antes de lanzar la UI. En debug, el catálogo pide este permiso en
 * Android 17 (API 37) y su diálogo taparía la pantalla bajo prueba; connectedAndroidTest instala
 * la app sin concederlo, así que las pruebas no pueden depender del estado del emulador.
 */
fun grantLocalNetworkPermission() {
    if (Build.VERSION.SDK_INT < 37) return
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    instrumentation.uiAutomation.grantRuntimePermission(
        instrumentation.targetContext.packageName,
        Manifest.permission.ACCESS_LOCAL_NETWORK,
    )
}
