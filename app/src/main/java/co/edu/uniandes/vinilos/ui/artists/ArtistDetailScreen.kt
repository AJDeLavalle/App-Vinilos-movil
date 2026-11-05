package co.edu.uniandes.vinilos.ui.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.domain.model.ArtistPrize
import co.edu.uniandes.vinilos.domain.model.ArtistType
import co.edu.uniandes.vinilos.ui.albums.errorMessage
import co.edu.uniandes.vinilos.ui.common.EmptySectionText
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.ErrorView
import co.edu.uniandes.vinilos.ui.common.InfoLine
import co.edu.uniandes.vinilos.ui.common.LoadingView
import co.edu.uniandes.vinilos.ui.common.SectionTitle
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

object ArtistDetailTags {
    const val CONTENT = "artist_detail"
    const val TITLE = "artist_detail_title"
    const val BACK = "artist_back_button"
    const val PRIZES = "artist_detail_prizes"
    fun album(id: Int) = "artist_album_$id"
    fun prize(id: Int) = "artist_prize_$id"
}

@Composable
fun ArtistDetailScreen(
    viewModel: ArtistDetailViewModel,
    onBack: () -> Unit,
    onAlbumClick: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArtistDetailContent(
        state = state,
        onRetry = viewModel::loadArtist,
        onBack = onBack,
        onAlbumClick = onAlbumClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailContent(
    state: ArtistDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onAlbumClick: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.artist_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(ArtistDetailTags.BACK)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (state) {
            ArtistDetailUiState.Loading ->
                LoadingView(stringResource(R.string.artist_detail_loading), modifier)
            is ArtistDetailUiState.Error -> {
                // Un id inexistente no se arregla reintentando: se informa y se ofrece volver.
                if (state.type == ErrorType.NOT_FOUND) {
                    ErrorView(
                        message = stringResource(R.string.artist_detail_not_found),
                        onRetry = onBack,
                        modifier = modifier,
                        actionLabel = stringResource(R.string.navigate_back),
                    )
                } else {
                    ErrorView(errorMessage(state.type), onRetry, modifier)
                }
            }
            is ArtistDetailUiState.Success -> ArtistDetail(state.artist, onAlbumClick, modifier)
        }
    }
}

/** Maqueta P04: imagen centrada, nombre, datos del artista, álbumes y premios. */
@Composable
private fun ArtistDetail(artist: Artist, onAlbumClick: (Int) -> Unit, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(ArtistDetailTags.CONTENT),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ArtistImage(artist, Modifier.size(140.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = artist.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag(ArtistDetailTags.TITLE),
            )
        }
        if (artist.year.isNotBlank()) {
            item { InfoLine(artistSummary(artist)) }
        }
        if (artist.description.isNotBlank()) {
            item {
                Text(
                    text = artist.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.artist_albums))
        }
        if (artist.albums.isEmpty()) {
            item { EmptySectionText(stringResource(R.string.artist_no_albums)) }
        } else {
            items(artist.albums, key = { "album_${it.id}" }) { album ->
                AlbumRow(album, onClick = { onAlbumClick(album.id) })
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.artist_prizes), Modifier.testTag(ArtistDetailTags.PRIZES))
        }
        if (artist.prizes.isEmpty()) {
            item { EmptySectionText(stringResource(R.string.artist_no_prizes)) }
        } else {
            items(artist.prizes, key = { "prize_${it.id}" }) { prize -> PrizeRow(prize) }
        }
    }
}

@Composable
private fun artistSummary(artist: Artist): String = when (artist.type) {
    ArtistType.MUSICIAN -> stringResource(R.string.artist_musician_born, artist.year)
    ArtistType.BAND -> stringResource(R.string.artist_band_formed, artist.year)
}

@Composable
private fun ArtistImage(artist: Artist, modifier: Modifier = Modifier) {
    val placeholder = painterResource(R.drawable.ic_artist_placeholder)
    AsyncImage(
        model = artist.image.ifBlank { null },
        contentDescription = stringResource(R.string.artist_image_description, artist.name),
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
    )
}

@Composable
private fun AlbumRow(album: Album, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .testTag(ArtistDetailTags.album(album.id)),
    ) {
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = album.releaseYear,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrizeRow(prize: ArtistPrize) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(ArtistDetailTags.prize(prize.id)),
    ) {
        Text(prize.name, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = listOf(prize.organization, prize.year).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArtistDetailPreview() {
    VinilosTheme {
        ArtistDetailContent(
            state = ArtistDetailUiState.Success(
                Artist(
                    id = 101,
                    name = "Queen",
                    image = "",
                    description = "Banda británica de rock formada en Londres.",
                    type = ArtistType.BAND,
                    date = "1970-01-01T00:00:00.000Z",
                    albums = listOf(
                        Album(102, "A Night at the Opera", "", "1975-11-21T00:00:00.000Z", "", "Rock", "EMI"),
                    ),
                    prizes = listOf(ArtistPrize(100, "Grammy Award", "Recording Academy", "1978-12-10T00:00:00.000Z")),
                )
            ),
            onRetry = {},
            onBack = {},
            onAlbumClick = {},
        )
    }
}
