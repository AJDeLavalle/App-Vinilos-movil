package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.data.remote.dto.CollectorAlbumDto
import co.edu.uniandes.vinilos.data.remote.dto.CollectorDto
import retrofit2.http.GET
import retrofit2.http.Path

/** Endpoints de BackVynils (src/collector y src/collectoralbum). */
interface CollectorApiService {
    @GET("collectors")
    suspend fun getCollectors(): List<CollectorDto>

    @GET("collectors/{collectorId}")
    suspend fun getCollector(@Path("collectorId") collectorId: Int): CollectorDto

    @GET("collectors/{collectorId}/albums")
    suspend fun getCollectorAlbums(@Path("collectorId") collectorId: Int): List<CollectorAlbumDto>
}
