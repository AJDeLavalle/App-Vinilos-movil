package co.edu.uniandes.vinilos.ui.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.ui.common.EmptyView
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.ErrorView
import co.edu.uniandes.vinilos.ui.common.LoadingView
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

object AlbumListTags {
    const val LIST = "album_list"
    fun item(albumId: Int) = "album_item_$albumId"
}

@Composable
fun AlbumListScreen(
    viewModel: AlbumListViewModel,
    onAlbumClick: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AlbumListContent(state = state, onRetry = viewModel::loadAlbums, onAlbumClick = onAlbumClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumListContent(
    state: AlbumListUiState,
    onRetry: () -> Unit,
    onAlbumClick: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.albums_title), fontWeight = FontWeight.Bold) })
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (state) {
            AlbumListUiState.Loading -> LoadingView(stringResource(R.string.albums_loading), modifier)
            AlbumListUiState.Empty -> EmptyView(stringResource(R.string.albums_empty), modifier)
            is AlbumListUiState.Error -> ErrorView(errorMessage(state.type), onRetry, modifier)
            is AlbumListUiState.Success -> AlbumList(state.albums, onAlbumClick, modifier)
        }
    }
}

@Composable
fun errorMessage(type: ErrorType): String = when (type) {
    ErrorType.NETWORK -> stringResource(R.string.error_network)
    else -> stringResource(R.string.error_unknown)
}

@Composable
private fun AlbumList(albums: List<Album>, onAlbumClick: (Int) -> Unit, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(AlbumListTags.LIST),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumItem(album = album, onClick = { onAlbumClick(album.id) })
            Spacer(Modifier.padding(4.dp))
        }
    }
}

@Composable
private fun AlbumItem(album: Album, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(AlbumListTags.item(album.id))
            .clickable(onClick = onClick),
    ) {
        // Maqueta P01: portada, nombre, "Artista · Año" y enlace "Ver detalles".
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AlbumCover(album, Modifier.size(80.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = album.subtitle(" · ").ifBlank { stringResource(R.string.album_unknown_artist) },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.album_view_details),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun AlbumCover(album: Album, modifier: Modifier = Modifier) {
    val placeholder = painterResource(R.drawable.ic_album_placeholder)
    AsyncImage(
        model = album.cover.ifBlank { null },
        contentDescription = stringResource(R.string.album_cover_description, album.name),
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
    )
}

@Preview(showBackground = true)
@Composable
private fun AlbumListPreview() {
    VinilosTheme {
        AlbumListContent(
            state = AlbumListUiState.Success(
                listOf(
                    Album(100, "Buscando América", "", "1984-08-01T00:00:00.000Z", "", "Salsa", "Elektra",
                        performers = listOf(Performer(1, "Rubén Blades", ""))),
                    Album(101, "Poeta del pueblo", "", "1984-08-01T00:00:00.000Z", "", "Salsa", "EMI"),
                )
            ),
            onRetry = {},
            onAlbumClick = {},
        )
    }
}
