package co.edu.uniandes.vinilos.domain.model

data class Album(
    val id: Int,
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String,
    val tracks: List<Track> = emptyList(),
    val performers: List<Performer> = emptyList(),
) {
    /** Año de lanzamiento extraído de la fecha ISO que entrega el backend (p. ej. "1984-08-01T00:00:00.000Z"). */
    val releaseYear: String
        get() = releaseDate.take(4)

    /** Nombres de los intérpretes separados por coma; vacío si el backend no los asocia. */
    val performerNames: String
        get() = performers.joinToString(", ") { it.name }

    /** Línea "Artista · Año" de la maqueta, omitiendo las partes vacías. */
    fun subtitle(separator: String): String =
        listOf(performerNames, releaseYear).filter { it.isNotBlank() }.joinToString(separator)
}

data class Track(
    val id: Int,
    val name: String,
    val duration: String,
)

data class Performer(
    val id: Int,
    val name: String,
    val image: String,
)
