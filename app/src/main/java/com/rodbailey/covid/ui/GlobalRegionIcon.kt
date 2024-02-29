package com.rodbailey.covid.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Icon at right of search field. When clicked it reveals the "Global" coffvid stats in the
 * [RegionDataPanel]
 */
@Composable
fun GlobalRegionIcon(clickCallback: () -> Unit) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Global",
        modifier = Modifier.clickable(onClick = clickCallback).testTag("tag.icon.global")
    )
}