package co.edu.uniandes.vinilos.domain.model

/** Valores que acepta BackVynils (src/album/album.service.ts); cualquier otro responde 400. */
val ALBUM_GENRES = listOf("Classical", "Salsa", "Rock", "Folk")
val ALBUM_RECORD_LABELS = listOf("Sony Music", "EMI", "Discos Fuentes", "Elektra", "Fania Records")

data class NewAlbum(
    val name: String,
    val cover: String,
    /** Fecha en formato AAAA-MM-DD. */
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String,
)

/** El backend respondió 400: rechazó los datos del álbum. */
class InvalidAlbumException : Exception("El backend rechazó los datos del álbum")

data class NewTrack(
    val name: String,
    /** Duración en formato m:ss, por ejemplo 5:05. */
    val duration: String,
)

/** El backend respondió 400: rechazó los datos del track. */
class InvalidTrackException : Exception("El backend rechazó los datos del track")
