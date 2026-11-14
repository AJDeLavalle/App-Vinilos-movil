package co.edu.uniandes.vinilos.testutil

import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.model.Comment
import co.edu.uniandes.vinilos.domain.model.NewAlbum
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.domain.model.Track
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import kotlinx.coroutines.CompletableDeferred

/**
 * Repositorio determinista para las pruebas E2E de UI (no depende del backend).
 * `gate` permite mantener la petición pendiente para observar el estado de carga.
 */
class FakeAlbumRepository(
    var albums: List<Album> = sampleAlbums,
    var error: Exception? = null,
    var gate: CompletableDeferred<Unit>? = null,
    var createError: Exception? = null,
    var linkError: Exception? = null,
    var createGate: CompletableDeferred<Unit>? = null,
) : AlbumRepository {

    var createCalls = 0
        private set
    val linkCalls = mutableListOf<Triple<Int, Int, ArtistType>>()

    override suspend fun getAlbums(): List<Album> {
        gate?.await()
        error?.let { throw it }
        return albums
    }

    override suspend fun getAlbum(albumId: Int): Album {
        gate?.await()
        error?.let { throw it }
        return albums.firstOrNull { it.id == albumId } ?: throw AlbumNotFoundException(albumId)
    }

    override suspend fun createAlbum(album: NewAlbum): Album {
        createCalls += 1
        createGate?.await()
        createError?.let { throw it }
        val created = Album(
            id = 500,
            name = album.name,
            cover = album.cover,
            releaseDate = album.releaseDate,
            description = album.description,
            genre = album.genre,
            recordLabel = album.recordLabel,
        )
        albums = albums + created
        return created
    }

    override suspend fun addArtistToAlbum(albumId: Int, artistId: Int, type: ArtistType) {
        linkCalls += Triple(albumId, artistId, type)
        linkError?.let { throw it }
    }

    companion object {
        val sampleAlbums = listOf(
            Album(
                id = 100,
                name = "Buscando América",
                cover = "",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Primer álbum de la banda Seis del Solar.",
                genre = "Salsa",
                recordLabel = "Elektra",
                tracks = listOf(Track(1, "Decisiones", "5:05"), Track(2, "Desapariciones", "6:29")),
                performers = listOf(Performer(1, "Rubén Blades Bellido de Luna", "")),
                comments = listOf(Comment(1, "Un clásico de la salsa.", 4)),
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
