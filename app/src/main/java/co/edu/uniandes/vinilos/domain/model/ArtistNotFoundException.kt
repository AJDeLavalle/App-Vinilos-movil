package co.edu.uniandes.vinilos.domain.model

/** El backend respondió 404: no existe un artista con el identificador solicitado. */
class ArtistNotFoundException(val artistId: Int) :
    Exception("No existe el artista con id $artistId")
