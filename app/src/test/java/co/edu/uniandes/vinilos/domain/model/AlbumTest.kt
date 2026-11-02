package co.edu.uniandes.vinilos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumTest {

    private fun album(
        releaseDate: String = "1984-08-01T00:00:00.000Z",
        performers: List<Performer> = listOf(Performer(1, "Rubén Blades", "")),
    ) = Album(
        id = 100,
        name = "Buscando América",
        cover = "",
        releaseDate = releaseDate,
        description = "",
        genre = "Salsa",
        recordLabel = "Elektra",
        performers = performers,
    )

    @Test
    fun releaseYear_tomaElAnioDeLaFechaIso() {
        assertEquals("1984", album().releaseYear)
    }

    @Test
    fun performerNames_variosInterpretes_seSeparanPorComa() {
        val album = album(
            performers = listOf(Performer(1, "Rubén Blades", ""), Performer(2, "Seis del Solar", "")),
        )

        assertEquals("Rubén Blades, Seis del Solar", album.performerNames)
    }

    @Test
    fun subtitle_conArtistaYAnio_losUneConElSeparador() {
        assertEquals("Rubén Blades · 1984", album().subtitle(" · "))
    }

    @Test
    fun subtitle_sinArtista_muestraSoloElAnio() {
        assertEquals("1984", album(performers = emptyList()).subtitle(" · "))
    }

    @Test
    fun subtitle_sinFecha_muestraSoloElArtista() {
        assertEquals("Rubén Blades", album(releaseDate = "").subtitle(" · "))
    }
}
