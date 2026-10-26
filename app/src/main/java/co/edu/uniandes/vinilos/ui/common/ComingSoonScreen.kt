package co.edu.uniandes.vinilos.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import co.edu.uniandes.vinilos.R

/** Destinos de la barra inferior que se implementan en sprints posteriores (Artistas, Coleccionistas). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(title: String) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title, fontWeight = FontWeight.Bold) }) },
    ) { innerPadding ->
        EmptyView(stringResource(R.string.coming_soon), Modifier.padding(innerPadding))
    }
}
