package co.edu.uniandes.vinilos.domain.model

data class Collector(
    val id: Int,
    val name: String,
    val telephone: String,
    val email: String,
    /** Artistas favoritos: son los "gustos" de la maqueta. */
    val favoritePerformers: List<Performer> = emptyList(),
    val albumCount: Int = 0,
    /** Solo se completa en el detalle; el listado conoce únicamente [albumCount]. */
    val albums: List<Album> = emptyList(),
)
