package co.edu.uniandes.vinilos.data.remote

import co.edu.uniandes.vinilos.data.remote.dto.ArtistDto
import co.edu.uniandes.vinilos.data.remote.dto.PerformerPrizeDto
import co.edu.uniandes.vinilos.data.remote.dto.PrizeDto
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistNotFoundException
import co.edu.uniandes.vinilos.domain.model.ArtistPrize
import co.edu.uniandes.vinilos.domain.model.ArtistType
import retrofit2.HttpException

/**
 * Service Adapter: traduce el contrato HTTP de BackVynils al modelo de dominio. Elige el recurso
 * según el tipo de artista y completa sus premios, que el backend entrega por separado.
 */
class ArtistServiceAdapter(private val api: ArtistApiService) {

    suspend fun getArtists(): List<Artist> {
        val musicians = api.getMusicians().mapNotNull { it.toDomain(ArtistType.MUSICIAN, emptyList()) }
        val bands = api.getBands().mapNotNull { it.toDomain(ArtistType.BAND, emptyList()) }
        // CASE_INSENSITIVE_ORDER compara sin crear una cadena en minúsculas en cada comparación.
        return (musicians + bands).sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    suspend fun getArtist(artistId: Int, type: ArtistType): Artist {
        val dto = try {
            when (type) {
                ArtistType.MUSICIAN -> api.getMusician(artistId)
                ArtistType.BAND -> api.getBand(artistId)
            }
        } catch (e: HttpException) {
            if (e.code() == 404) throw ArtistNotFoundException(artistId) else throw e
        }
        val prizes = if (dto.performerPrizes.isNullOrEmpty()) emptyList() else api.getPrizes()
        return dto.toDomain(type, prizes) ?: throw ArtistNotFoundException(artistId)
    }
}

/** Descarta registros sin id, que no se pueden navegar ni identificar. */
internal fun ArtistDto.toDomain(type: ArtistType, prizes: List<PrizeDto>): Artist? {
    val artistId = id ?: return null
    return Artist(
        id = artistId,
        name = name.orEmpty(),
        image = image.orEmpty(),
        description = description.orEmpty(),
        type = type,
        date = (birthDate ?: creationDate).orEmpty(),
        albums = albums.orEmpty().mapNotNull { it.toDomain() },
        prizes = performerPrizes.orEmpty().let { awards ->
            if (awards.isEmpty()) emptyList() else prizes.indexByAward().let { index -> awards.mapNotNull { it.toDomain(index) } }
        },
    )
}

/**
 * Índice id de premiación → premio, construido una vez por artista. Evita recorrer todos los
 * premios y sus premiaciones por cada premiación (O(n·m)).
 */
internal fun List<PrizeDto>.indexByAward(): Map<Int, PrizeDto> = buildMap {
    for (prize in this@indexByAward) {
        prize.performerPrizes.orEmpty().forEach { award -> award.id?.let { getOrPut(it) { prize } } }
    }
}

/** Busca el premio que contiene esta premiación; sin él no hay nombre que mostrar. */
internal fun PerformerPrizeDto.toDomain(prizeByAward: Map<Int, PrizeDto>): ArtistPrize? {
    val awardId = id ?: return null
    val prize = prizeByAward[awardId] ?: return null
    return ArtistPrize(
        id = awardId,
        name = prize.name.orEmpty(),
        organization = prize.organization.orEmpty(),
        premiationDate = premiationDate.orEmpty(),
    )
}
