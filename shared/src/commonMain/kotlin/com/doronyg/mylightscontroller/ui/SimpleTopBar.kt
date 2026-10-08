package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ui.icons.ArrowBack

@Composable
fun SimpleTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val (backArrowColor, _) = lightOnColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    tint = backArrowColor,
                    imageVector = ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )

        actions()
    }
}