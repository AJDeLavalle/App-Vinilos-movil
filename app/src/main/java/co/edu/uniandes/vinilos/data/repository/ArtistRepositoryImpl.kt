package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.remote.ArtistServiceAdapter
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.domain.repository.ArtistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArtistRepositoryImpl(
    private val serviceAdapter: ArtistServiceAdapter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArtistRepository {

    override suspend fun getArtists(): List<Artist> = withContext(ioDispatcher) {
        serviceAdapter.getArtists()
    }

    override suspend fun getArtist(artistId: Int, type: ArtistType): Artist = withContext(ioDispatcher) {
        serviceAdapter.getArtist(artistId, type)
    }
}
