package co.edu.uniandes.vinilos.ui.tracks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.Album
import co.edu.uniandes.vinilos.domain.model.Track
import co.edu.uniandes.vinilos.ui.albums.errorMessage
import co.edu.uniandes.vinilos.ui.common.EmptySectionText
import co.edu.uniandes.vinilos.ui.common.ErrorType
import co.edu.uniandes.vinilos.ui.common.ErrorView
import co.edu.uniandes.vinilos.ui.common.LoadingView
import co.edu.uniandes.vinilos.ui.common.SectionTitle
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

object AssociateTrackTags {
    const val ALBUM = "associate_track_album"
    const val SAVE = "associate_track_save"
    const val BACK = "associate_track_back"
    const val SUBMIT_ERROR = "associate_track_submit_error"
    fun field(field: TrackField) = "associate_track_${field.name.lowercase()}"
    fun error(field: TrackField) = "associate_track_error_${field.name.lowercase()}"
}

@Composable
fun AssociateTrackScreen(
    viewModel: AssociateTrackViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.submission) {
        if (state.submission == TrackSubmission.Saved) onSaved()
    }
    AssociateTrackContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onDurationChange = viewModel::onDurationChange,
        onFieldBlur = viewModel::onFieldBlur,
        onFocusHandled = viewModel::onFocusHandled,
        onRetryAlbum = viewModel::loadAlbum,
        onSave = viewModel::save,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociateTrackContent(
    state: AssociateTrackUiState,
    onNameChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onFieldBlur: (TrackField) -> Unit,
    onFocusHandled: () -> Unit,
    onRetryAlbum: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.associate_track_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(AssociateTrackTags.BACK)) {
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
        when (val album = state.album) {
            AlbumLoad.Loading -> LoadingView(stringResource(R.string.album_detail_loading), modifier)
            is AlbumLoad.Error -> {
                if (album.type == ErrorType.NOT_FOUND) {
                    ErrorView(
                        message = stringResource(R.string.album_detail_not_found),
                        onRetry = onBack,
                        modifier = modifier,
                        actionLabel = stringResource(R.string.navigate_back),
                    )
                } else {
                    ErrorView(errorMessage(album.type), onRetryAlbum, modifier)
                }
            }
            is AlbumLoad.Loaded -> TrackFormBody(
                album = album.album,
                state = state,
                onNameChange = onNameChange,
                onDurationChange = onDurationChange,
                onFieldBlur = onFieldBlur,
                onFocusHandled = onFocusHandled,
                onSave = onSave,
                modifier = modifier,
            )
        }
    }
}

/** Maqueta P08: álbum seleccionado, nombre, duración, tracks existentes y botón de asociar. */
@Composable
private fun TrackFormBody(
    album: Album,
    state: AssociateTrackUiState,
    onNameChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onFieldBlur: (TrackField) -> Unit,
    onFocusHandled: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier,
) {
    val focusRequesters = remember { TrackField.entries.associateWith { FocusRequester() } }
    LaunchedEffect(state.focusRequest) {
        state.focusRequest?.let { field ->
            focusRequesters.getValue(field).requestFocus()
            onFocusHandled()
        }
    }
    val sending = state.submission == TrackSubmission.Sending
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.associate_track_album, album.name),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(AssociateTrackTags.ALBUM),
        )
        TrackTextField(
            field = TrackField.NAME,
            label = stringResource(R.string.associate_track_name),
            value = state.form.name,
            onValueChange = onNameChange,
            error = state.errors[TrackField.NAME],
            onBlur = onFieldBlur,
            focusRequester = focusRequesters.getValue(TrackField.NAME),
            enabled = !sending,
        )
        TrackTextField(
            field = TrackField.DURATION,
            label = stringResource(R.string.associate_track_duration),
            placeholder = stringResource(R.string.associate_track_duration_hint),
            value = state.form.duration,
            onValueChange = onDurationChange,
            error = state.errors[TrackField.DURATION],
            onBlur = onFieldBlur,
            focusRequester = focusRequesters.getValue(TrackField.DURATION),
            enabled = !sending,
        )
        Spacer(Modifier.height(4.dp))
        SectionTitle(stringResource(R.string.associate_track_existing))
        if (album.tracks.isEmpty()) {
            EmptySectionText(stringResource(R.string.album_no_tracks))
        } else {
            album.tracks.forEach { track -> ExistingTrackRow(track) }
        }
        SubmitErrorMessage(state.submission)
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onSave,
            enabled = !sending,
            modifier = Modifier.fillMaxWidth().testTag(AssociateTrackTags.SAVE),
        ) {
            if (sending) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.associate_track_title))
            }
        }
    }
}

@Composable
private fun TrackTextField(
    field: TrackField,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: TrackFieldError?,
    onBlur: (TrackField) -> Unit,
    focusRequester: FocusRequester,
    enabled: Boolean,
    placeholder: String? = null,
) {
    var hadFocus by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = error?.let {
            { Text(fieldErrorMessage(it), modifier = Modifier.testTag(AssociateTrackTags.error(field))) }
        },
        singleLine = true,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focus ->
                if (hadFocus && !focus.isFocused) onBlur(field)
                hadFocus = focus.isFocused
            }
            .testTag(AssociateTrackTags.field(field)),
    )
}

@Composable
private fun fieldErrorMessage(error: TrackFieldError): String = when (error) {
    TrackFieldError.NAME_REQUIRED -> stringResource(R.string.associate_track_error_name_required)
    TrackFieldError.NAME_TOO_LONG ->
        stringResource(R.string.associate_track_error_name_too_long, TRACK_NAME_MAX_LENGTH)
    TrackFieldError.DURATION_REQUIRED -> stringResource(R.string.associate_track_error_duration_required)
    TrackFieldError.DURATION_INVALID -> stringResource(R.string.associate_track_error_duration_invalid)
}

@Composable
private fun ExistingTrackRow(track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(track.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            text = track.duration,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SubmitErrorMessage(submission: TrackSubmission) {
    val failed = submission as? TrackSubmission.Failed ?: return
    val message = when (failed.error) {
        TrackSubmitError.NETWORK -> stringResource(R.string.error_network)
        TrackSubmitError.INVALID -> stringResource(R.string.associate_track_error_invalid)
        TrackSubmitError.ALBUM_NOT_FOUND -> stringResource(R.string.album_detail_not_found)
        TrackSubmitError.UNKNOWN -> stringResource(R.string.error_unknown)
    }
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.testTag(AssociateTrackTags.SUBMIT_ERROR),
    )
}

@Preview(showBackground = true)
@Composable
private fun AssociateTrackPreview() {
    VinilosTheme {
        AssociateTrackContent(
            state = AssociateTrackUiState(
                album = AlbumLoad.Loaded(
                    Album(
                        100, "Buscando América", "", "1984-08-01T00:00:00.000Z", "", "Salsa", "Elektra",
                        tracks = listOf(Track(1, "Decisiones", "5:05")),
                    )
                ),
            ),
            onNameChange = {},
            onDurationChange = {},
            onFieldBlur = {},
            onFocusHandled = {},
            onRetryAlbum = {},
            onSave = {},
            onBack = {},
        )
    }
}
