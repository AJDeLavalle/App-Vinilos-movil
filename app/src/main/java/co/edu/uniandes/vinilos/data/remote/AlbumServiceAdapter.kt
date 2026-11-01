package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.data.remote.dto.AlbumDto
import co.edu.uniandes.vinilos.data.remote.dto.CommentDto
import co.edu.uniandes.vinilos.data.remote.dto.PerformerDto
import co.edu.uniandes.vinilos.data.remote.dto.TrackDto
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.AlbumNotFoundException
import co.edu.uniandes.vinilos.domain.model.Comment
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.domain.model.Track
import retrofit2.HttpException

/**
 * Service Adapter: traduce el contrato HTTP de BackVynils (DTO, códigos de estado)
 * al modelo de dominio que consume el Repository.
 */
class AlbumServiceAdapter(private val api: AlbumApiService) {

    suspend fun getAlbums(): List<Album> =
        api.getAlbums().mapNotNull { it.toDomain() }

    suspend fun getAlbum(albumId: Int): Album {
        val dto = try {
            api.getAlbum(albumId)
        } catch (e: HttpException) {
            if (e.code() == 404) throw AlbumNotFoundException(albumId) else throw e
        }
        return dto.toDomain() ?: throw AlbumNotFoundException(albumId)
    }
}

/** Descarta registros sin id, que no se pueden navegar ni identificar. */
internal fun AlbumDto.toDomain(): Album? {
    val albumId = id ?: return null
    return Album(
        id = albumId,
        name = name.orEmpty(),
        cover = cover.orEmpty(),
        releaseDate = releaseDate.orEmpty(),
        description = description.orEmpty(),
        genre = genre.orEmpty(),
        recordLabel = recordLabel.orEmpty(),
        tracks = tracks.orEmpty().mapNotNull { it.toDomain() },
        performers = performers.orEmpty().mapNotNull { it.toDomain() },
        comments = comments.orEmpty().mapNotNull { it.toDomain() },
    )
}

internal fun TrackDto.toDomain(): Track? =
    id?.let { Track(id = it, name = name.orEmpty(), duration = duration.orEmpty()) }

internal fun PerformerDto.toDomain(): Performer? =
    id?.let { Performer(id = it, name = name.orEmpty(), image = image.orEmpty()) }

internal fun CommentDto.toDomain(): Comment? =
    id?.let { Comment(id = it, description = description.orEmpty(), rating = (rating ?: 0).coerceIn(0, 5)) }
