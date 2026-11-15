package co.edu.uniandes.vinilos

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.StrictMode
import co.edu.uniandes.vinilos.di.AppContainer
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class VinilosApplication : Application(), ImageLoaderFactory {
    val container: AppContainer by lazy { AppContainer() }

    /**
     * Cargador de imágenes de toda la app (Coil lo pide la primera vez que muestra una portada):
     * - Caché en memoria de hasta el 15 % de la memoria de la app (Coil usa 25 % por defecto).
     * - Caché en disco de 50 MB: las portadas no se vuelven a descargar al reabrir la app.
     * - RGB_565 cuando la imagen no tiene transparencia: 2 bytes por píxel en lugar de 4.
     * - Se ignoran las cabeceras de caché del servidor de cada portada, que a veces la prohíben.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.15).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(50L * 1024 * 1024)
                .build()
        }
        .allowRgb565(true)
        .respectCacheHeaders(false)
        .build()

    /**
     * Si el sistema necesita memoria con la app en segundo plano, se sueltan las cachés de datos;
     * se vuelven a llenar al volver. Coil hace lo mismo con su caché de imágenes por su cuenta.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        @Suppress("DEPRECATION")
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) container.clearCaches()
    }

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
