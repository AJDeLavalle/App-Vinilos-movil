package co.edu.uniandes.vinilos.di

import co.edu.uniandes.vinilos.BuildConfig
import co.edu.uniandes.vinilos.data.remote.AlbumApiService
import co.edu.uniandes.vinilos.data.remote.AlbumServiceAdapter
import co.edu.uniandes.vinilos.data.remote.ArtistApiService
import co.edu.uniandes.vinilos.data.remote.ArtistServiceAdapter
import co.edu.uniandes.vinilos.data.remote.CollectorApiService
import co.edu.uniandes.vinilos.data.remote.CollectorServiceAdapter
import co.edu.uniandes.vinilos.data.remote.UserAgentInterceptor
import co.edu.uniandes.vinilos.data.repository.AlbumRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.ArtistRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.CollectorRepositoryImpl
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import co.edu.uniandes.vinilos.domain.repository.ArtistRepository
import co.edu.uniandes.vinilos.domain.repository.CollectorRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Inyección de dependencias manual: crea una sola instancia de cada capa para toda la app.
 *
 * Los tres servicios y el cargador de imágenes (Coil) comparten un solo OkHttpClient: un pool de
 * conexiones y un pool de hilos en lugar de tres, y la conexión con el backend se reutiliza
 * entre pantallas. Todo se crea al primer uso.
 */
class AppContainer(baseUrl: String = BuildConfig.API_BASE_URL) {

    /** Cliente HTTP único de la app: lo usan Retrofit (API) y Coil (portadas e imágenes). */
    val okHttpClient: OkHttpClient by lazy { createOkHttpClient() }

    private val retrofit: Retrofit by lazy { createRetrofit(baseUrl, okHttpClient) }

    private val albumRepositoryImpl by lazy {
        AlbumRepositoryImpl(
            AlbumServiceAdapter(retrofit.create(AlbumApiService::class.java)),
            // Un álbum nuevo cambia los álbumes del artista y puede cambiar los de un coleccionista.
            onAlbumsChanged = {
                artistRepositoryImpl.clearCache()
                collectorRepositoryImpl.clearCache()
            },
        )
    }
    private val artistRepositoryImpl by lazy {
        ArtistRepositoryImpl(ArtistServiceAdapter(retrofit.create(ArtistApiService::class.java)))
    }
    private val collectorRepositoryImpl by lazy {
        CollectorRepositoryImpl(CollectorServiceAdapter(retrofit.create(CollectorApiService::class.java)))
    }

    val albumRepository: AlbumRepository get() = albumRepositoryImpl
    val artistRepository: ArtistRepository get() = artistRepositoryImpl
    val collectorRepository: CollectorRepository get() = collectorRepositoryImpl

    /** Libera las cachés en memoria; la siguiente lectura vuelve a consultar el backend. */
    fun clearCaches() {
        albumRepositoryImpl.clearCache()
        artistRepositoryImpl.clearCache()
        collectorRepositoryImpl.clearCache()
    }
}

fun createAlbumApiService(baseUrl: String): AlbumApiService =
    createRetrofit(baseUrl).create(AlbumApiService::class.java)

fun createArtistApiService(baseUrl: String): ArtistApiService =
    createRetrofit(baseUrl).create(ArtistApiService::class.java)

fun createCollectorApiService(baseUrl: String): CollectorApiService =
    createRetrofit(baseUrl).create(CollectorApiService::class.java)

private const val CONNECT_TIMEOUT_SECONDS = 10L
private const val READ_TIMEOUT_SECONDS = 15L

internal fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .addInterceptor(UserAgentInterceptor())
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        }
    }
    .build()

internal fun createRetrofit(baseUrl: String, client: OkHttpClient = createOkHttpClient()): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
