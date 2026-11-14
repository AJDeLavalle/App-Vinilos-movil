package co.edu.uniandes.vinilos.ui.tracks

import co.edu.uniandes.vinilos.domain.model.NewTrack

// Reglas del formulario según docs/diseno/sprint-3/asociar-track.md.

const val TRACK_NAME_MAX_LENGTH = 100

/** En el orden de la pantalla: el foco va al primer campo inválido. */
enum class TrackField { NAME, DURATION }

enum class TrackFieldError { NAME_REQUIRED, NAME_TOO_LONG, DURATION_REQUIRED, DURATION_INVALID }

data class TrackForm(
    val name: String = "",
    val duration: String = "",
) {
    fun validate(field: TrackField): TrackFieldError? = when (field) {
        TrackField.NAME -> when {
            name.isBlank() -> TrackFieldError.NAME_REQUIRED
            name.trim().length > TRACK_NAME_MAX_LENGTH -> TrackFieldError.NAME_TOO_LONG
            else -> null
        }
        TrackField.DURATION -> {
            val text = duration.trim()
            when {
                text.isEmpty() -> TrackFieldError.DURATION_REQUIRED
                !DURATION.matches(text) || text.all { it == '0' || it == ':' } -> TrackFieldError.DURATION_INVALID
                else -> null
            }
        }
    }

    fun validateAll(): Map<TrackField, TrackFieldError> =
        TrackField.entries.mapNotNull { field -> validate(field)?.let { field to it } }.toMap()

    fun toNewTrack() = NewTrack(name = name.trim(), duration = duration.trim())

    private companion object {
        /** El backend acepta cualquier texto; sus datos usan m:ss, como 5:05. */
        val DURATION = Regex("""\d{1,3}:[0-5]\d""")
    }
}
