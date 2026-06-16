package com.rodbailey.covid.presentation.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodbailey.covid.R

/**
 * Icon at right of search field. Tap to reveal global COVID stats; long-press to open
 * cache statistics. The long-press label is announced by TalkBack so the action is
 * discoverable without sight.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlobalRegionIcon(clickCallback: () -> Unit, longClickCallback: () -> Unit) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = stringResource(R.string.region_global),
        modifier = Modifier
            .combinedClickable(
                role = Role.Button,
                onClick = clickCallback,
                onLongClick = longClickCallback,
                onLongClickLabel = stringResource(R.string.cache_stats_title)
            )
            .testTag(MainScreenTag.TAG_ICON_GLOBAL.tag)
            .size(48.dp)
    )
}

@Preview
@Composable
fun GlobalRegionIconPreview() {
    GlobalRegionIcon(clickCallback = {}, longClickCallback = {})
}