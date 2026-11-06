package co.edu.uniandes.vinilos.ui.artists

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.ui.albums.errorMessage
import co.edu.uniandes.vinilos.ui.common.EmptyView
import co.edu.uniandes.vinilos.ui.common.ErrorView
import co.edu.uniandes.vinilos.ui.common.LoadingView
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

object ArtistListTags {
    const val LIST = "artist_list"
    fun item(artistId: Int) = "artist_item_$artistId"
}

@Composable
fun ArtistListScreen(
    viewModel: ArtistListViewModel,
    onArtistClick: (Artist) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArtistListContent(state = state, onRetry = viewModel::loadArtists, onArtistClick = onArtistClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistListContent(
    state: ArtistListUiState,
    onRetry: () -> Unit,
    onArtistClick: (Artist) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_artists), fontWeight = FontWeight.Bold) })
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (state) {
            ArtistListUiState.Loading -> LoadingView(stringResource(R.string.artists_loading), modifier)
            ArtistListUiState.Empty -> EmptyView(stringResource(R.string.artists_empty), modifier)
            is ArtistListUiState.Error -> ErrorView(errorMessage(state.type), onRetry, modifier)
            is ArtistListUiState.Success -> ArtistList(state.artists, onArtistClick, modifier)
        }
    }
}

@Composable
private fun ArtistList(artists: List<Artist>, onArtistClick: (Artist) -> Unit, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(ArtistListTags.LIST),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(artists, key = { "${it.type}_${it.id}" }) { artist ->
            ArtistItem(artist = artist, onClick = { onArtistClick(artist) })
            Spacer(Modifier.padding(4.dp))
        }
    }
}

@Composable
private fun ArtistItem(artist: Artist, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(ArtistListTags.item(artist.id))
            .clickable(onClick = onClick),
    ) {
        // Maqueta P03: imagen, nombre, línea secundaria y enlace "Ver detalles". El backend no tiene
        // país ni género del artista, así que la línea muestra el tipo y el año.
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ArtistImage(artist, Modifier.size(80.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = artistSubtitle(artist),
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
private fun artistSubtitle(artist: Artist): String {
    val type = when (artist.type) {
        ArtistType.MUSICIAN -> stringResource(R.string.artist_type_musician)
        ArtistType.BAND -> stringResource(R.string.artist_type_band)
    }
    return listOf(type, artist.year).filter { it.isNotBlank() }.joinToString(" · ")
}

@Preview(showBackground = true)
@Composable
private fun ArtistListPreview() {
    VinilosTheme {
        ArtistListContent(
            state = ArtistListUiState.Success(
                listOf(
                    Artist(101, "Queen", "", "", ArtistType.BAND, "1970-01-01T00:00:00.000Z"),
                    Artist(100, "Rubén Blades", "", "", ArtistType.MUSICIAN, "1948-07-16T00:00:00.000Z"),
                )
            ),
            onRetry = {},
            onArtistClick = {},
        )
    }
}
