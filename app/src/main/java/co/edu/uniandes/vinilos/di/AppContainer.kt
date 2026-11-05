package co.edu.uniandes.vinilos.di

import co.edu.uniandes.vinilos.BuildConfig
import co.edu.uniandes.vinilos.data.remote.AlbumApiService
import co.edu.uniandes.vinilos.data.remote.AlbumServiceAdapter
import co.edu.uniandes.vinilos.data.remote.ArtistApiService
import co.edu.uniandes.vinilos.data.remote.ArtistServiceAdapter
import co.edu.uniandes.vinilos.data.remote.CollectorApiService
import co.edu.uniandes.vinilos.data.remote.CollectorServiceAdapter
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

/** Inyección de dependencias manual: crea una sola instancia de cada capa para toda la app. */
class AppContainer(baseUrl: String = BuildConfig.API_BASE_URL) {

    private val apiService: AlbumApiService = createAlbumApiService(baseUrl)
    private val artistApiService: ArtistApiService = createArtistApiService(baseUrl)
    private val collectorApiService: CollectorApiService = createCollectorApiService(baseUrl)

    val albumRepository: AlbumRepository by lazy {
        AlbumRepositoryImpl(AlbumServiceAdapter(apiService))
    }

    val artistRepository: ArtistRepository by lazy {
        ArtistRepositoryImpl(ArtistServiceAdapter(artistApiService))
    }

    val collectorRepository: CollectorRepository by lazy {
        CollectorRepositoryImpl(CollectorServiceAdapter(collectorApiService))
    }
}

fun createAlbumApiService(baseUrl: String): AlbumApiService =
    createRetrofit(baseUrl).create(AlbumApiService::class.java)

fun createArtistApiService(baseUrl: String): ArtistApiService =
    createRetrofit(baseUrl).create(ArtistApiService::class.java)

fun createCollectorApiService(baseUrl: String): CollectorApiService =
    createRetrofit(baseUrl).create(CollectorApiService::class.java)

private fun createRetrofit(baseUrl: String): Retrofit {
    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
