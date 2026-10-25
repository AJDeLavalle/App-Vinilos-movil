package co.edu.uniandes.vinilos.domain.model

/** El backend respondió 404: no existe un álbum con el identificador solicitado. */
class AlbumNotFoundException(val albumId: Int) :
    Exception("No existe el álbum con id $albumId")
