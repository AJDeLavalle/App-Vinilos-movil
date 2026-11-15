package co.edu.uniandes.vinilos.data.remote.dto

// Contrato de BackVynils (src/musician, src/band y src/prize). Un músico trae birthDate y una
// banda creationDate. Los campos son anulables porque Gson no respeta la nulabilidad de Kotlin.

data class ArtistDto(
    val id: Int?,
    val name: String?,
    val image: String?,
    val description: String?,
    val birthDate: String?,
    val creationDate: String?,
    val albums: List<AlbumDto>?,
    val performerPrizes: List<PerformerPrizeDto>?,
)

/** El artista solo recibe el id y la fecha; el nombre del premio viene en `GET /prizes`. */
data class PerformerPrizeDto(
    val id: Int?,
    val premiationDate: String?,
)

data class PrizeDto(
    val id: Int?,
    val name: String?,
    val organization: String?,
    val performerPrizes: List<PerformerPrizeDto>?,
)
