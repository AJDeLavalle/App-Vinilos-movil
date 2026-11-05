package co.edu.uniandes.vinilos.ui.collectors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.Collector
import co.edu.uniandes.vinilos.domain.model.Performer
import co.edu.uniandes.vinilos.ui.albums.errorMessage
import co.edu.uniandes.vinilos.ui.common.EmptySectionText
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.ErrorView
import co.edu.uniandes.vinilos.ui.common.InfoLine
import co.edu.uniandes.vinilos.ui.common.LoadingView
import co.edu.uniandes.vinilos.ui.common.SectionTitle
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

object CollectorDetailTags {
    const val CONTENT = "collector_detail"
    const val TITLE = "collector_detail_title"
    const val BACK = "collector_back_button"
    fun artist(id: Int) = "collector_artist_$id"
    fun album(id: Int) = "collector_album_$id"
}

@Composable
fun CollectorDetailScreen(
    viewModel: CollectorDetailViewModel,
    onBack: () -> Unit,
    onArtistClick: (Performer) -> Unit,
    onAlbumClick: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CollectorDetailContent(
        state = state,
        onRetry = viewModel::loadCollector,
        onBack = onBack,
        onArtistClick = onArtistClick,
        onAlbumClick = onAlbumClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorDetailContent(
    state: CollectorDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onArtistClick: (Performer) -> Unit,
    onAlbumClick: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.collector_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(CollectorDetailTags.BACK)) {
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
            CollectorDetailUiState.Loading ->
                LoadingView(stringResource(R.string.collector_detail_loading), modifier)
            is CollectorDetailUiState.Error -> {
                // Un id inexistente no se arregla reintentando: se informa y se ofrece volver.
                if (state.type == ErrorType.NOT_FOUND) {
                    ErrorView(
                        message = stringResource(R.string.collector_detail_not_found),
                        onRetry = onBack,
                        modifier = modifier,
                        actionLabel = stringResource(R.string.navigate_back),
                    )
                } else {
                    ErrorView(errorMessage(state.type), onRetry, modifier)
                }
            }
            is CollectorDetailUiState.Success ->
                CollectorDetail(state.collector, onArtistClick, onAlbumClick, modifier)
        }
    }
}

/** Maqueta P06: imagen centrada, nombre, contacto, gustos (artistas favoritos) y colección. */
@Composable
private fun CollectorDetail(
    collector: Collector,
    onArtistClick: (Performer) -> Unit,
    onAlbumClick: (Int) -> Unit,
    modifier: Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(CollectorDetailTags.CONTENT),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CollectorImage(Modifier.size(140.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = collector.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag(CollectorDetailTags.TITLE),
            )
        }
        item {
            if (collector.telephone.isNotBlank()) InfoLine(stringResource(R.string.collector_telephone, collector.telephone))
            if (collector.email.isNotBlank()) InfoLine(stringResource(R.string.collector_email, collector.email))
        }
        item {
            Spacer(Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.collector_favorites))
        }
        if (collector.favoritePerformers.isEmpty()) {
            item { EmptySectionText(stringResource(R.string.collector_no_favorites)) }
        } else {
            items(collector.favoritePerformers, key = { "artist_${it.id}" }) { performer ->
                LinkRow(
                    text = performer.name,
                    tag = CollectorDetailTags.artist(performer.id),
                    onClick = { onArtistClick(performer) },
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            SectionTitle(stringResource(R.string.collector_collection))
        }
        if (collector.albums.isEmpty()) {
            item { EmptySectionText(stringResource(R.string.collector_no_albums)) }
        } else {
            items(collector.albums, key = { "album_${it.id}" }) { album ->
                LinkRow(
                    text = album.name,
                    tag = CollectorDetailTags.album(album.id),
                    onClick = { onAlbumClick(album.id) },
                )
            }
        }
    }
}

@Composable
private fun LinkRow(text: String, tag: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .testTag(tag),
    )
}

@Preview(showBackground = true)
@Composable
private fun CollectorDetailPreview() {
    VinilosTheme {
        CollectorDetailContent(
            state = CollectorDetailUiState.Success(
                Collector(
                    id = 100,
                    name = "Manolo Bellon",
                    telephone = "3502457896",
                    email = "manollo@caracol.com.co",
                    favoritePerformers = listOf(Performer(100, "Rubén Blades", "")),
                    albumCount = 1,
                    albums = listOf(Album(100, "Buscando América", "", "1984-08-01T00:00:00.000Z", "", "Salsa", "Elektra")),
                )
            ),
            onRetry = {},
            onBack = {},
            onArtistClick = {},
            onAlbumClick = {},
        )
    }
}
