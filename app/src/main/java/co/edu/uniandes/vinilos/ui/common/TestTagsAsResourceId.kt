package co.edu.uniandes.vinilos.ui.common

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

/** Expone los testTag de este árbol como resource-id para uiautomator y Firebase Robo. */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.exposeTestTagsAsResourceId(): Modifier = semantics { testTagsAsResourceId = true }
