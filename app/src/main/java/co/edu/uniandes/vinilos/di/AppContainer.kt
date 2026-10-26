package co.edu.uniandes.vinilos.di

import co.edu.uniandes.vinilos.BuildConfig
import co.edu.uniandes.vinilos.data.remote.AlbumApiService
import co.edu.uniandes.vinilos.data.remote.AlbumServiceAdapter
import co.edu.uniandes.vinilos.data.repository.AlbumRepositoryImpl
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Inyección de dependencias manual: crea una sola instancia de cada capa para toda la app. */
class AppContainer(baseUrl: String = BuildConfig.API_BASE_URL) {

    private val apiService: AlbumApiService = createAlbumApiService(baseUrl)

    val albumRepository: AlbumRepository by lazy {
        AlbumRepositoryImpl(AlbumServiceAdapter(apiService))
    }
}

fun createAlbumApiService(baseUrl: String): AlbumApiService {
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
        .create(AlbumApiService::class.java)
}
