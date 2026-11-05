package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.data.remote.dto.ArtistDto
import co.edu.uniandes.vinilos.data.remote.dto.PrizeDto
import retrofit2.http.GET
import retrofit2.http.Path

/** Endpoints de BackVynils (src/musician, src/band y src/prize). */
interface ArtistApiService {
    @GET("musicians/{musicianId}")
    suspend fun getMusician(@Path("musicianId") musicianId: Int): ArtistDto

    @GET("bands/{bandId}")
    suspend fun getBand(@Path("bandId") bandId: Int): ArtistDto

    @GET("prizes")
    suspend fun getPrizes(): List<PrizeDto>
}
