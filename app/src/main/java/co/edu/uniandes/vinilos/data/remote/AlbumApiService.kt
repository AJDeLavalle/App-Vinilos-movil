package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.data.remote.dto.AlbumDto
import co.edu.uniandes.vinilos.data.remote.dto.NewAlbumDto
import co.edu.uniandes.vinilos.data.remote.dto.NewTrackDto
import co.edu.uniandes.vinilos.data.remote.dto.TrackDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Endpoints de BackVynils (src/album, src/albummusician y src/albumband). */
interface AlbumApiService {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("albums/{albumId}")
    suspend fun getAlbum(@Path("albumId") albumId: Int): AlbumDto

    @POST("albums")
    suspend fun createAlbum(@Body album: NewAlbumDto): AlbumDto

    @POST("albums/{albumId}/musicians/{musicianId}")
    suspend fun addMusicianToAlbum(@Path("albumId") albumId: Int, @Path("musicianId") musicianId: Int)

    @POST("albums/{albumId}/bands/{bandId}")
    suspend fun addBandToAlbum(@Path("albumId") albumId: Int, @Path("bandId") bandId: Int)

    @POST("albums/{albumId}/tracks")
    suspend fun addTrack(@Path("albumId") albumId: Int, @Body track: NewTrackDto): TrackDto
}
