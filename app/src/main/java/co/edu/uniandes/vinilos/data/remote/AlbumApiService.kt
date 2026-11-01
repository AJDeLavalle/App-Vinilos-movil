package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.data.remote.dto.AlbumDto
import retrofit2.http.GET
import retrofit2.http.Path

/** Endpoints de BackVynils (src/album/album.controller.ts). */
interface AlbumApiService {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("albums/{albumId}")
    suspend fun getAlbum(@Path("albumId") albumId: Int): AlbumDto
}
