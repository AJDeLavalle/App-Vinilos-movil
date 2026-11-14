package co.edu.uniandes.vinilos.ui.create

import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.domain.model.ALBUM_GENRES
import co.edu.uniandes.vinilos.domain.model.ALBUM_RECORD_LABELS
import co.edu.uniandes.vinilos.domain.model.Artist
import co.edu.uniandes.vinilos.ui.common.SectionTitle
import co.edu.uniandes.vinilos.ui.theme.VinilosTheme

object CreateAlbumTags {
    const val SAVE = "create_album_save"
    const val BACK = "create_album_back"
    const val SUBMIT_ERROR = "create_album_submit_error"
    const val BACK_TO_CATALOG = "create_album_back_to_catalog"
    const val ARTISTS_RETRY = "create_album_artists_retry"
    fun field(field: AlbumField) = "create_album_${field.name.lowercase()}"
    fun error(field: AlbumField) = "create_album_error_${field.name.lowercase()}"
}

@Composable
fun CreateAlbumScreen(
    viewModel: CreateAlbumViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.submission) {
        if (state.submission == Submission.Saved) onSaved()
    }
    CreateAlbumContent(
        state = state,
        actions = CreateAlbumActions(
            onNameChange = viewModel::onNameChange,
            onArtistChange = viewModel::onArtistChange,
            onGenreChange = viewModel::onGenreChange,
            onRecordLabelChange = viewModel::onRecordLabelChange,
            onReleaseDateChange = viewModel::onReleaseDateChange,
            onCoverChange = viewModel::onCoverChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onFieldBlur = viewModel::onFieldBlur,
            onFocusHandled = viewModel::onFocusHandled,
            onRetryArtists = viewModel::loadArtists,
            onSave = viewModel::save,
            onBack = onBack,
            onFinish = onSaved,
        ),
    )
}

class CreateAlbumActions(
    val onNameChange: (String) -> Unit,
    val onArtistChange: (Artist) -> Unit,
    val onGenreChange: (String) -> Unit,
    val onRecordLabelChange: (String) -> Unit,
    val onReleaseDateChange: (String) -> Unit,
    val onCoverChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onFieldBlur: (AlbumField) -> Unit,
    val onFocusHandled: () -> Unit,
    val onRetryArtists: () -> Unit,
    val onSave: () -> Unit,
    val onBack: () -> Unit,
    /** Sale al catálogo cuando el álbum ya existe y el usuario desiste de asociar el artista. */
    val onFinish: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlbumContent(state: CreateAlbumUiState, actions: CreateAlbumActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_album_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack, modifier = Modifier.testTag(CreateAlbumTags.BACK)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val focusRequesters = remember { AlbumField.entries.associateWith { FocusRequester() } }
        LaunchedEffect(state.focusRequest) {
            state.focusRequest?.let { field ->
                focusRequesters.getValue(field).requestFocus()
                actions.onFocusHandled()
            }
        }
        val editable = !state.albumCreated && state.submission != Submission.Sending
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(stringResource(R.string.create_album_section))
            FormTextField(
                field = AlbumField.NAME,
                label = stringResource(R.string.create_album_name),
                value = state.form.name,
                onValueChange = actions.onNameChange,
                state = state,
                actions = actions,
                focusRequester = focusRequesters.getValue(AlbumField.NAME),
                enabled = editable,
            )
            ArtistField(state, actions, focusRequesters.getValue(AlbumField.ARTIST), editable)
            FormDropdown(
                field = AlbumField.GENRE,
                label = stringResource(R.string.create_album_genre),
                selected = state.form.genre,
                options = ALBUM_GENRES,
                optionLabel = { it },
                onSelect = actions.onGenreChange,
                state = state,
                actions = actions,
                focusRequester = focusRequesters.getValue(AlbumField.GENRE),
                enabled = editable,
            )
            FormDropdown(
                field = AlbumField.RECORD_LABEL,
                label = stringResource(R.string.create_album_record_label),
                selected = state.form.recordLabel,
                options = ALBUM_RECORD_LABELS,
                optionLabel = { it },
                onSelect = actions.onRecordLabelChange,
                state = state,
                actions = actions,
                focusRequester = focusRequesters.getValue(AlbumField.RECORD_LABEL),
                enabled = editable,
            )
            FormTextField(
                field = AlbumField.RELEASE_DATE,
                label = stringResource(R.string.create_album_release_date),
                placeholder = stringResource(R.string.create_album_release_date_hint),
                value = state.form.releaseDate,
                onValueChange = actions.onReleaseDateChange,
                state = state,
                actions = actions,
                focusRequester = focusRequesters.getValue(AlbumField.RELEASE_DATE),
                enabled = editable,
            )
            FormTextField(
                field = AlbumField.COVER,
                label = stringResource(R.string.create_album_cover),
                value = state.form.cover,
                onValueChange = actions.onCoverChange,
                state = state,
                actions = actions,
                focusRequester = focusRequesters.getValue(AlbumField.COVER),
                enabled = editable,
                keyboardType = KeyboardType.Uri,
            )
            FormTextField(
                field = AlbumField.DESCRIPTION,
                label = stringResource(R.string.create_album_description),
                value = state.form.description,
                onValueChange = actions.onDescriptionChange,
                state = state,
                actions = actions,
                focusRequester = focusRequesters.getValue(AlbumField.DESCRIPTION),
                enabled = editable,
                singleLine = false,
            )
            SubmitErrorMessage(state.submission)
            Spacer(Modifier.height(4.dp))
            SaveButton(state, actions)
            if (state.albumCreated && state.submission != Submission.Sending) {
                TextButton(
                    onClick = actions.onFinish,
                    modifier = Modifier.fillMaxWidth().testTag(CreateAlbumTags.BACK_TO_CATALOG),
                ) {
                    Text(stringResource(R.string.create_album_back_to_catalog))
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    field: AlbumField,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    state: CreateAlbumUiState,
    actions: CreateAlbumActions,
    focusRequester: FocusRequester,
    enabled: Boolean,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    val error = state.errors[field]
    var hadFocus by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = error?.let { { ErrorText(field, it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focus ->
                if (hadFocus && !focus.isFocused) actions.onFieldBlur(field)
                hadFocus = focus.isFocused
            }
            .testTag(CreateAlbumTags.field(field)),
    )
}

@Composable
private fun ArtistField(
    state: CreateAlbumUiState,
    actions: CreateAlbumActions,
    focusRequester: FocusRequester,
    enabled: Boolean,
) {
    when (val options = state.artistOptions) {
        is ArtistOptions.Loaded -> FormDropdown(
            field = AlbumField.ARTIST,
            label = stringResource(R.string.create_album_artist),
            selected = state.form.artist?.name.orEmpty(),
            options = options.artists,
            optionLabel = { it.name },
            onSelect = actions.onArtistChange,
            state = state,
            actions = actions,
            focusRequester = focusRequester,
            enabled = enabled,
        )
        // Estos dos estados también enlazan el FocusRequester: guardar sin artista pide foco aquí,
        // y requestFocus lanza una excepción si no está unido a ningún elemento.
        ArtistOptions.Loading -> Row(
            modifier = Modifier.focusRequester(focusRequester).focusable(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(Modifier.size(24.dp))
            Text(stringResource(R.string.artists_loading), style = MaterialTheme.typography.bodyMedium)
        }
        ArtistOptions.Error -> Column(Modifier.focusRequester(focusRequester).focusable()) {
            Text(
                text = stringResource(R.string.create_album_artists_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            OutlinedButton(
                onClick = actions.onRetryArtists,
                modifier = Modifier.testTag(CreateAlbumTags.ARTISTS_RETRY),
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FormDropdown(
    field: AlbumField,
    label: String,
    selected: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    state: CreateAlbumUiState,
    actions: CreateAlbumActions,
    focusRequester: FocusRequester,
    enabled: Boolean,
) {
    val error = state.errors[field]
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = error != null,
            supportingText = error?.let { { ErrorText(field, it) } },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled)
                .focusRequester(focusRequester)
                .testTag(CreateAlbumTags.field(field)),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ErrorText(field: AlbumField, error: AlbumFieldError) {
    Text(fieldErrorMessage(error), modifier = Modifier.testTag(CreateAlbumTags.error(field)))
}

@Composable
private fun fieldErrorMessage(error: AlbumFieldError): String = when (error) {
    AlbumFieldError.NAME_REQUIRED -> stringResource(R.string.create_album_error_name_required)
    AlbumFieldError.NAME_TOO_LONG -> stringResource(R.string.create_album_error_name_too_long, ALBUM_NAME_MAX_LENGTH)
    AlbumFieldError.ARTIST_REQUIRED -> stringResource(R.string.create_album_error_artist_required)
    AlbumFieldError.GENRE_REQUIRED -> stringResource(R.string.create_album_error_genre_required)
    AlbumFieldError.RECORD_LABEL_REQUIRED -> stringResource(R.string.create_album_error_record_label_required)
    AlbumFieldError.DATE_INVALID -> stringResource(R.string.create_album_error_date_invalid)
    AlbumFieldError.DATE_FUTURE -> stringResource(R.string.create_album_error_date_future)
    AlbumFieldError.COVER_REQUIRED -> stringResource(R.string.create_album_error_cover_required)
    AlbumFieldError.COVER_INVALID -> stringResource(R.string.create_album_error_cover_invalid)
    AlbumFieldError.DESCRIPTION_REQUIRED -> stringResource(R.string.create_album_error_description_required)
    AlbumFieldError.DESCRIPTION_TOO_LONG ->
        stringResource(R.string.create_album_error_description_too_long, ALBUM_DESCRIPTION_MAX_LENGTH)
}

@Composable
private fun SubmitErrorMessage(submission: Submission) {
    val failed = submission as? Submission.Failed ?: return
    val message = when (failed.error) {
        SubmitError.NETWORK -> stringResource(R.string.error_network)
        SubmitError.INVALID -> stringResource(R.string.create_album_error_invalid)
        SubmitError.UNKNOWN -> stringResource(R.string.error_unknown)
        SubmitError.ARTIST_LINK -> stringResource(R.string.create_album_error_artist_link)
    }
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.testTag(CreateAlbumTags.SUBMIT_ERROR),
    )
}

@Composable
private fun SaveButton(state: CreateAlbumUiState, actions: CreateAlbumActions) {
    val sending = state.submission == Submission.Sending
    Button(
        onClick = actions.onSave,
        enabled = !sending,
        modifier = Modifier.fillMaxWidth().testTag(CreateAlbumTags.SAVE),
    ) {
        if (sending) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            val label = if (state.albumCreated) R.string.create_album_retry_link else R.string.create_album_save
            Text(stringResource(label))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateAlbumPreview() {
    VinilosTheme {
        CreateAlbumContent(
            state = CreateAlbumUiState(artistOptions = ArtistOptions.Loaded(emptyList())),
            actions = CreateAlbumActions(
                {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
            ),
        )
    }
}
