package co.edu.uniandes.vinilos.ui.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import co.edu.uniandes.vinilos.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VinilosTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backTag: String = "",
) {
    // Con la fuente grande el título usa dos líneas; la barra crece en lugar de recortarlo.
    val barHeight = TopAppBarDefaults.TopAppBarExpandedHeight *
        LocalDensity.current.fontScale.coerceAtLeast(1f)
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.testTag(backTag)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.navigate_back),
                    )
                }
            }
        },
        expandedHeight = barHeight,
    )
}
