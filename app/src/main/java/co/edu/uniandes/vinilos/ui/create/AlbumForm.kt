package co.edu.uniandes.vinilos.ui.create

import co.edu.uniandes.vinilos.domain.model.ALBUM_GENRES
import co.edu.uniandes.vinilos.domain.model.ALBUM_RECORD_LABELS
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.NewAlbum
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Reglas del formulario según docs/diseno/sprint-2/crear-album.md.

const val ALBUM_NAME_MAX_LENGTH = 100
const val ALBUM_DESCRIPTION_MAX_LENGTH = 500

/** En el orden de la pantalla: el foco va al primer campo inválido. */
enum class AlbumField { NAME, ARTIST, GENRE, RECORD_LABEL, RELEASE_DATE, COVER, DESCRIPTION }

enum class AlbumFieldError {
    NAME_REQUIRED,
    NAME_TOO_LONG,
    ARTIST_REQUIRED,
    GENRE_REQUIRED,
    RECORD_LABEL_REQUIRED,
    DATE_INVALID,
    DATE_FUTURE,
    COVER_REQUIRED,
    COVER_INVALID,
    DESCRIPTION_REQUIRED,
    DESCRIPTION_TOO_LONG,
}

data class AlbumForm(
    val name: String = "",
    val artist: Artist? = null,
    val genre: String = "",
    val recordLabel: String = "",
    /** Como lo escribe el usuario: DD/MM/AAAA. */
    val releaseDate: String = "",
    val cover: String = "",
    val description: String = "",
) {
    fun validate(field: AlbumField, today: Date): AlbumFieldError? = when (field) {
        AlbumField.NAME -> when {
            name.isBlank() -> AlbumFieldError.NAME_REQUIRED
            name.trim().length > ALBUM_NAME_MAX_LENGTH -> AlbumFieldError.NAME_TOO_LONG
            else -> null
        }
        AlbumField.ARTIST -> if (artist == null) AlbumFieldError.ARTIST_REQUIRED else null
        AlbumField.GENRE -> if (genre !in ALBUM_GENRES) AlbumFieldError.GENRE_REQUIRED else null
        AlbumField.RECORD_LABEL ->
            if (recordLabel !in ALBUM_RECORD_LABELS) AlbumFieldError.RECORD_LABEL_REQUIRED else null
        AlbumField.RELEASE_DATE -> {
            val date = parseDate(releaseDate.trim())
            when {
                date == null -> AlbumFieldError.DATE_INVALID
                date.after(today) -> AlbumFieldError.DATE_FUTURE
                else -> null
            }
        }
        AlbumField.COVER -> when {
            cover.isBlank() -> AlbumFieldError.COVER_REQUIRED
            !COVER_URL.matches(cover.trim()) -> AlbumFieldError.COVER_INVALID
            else -> null
        }
        AlbumField.DESCRIPTION -> when {
            description.isBlank() -> AlbumFieldError.DESCRIPTION_REQUIRED
            description.trim().length > ALBUM_DESCRIPTION_MAX_LENGTH -> AlbumFieldError.DESCRIPTION_TOO_LONG
            else -> null
        }
    }

    fun validateAll(today: Date): Map<AlbumField, AlbumFieldError> =
        AlbumField.entries.mapNotNull { field -> validate(field, today)?.let { field to it } }.toMap()

    /** Solo para un formulario que ya pasó [validateAll]. */
    fun toNewAlbum(): NewAlbum {
        val (day, month, year) = releaseDate.trim().split("/")
        return NewAlbum(
            name = name.trim(),
            cover = cover.trim(),
            releaseDate = "$year-$month-$day",
            description = description.trim(),
            genre = genre,
            recordLabel = recordLabel,
        )
    }

    private companion object {
        val DATE_PATTERN = Regex("""\d{2}/\d{2}/\d{4}""")
        val COVER_URL = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)

        // SimpleDateFormat y no java.time, que no existe en el minSdk 22 sin desugaring.
        // Sin lenient, una fecha como 31/02/2020 falla en lugar de pasar al 2 de marzo.
        fun parseDate(text: String): Date? {
            if (!DATE_PATTERN.matches(text)) return null
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }
            return try {
                format.parse(text)
            } catch (e: ParseException) {
                null
            }
        }
    }
}
