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
)
