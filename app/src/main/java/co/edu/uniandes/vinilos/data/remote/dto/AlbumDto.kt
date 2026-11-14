package co.edu.uniandes.vinilos.data.remote.dto

// Contrato de BackVynils (src/album/album.entity.ts). Los campos son anulables porque
// Gson no respeta la nulabilidad de Kotlin; el adapter aplica los valores por defecto.

data class AlbumDto(
    val id: Int?,
    val name: String?,
    val cover: String?,
    val releaseDate: String?,
    val description: String?,
    val genre: String?,
    val recordLabel: String?,
    val tracks: List<TrackDto>?,
    val performers: List<PerformerDto>?,
    val comments: List<CommentDto>?,
)

/** Cuerpo de `POST /albums`. El esquema de Joi rechaza campos adicionales, así que son estos seis. */
data class NewAlbumDto(
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String,
)

/** Cuerpo de `POST /albums/{id}/tracks`. */
data class NewTrackDto(
    val name: String,
    val duration: String,
)

data class TrackDto(
    val id: Int?,
    val name: String?,
    val duration: String?,
)

data class CommentDto(
    val id: Int?,
    val description: String?,
    val rating: Int?,
)

data class PerformerDto(
    val id: Int?,
    val name: String?,
    val image: String?,
    /** Solo las bandas lo traen; los músicos traen birthDate. */
    val creationDate: String?,
)
