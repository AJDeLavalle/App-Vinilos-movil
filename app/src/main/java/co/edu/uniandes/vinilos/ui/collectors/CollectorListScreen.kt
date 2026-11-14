package co.edu.uniandes.vinilos.ui.collectors

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.Collector
import co.edu.uniandes.vinilos.ui.albums.errorMessage
import co.edu.uniandes.vinilos.ui.common.EmptyView
import co.edu.uniandes.vinilos.ui.common.ErrorView
import co.edu.uniandes.vinilos.ui.common.LoadingView
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

object CollectorListTags {
    const val LIST = "collector_list"
    fun item(collectorId: Int) = "collector_item_$collectorId"
}

@Composable
fun CollectorListScreen(
    viewModel: CollectorListViewModel,
    onCollectorClick: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CollectorListContent(state = state, onRetry = viewModel::loadCollectors, onCollectorClick = onCollectorClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorListContent(
    state: CollectorListUiState,
    onRetry: () -> Unit,
    onCollectorClick: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_collectors), fontWeight = FontWeight.Bold) })
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (state) {
            CollectorListUiState.Loading -> LoadingView(stringResource(R.string.collectors_loading), modifier)
            CollectorListUiState.Empty -> EmptyView(stringResource(R.string.collectors_empty), modifier)
            is CollectorListUiState.Error -> ErrorView(errorMessage(state.type), onRetry, modifier)
            is CollectorListUiState.Success -> CollectorList(state.collectors, onCollectorClick, modifier)
        }
    }
}

@Composable
private fun CollectorList(collectors: List<Collector>, onCollectorClick: (Int) -> Unit, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(CollectorListTags.LIST),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(collectors, key = { it.id }) { collector ->
            CollectorItem(collector = collector, onClick = { onCollectorClick(collector.id) })
            Spacer(Modifier.padding(4.dp))
        }
    }
}

@Composable
private fun CollectorItem(collector: Collector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(CollectorListTags.item(collector.id))
            .clickable(onClick = onClick),
    ) {
        // Maqueta P05: imagen, nombre, cantidad de álbumes y enlace "Ver detalles". El backend no
        // tiene la ciudad del coleccionista, así que no se muestra.
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CollectorImage(Modifier.size(80.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = collector.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pluralStringResource(R.plurals.collector_album_count, collector.albumCount, collector.albumCount),
                    style = MaterialTheme.typography.bodySmall,
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

/** El backend no guarda imagen del coleccionista: siempre se usa la de la maqueta. */
@Composable
fun CollectorImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_collector_placeholder),
        contentDescription = null,
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
    )
}

@Preview(showBackground = true)
@Composable
private fun CollectorListPreview() {
    VinilosTheme {
        CollectorListContent(
            state = CollectorListUiState.Success(
                listOf(Collector(100, "Manolo Bellon", "3502457896", "manollo@caracol.com.co", albumCount = 1))
            ),
            onRetry = {},
            onCollectorClick = {},
        )
    }
}
