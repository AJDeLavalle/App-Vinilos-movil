package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.remote.AlbumServiceAdapter
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.repository.AlbumRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlbumRepositoryImpl(
    private val serviceAdapter: AlbumServiceAdapter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AlbumRepository {

    override suspend fun getAlbums(): List<Album> = withContext(ioDispatcher) {
        serviceAdapter.getAlbums()
    }

    override suspend fun getAlbum(albumId: Int): Album = withContext(ioDispatcher) {
        serviceAdapter.getAlbum(albumId)
    }
}
