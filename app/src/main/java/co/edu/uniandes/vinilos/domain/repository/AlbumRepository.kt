package co.edu.uniandes.vinilos.domain.repository

import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.model.NewAlbum
import co.edu.uniandes.vinilos.domain.model.NewTrack
import co.edu.uniandes.vinilos.domain.model.Track

/**
 * Punto único de acceso a los álbumes para la capa de UI.
 *
 * Las implementaciones lanzan [co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException]
 * cuando el álbum no existe y [java.io.IOException] ante fallos de red.
 */
interface AlbumRepository {
    suspend fun getAlbums(): List<Album>
    suspend fun getAlbum(albumId: Int): Album

    /** Lanza [co.edu.uniandes.vinilos.domain.model.InvalidAlbumException] si el backend rechaza los datos. */
    suspend fun createAlbum(album: NewAlbum): Album
    suspend fun addArtistToAlbum(albumId: Int, artistId: Int, type: ArtistType)

    /**
     * Lanza [co.edu.uniandes.vinilos.domain.model.InvalidTrackException] si el backend rechaza los
     * datos y [co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException] si el álbum no existe.
     */
    suspend fun addTrack(albumId: Int, track: NewTrack): Track
}
