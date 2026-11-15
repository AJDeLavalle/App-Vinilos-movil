package co.edu.uniandes.vinilos

import android.app.Application
import android.os.StrictMode
import co.edu.uniandes.vinilos.di.AppContainer

class VinilosApplication : Application() {
    val container: AppContainer by lazy { AppContainer() }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) enableStrictMode()
    }

    /**
     * Solo en debug: registra en logcat (etiqueta StrictMode) cualquier acceso a red o disco en
     * el hilo principal, que es la causa típica de un ANR. Sirve para vigilar que la red siga
     * en Dispatchers.IO a medida que se agregan pantallas.
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectNetwork()
                .detectDiskReads()
                .detectDiskWrites()
                .penaltyLog()
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build(),
        )
    }
}
