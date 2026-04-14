package com.doronyg.mylightscontroller.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    accentColor: androidx.compose.ui.graphics.Color,
    error: String? = null,
    singleLine: Boolean = true,
    secure: Boolean = false
) {

    val isError = error != null

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        singleLine = singleLine,
        visualTransformation = if (secure) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        supportingText = {
            error?.let { Text(it) }
        },
        colors = OutlinedTextFieldDefaults.colors(
            // --- BORDER (this is the big fix) ---
            focusedBorderColor = accentColor,
            unfocusedBorderColor = accentColor.copy(alpha = 0.6f),

            errorBorderColor = MaterialTheme.colorScheme.error,

            // --- LABEL ---
            focusedLabelColor = accentColor,
            unfocusedLabelColor = accentColor.copy(alpha = 0.7f),

            errorLabelColor = MaterialTheme.colorScheme.error,

            // --- TEXT ---
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

            // --- CURSOR ---
            cursorColor = accentColor,

            // --- OPTIONAL BUT IMPORTANT (selection consistency) ---
            selectionColors = androidx.compose.foundation.text.selection.TextSelectionColors(
                handleColor = accentColor,
                backgroundColor = accentColor.copy(alpha = 0.3f)
            )
        ),
        modifier = modifier
    )
}