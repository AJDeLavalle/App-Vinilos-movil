package co.edu.uniandes.vinilos.domain.repository

import co.edu.uniandes.vinilos.domain.model.Album

/**
 * Punto único de acceso a los álbumes para la capa de UI.
 *
 * Las implementaciones lanzan [co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException]
 * cuando el álbum no existe y [java.io.IOException] ante fallos de red.
 */
interface AlbumRepository {
    suspend fun getAlbums(): List<Album>
    suspend fun getAlbum(albumId: Int): Album
}
