package co.edu.uniandes.vinilos.ui.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.Comment
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.domain.model.Track
import co.edu.uniandes.vinilos.ui.albums.AlbumCover
import co.edu.uniandes.vinilos.ui.albums.errorMessage
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.ErrorView
import co.edu.uniandes.vinilos.ui.common.LoadingView
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

object AlbumDetailTags {
    const val CONTENT = "album_detail"
    const val TITLE = "album_detail_title"
    const val BACK = "back_button"
    const val COMMENTS = "album_detail_comments"
    fun comment(id: Int) = "album_comment_$id"
}

@Composable
fun AlbumDetailScreen(
    viewModel: AlbumDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AlbumDetailContent(state = state, onRetry = viewModel::loadAlbum, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailContent(
    state: AlbumDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.album_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(AlbumDetailTags.BACK)) {
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
            AlbumDetailUiState.Loading ->
                LoadingView(stringResource(R.string.album_detail_loading), modifier)
            is AlbumDetailUiState.Error -> {
                // Un id inexistente no se arregla reintentando: se informa y se ofrece volver.
                if (state.type == ErrorType.NOT_FOUND) {
                    ErrorView(stringResource(R.string.album_detail_not_found), onBack, modifier)
                } else {
                    ErrorView(errorMessage(state.type), onRetry, modifier)
                }
            }
            is AlbumDetailUiState.Success -> AlbumDetail(state.album, modifier)
        }
    }
}

/** Maqueta P02: portada centrada, título, "Artista • Año", género, sello, tracks y comentarios. */
@Composable
private fun AlbumDetail(album: Album, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(AlbumDetailTags.CONTENT),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AlbumCover(album, Modifier.size(140.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag(AlbumDetailTags.TITLE),
            )
        }
        item {
            InfoLine(album.subtitle(" • ").ifBlank { stringResource(R.string.album_unknown_artist) })
            if (album.genre.isNotBlank()) InfoLine(stringResource(R.string.album_genre, album.genre))
            if (album.recordLabel.isNotBlank()) InfoLine(stringResource(R.string.album_record_label, album.recordLabel))
        }
        if (album.description.isNotBlank()) {
            item {
                Text(
                    text = album.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.album_tracks))
        }
        if (album.tracks.isEmpty()) {
            item { EmptySectionText(stringResource(R.string.album_no_tracks)) }
        } else {
            itemsIndexed(album.tracks, key = { _, track -> "track_${track.id}" }) { index, track ->
                TrackRow(position = index + 1, track = track)
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.album_comments), Modifier.testTag(AlbumDetailTags.COMMENTS))
        }
        if (album.comments.isEmpty()) {
            item { EmptySectionText(stringResource(R.string.album_no_comments)) }
        } else {
            items(album.comments, key = { "comment_${it.id}" }) { comment -> CommentRow(comment) }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@Composable
private fun EmptySectionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun InfoLine(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
private fun TrackRow(position: Int, track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = stringResource(R.string.album_track_item, position, track.name),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = track.duration,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Estrellas como texto; el lector de pantalla anuncia "Calificación: N de 5" en lugar de los símbolos. */
@Composable
private fun CommentRow(comment: Comment) {
    val ratingDescription = stringResource(R.string.album_comment_rating, comment.rating)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(AlbumDetailTags.comment(comment.id)),
    ) {
        Text(
            text = "★".repeat(comment.rating) + "☆".repeat(5 - comment.rating),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { contentDescription = ratingDescription },
        )
        Text(
            text = comment.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumDetailPreview() {
    VinilosTheme {
        AlbumDetailContent(
            state = AlbumDetailUiState.Success(
                Album(
                    100, "Buscando América", "", "1984-08-01T00:00:00.000Z",
                    "Primer álbum de la banda Seis del Solar.", "Salsa", "Elektra",
                    tracks = listOf(Track(1, "Decisiones", "5:05"), Track(2, "Desapariciones", "6:29")),
                    performers = listOf(Performer(1, "Rubén Blades", "")),
                    comments = listOf(Comment(1, "Un clásico de la salsa.", 5)),
                )
            ),
            onRetry = {},
            onBack = {},
        )
    }
}
