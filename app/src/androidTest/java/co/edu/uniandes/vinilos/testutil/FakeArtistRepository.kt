package co.edu.uniandes.vinilos.testutil

import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistNotFoundException
import co.edu.uniandes.vinilos.domain.model.ArtistPrize
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.repository.ArtistRepository

/** Repositorio determinista para las pruebas E2E de UI (no depende del backend). */
class FakeArtistRepository(
    var artists: List<Artist> = sampleArtists,
    var error: Exception? = null,
) : ArtistRepository {

    override suspend fun getArtists(): List<Artist> {
        error?.let { throw it }
        return artists
    }

    override suspend fun getArtist(artistId: Int, type: ArtistType): Artist {
        error?.let { throw it }
        return artists.firstOrNull { it.id == artistId && it.type == type }
            ?: throw ArtistNotFoundException(artistId)
    }

    companion object {
        /** El id 1 coincide con el intérprete de "Buscando América" en [FakeAlbumRepository]. */
        val sampleArtists = listOf(
            Artist(
                id = 1,
                name = "Rubén Blades Bellido de Luna",
                image = "",
                description = "Cantante y compositor panameño.",
                type = ArtistType.MUSICIAN,
                date = "1948-07-16T00:00:00.000Z",
                albums = listOf(
                    Album(100, "Buscando América", "", "1984-08-01T00:00:00.000Z", "", "Salsa", "Elektra"),
                ),
                prizes = listOf(ArtistPrize(100, "Grammy Award", "Recording Academy", "1978-12-10T00:00:00.000Z")),
            ),
            Artist(
                id = 2,
                name = "Queen",
                image = "",
                description = "Banda británica de rock.",
                type = ArtistType.BAND,
                date = "1970-01-01T00:00:00.000Z",
            ),
        )
    }
}
