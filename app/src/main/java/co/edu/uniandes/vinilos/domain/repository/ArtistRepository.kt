package co.edu.uniandes.vinilos.domain.repository

import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistType

/**
 * Punto único de acceso a los artistas para la capa de UI.
 *
 * Las implementaciones lanzan [co.edu.uniandes.vinilos.domain.model.ArtistNotFoundException]
 * cuando el artista no existe y [java.io.IOException] ante fallos de red.
 */
interface ArtistRepository {
    /** Músicos y bandas ordenados por nombre. Sus premios solo vienen en [getArtist]. */
    suspend fun getArtists(): List<Artist>
    suspend fun getArtist(artistId: Int, type: ArtistType): Artist
}
