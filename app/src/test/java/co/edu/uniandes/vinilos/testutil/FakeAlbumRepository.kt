package co.edu.uniandes.vinilos.testutil

import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.model.NewAlbum
import co.edu.uniandes.vinilos.domain.model.NewTrack
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.domain.model.Track
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository

/** Repositorio en memoria con respuestas deterministas; `error` simula un fallo en cada llamada. */
class FakeAlbumRepository(
    var albums: List<Album> = sampleAlbums,
    var error: Exception? = null,
    var createError: Exception? = null,
    var linkError: Exception? = null,
    var trackError: Exception? = null,
) : AlbumRepository {

    var addTrackCalls = 0
        private set
    var lastNewTrack: NewTrack? = null
        private set

    var getAlbumsCalls = 0
        private set
    var createCalls = 0
        private set
    var lastNewAlbum: NewAlbum? = null
        private set
    val linkCalls = mutableListOf<Triple<Int, Int, ArtistType>>()

    override suspend fun getAlbums(): List<Album> {
        getAlbumsCalls++
        error?.let { throw it }
        return albums
    }

    override suspend fun getAlbum(albumId: Int): Album {
        error?.let { throw it }
        return albums.firstOrNull { it.id == albumId } ?: throw AlbumNotFoundException(albumId)
    }

    override suspend fun createAlbum(album: NewAlbum): Album {
        createCalls += 1
        createError?.let { throw it }
        val created = Album(
            id = NEW_ALBUM_ID,
            name = album.name,
            cover = album.cover,
            releaseDate = album.releaseDate,
            description = album.description,
            genre = album.genre,
            recordLabel = album.recordLabel,
        )
        lastNewAlbum = album
        albums = albums + created
        return created
    }

    override suspend fun addArtistToAlbum(albumId: Int, artistId: Int, type: ArtistType) {
        linkCalls += Triple(albumId, artistId, type)
        linkError?.let { throw it }
    }

    override suspend fun addTrack(albumId: Int, track: NewTrack): Track {
        addTrackCalls += 1
        trackError?.let { throw it }
        lastNewTrack = track
        return Track(NEW_TRACK_ID, track.name, track.duration)
    }

    companion object {
        const val NEW_ALBUM_ID = 500
        const val NEW_TRACK_ID = 900

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
