package co.edu.uniandes.vinilos.ui.create

import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class AlbumFormTest {

    private val today = SimpleDateFormat("dd/MM/yyyy", Locale.US).parse("15/06/2026")!!

    private val validForm = AlbumForm(
        name = "Buscando América",
        artist = Artist(100, "Rubén Blades", "", "", ArtistType.MUSICIAN, ""),
        genre = "Salsa",
        recordLabel = "Elektra",
        releaseDate = "01/08/1984",
        cover = "https://example.com/portada.jpg",
        description = "Primer álbum de Seis del Solar.",
    )

    @Test
    fun formularioValido_noTieneErrores() {
        assertTrue(validForm.validateAll(today).isEmpty())
    }

    @Test
    fun formularioVacio_marcaLosSieteCampos() {
        val errors = AlbumForm().validateAll(today)

        assertEquals(AlbumField.entries.toSet(), errors.keys)
    }

    @Test
    fun nombre_soloEspacios_esObligatorio() {
        assertEquals(AlbumFieldError.NAME_REQUIRED, validForm.copy(name = "   ").validate(AlbumField.NAME, today))
    }

    @Test
    fun nombre_respetaElLimiteDeCaracteres() {
        assertNull(validForm.copy(name = "a".repeat(100)).validate(AlbumField.NAME, today))
        assertEquals(
            AlbumFieldError.NAME_TOO_LONG,
            validForm.copy(name = "a".repeat(101)).validate(AlbumField.NAME, today),
        )
    }

    @Test
    fun genero_soloAceptaValoresDelBackend() {
        assertEquals(AlbumFieldError.GENRE_REQUIRED, validForm.copy(genre = "Pop").validate(AlbumField.GENRE, today))
    }

    @Test
    fun selloDiscografico_soloAceptaValoresDelBackend() {
        assertEquals(
            AlbumFieldError.RECORD_LABEL_REQUIRED,
            validForm.copy(recordLabel = "Otro").validate(AlbumField.RECORD_LABEL, today),
        )
    }

    @Test
    fun fecha_inexistenteOFormatoIncorrecto_esInvalida() {
        listOf("31/02/2020", "32/01/2020", "01/13/2020", "1/8/1984", "1984-08-01", "").forEach { date ->
            assertEquals(
                date,
                AlbumFieldError.DATE_INVALID,
                validForm.copy(releaseDate = date).validate(AlbumField.RELEASE_DATE, today),
            )
        }
    }

    @Test
    fun fecha_aceptaElVeintinueveDeFebreroEnBisiesto() {
        assertNull(validForm.copy(releaseDate = "29/02/2024").validate(AlbumField.RELEASE_DATE, today))
    }

    @Test
    fun fecha_futura_seRechaza_yHoySeAcepta() {
        assertEquals(
            AlbumFieldError.DATE_FUTURE,
            validForm.copy(releaseDate = "16/06/2026").validate(AlbumField.RELEASE_DATE, today),
        )
        assertNull(validForm.copy(releaseDate = "15/06/2026").validate(AlbumField.RELEASE_DATE, today))
    }

    @Test
    fun portada_exigeUrlHttp() {
        assertEquals(AlbumFieldError.COVER_REQUIRED, validForm.copy(cover = " ").validate(AlbumField.COVER, today))
        listOf("portada.jpg", "ftp://x.com/a.jpg", "http://", "https://con espacio.jpg").forEach { cover ->
            assertEquals(cover, AlbumFieldError.COVER_INVALID, validForm.copy(cover = cover).validate(AlbumField.COVER, today))
        }
        assertNull(validForm.copy(cover = "http://x.com/a.jpg").validate(AlbumField.COVER, today))
    }

    @Test
    fun descripcion_respetaElLimiteDeCaracteres() {
        assertEquals(
            AlbumFieldError.DESCRIPTION_REQUIRED,
            validForm.copy(description = "").validate(AlbumField.DESCRIPTION, today),
        )
        assertEquals(
            AlbumFieldError.DESCRIPTION_TOO_LONG,
            validForm.copy(description = "a".repeat(501)).validate(AlbumField.DESCRIPTION, today),
        )
    }

    @Test
    fun toNewAlbum_convierteLaFecha_yQuitaEspacios() {
        val album = validForm.copy(name = "  Buscando  ", cover = " https://x.com/a.jpg ").toNewAlbum()

        assertEquals("Buscando", album.name)
        assertEquals("https://x.com/a.jpg", album.cover)
        assertEquals("1984-08-01", album.releaseDate)
    }
}
