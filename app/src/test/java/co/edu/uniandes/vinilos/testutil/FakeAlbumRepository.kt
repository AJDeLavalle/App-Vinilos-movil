package co.edu.uniandes.vinilos.testutil

import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.domain.model.Track
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository

/** Repositorio en memoria con respuestas deterministas; `error` simula un fallo en cada llamada. */
class FakeAlbumRepository(
    var albums: List<Album> = sampleAlbums,
    var error: Exception? = null,
) : AlbumRepository {

    var getAlbumsCalls = 0
        private set

    override suspend fun getAlbums(): List<Album> {
        getAlbumsCalls++
        error?.let { throw it }
        return albums
    }

    override suspend fun getAlbum(albumId: Int): Album {
        error?.let { throw it }
        return albums.firstOrNull { it.id == albumId } ?: throw AlbumNotFoundException(albumId)
    }

    companion object {
        val sampleAlbums = listOf(
            Album(
                id = 100,
                name = "Buscando América",
                cover = "https://example.com/buscando.jpg",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Primer álbum de la banda Seis del Solar.",
                genre = "Salsa",
                recordLabel = "Elektra",
                tracks = listOf(Track(1, "Decisiones", "5:05"), Track(2, "Desapariciones", "6:29")),
                performers = listOf(Performer(1, "Rubén Blades Bellido de Luna", "")),
            ),
            Album(
                id = 101,
                name = "Poeta del pueblo",
                cover = "",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Recopilación de 27 composiciones.",
                genre = "Salsa",
                recordLabel = "EMI",
            ),
        )
    }
}
