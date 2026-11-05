package co.edu.uniandes.vinilos.domain.model

/** BackVynils separa a los artistas en dos recursos: `/musicians` y `/bands`. */
enum class ArtistType { MUSICIAN, BAND }

data class Artist(
    val id: Int,
    val name: String,
    val image: String,
    val description: String,
    val type: ArtistType,
    /** Fecha ISO de nacimiento (músico) o de creación (banda). */
    val date: String,
    val albums: List<Album> = emptyList(),
    val prizes: List<ArtistPrize> = emptyList(),
) {
    val year: String
        get() = date.take(4)
}

data class ArtistPrize(
    val id: Int,
    val name: String,
    val organization: String,
    val premiationDate: String,
) {
    val year: String
        get() = premiationDate.take(4)
}
