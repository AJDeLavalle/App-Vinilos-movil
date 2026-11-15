package co.edu.uniandes.vinilos.data.remote.dto

// Contrato de BackVynils (src/collector y src/collectoralbum). Los campos son anulables porque
// Gson no respeta la nulabilidad de Kotlin; el adapter aplica los valores por defecto.

data class CollectorDto(
    val id: Int?,
    val name: String?,
    val telephone: String?,
    val email: String?,
    val favoritePerformers: List<PerformerDto>?,
    val collectorAlbums: List<CollectorAlbumDto>?,
)

/** `album` solo viene en `GET /collectors/{id}/albums`; en `GET /collectors` llega sin él. */
data class CollectorAlbumDto(
    val id: Int?,
    val album: AlbumDto?,
)
