package co.edu.uniandes.vinilos.data.remote

import android.os.Build
import co.edu.uniandes.vinilos.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Identifica a la app ante los servidores de imágenes.
 * Wikimedia responde 403 al User-Agent por defecto de OkHttp (DEF-07, #83).
 */
internal class UserAgentInterceptor(private val userAgent: String = vinilosUserAgent()) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("User-Agent", userAgent).build())
}

/** "Vinilos/2.0 (Android 17; co.edu.uniandes.vinilos)". Genérico: sin personas, correos ni URL. */
internal fun vinilosUserAgent(
    versionName: String = BuildConfig.VERSION_NAME,
    androidRelease: String = Build.VERSION.RELEASE.orEmpty(),
): String = "Vinilos/$versionName (Android $androidRelease; ${BuildConfig.APPLICATION_ID})"
    // OkHttp rechaza cabeceras con caracteres fuera de ASCII imprimible.
    .filter { it in ' '..'~' }
