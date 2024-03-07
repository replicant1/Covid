package com.rodbailey.covid.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rodbailey.covid.R

/**
 * Icon at right of search field. When clicked it reveals the "Global" covid stats in the
 * [RegionDataPanel]
 */
@Composable
fun GlobalRegionIcon(clickCallback: () -> Unit) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = stringResource(R.string.region_global),
        modifier = Modifier
            .clickable(onClick = clickCallback)
            .testTag(MainScreenTag.TAG_ICON_GLOBAL.tag)
    )
}

@Preview
@Composable
fun GlobalRegionIconPreview() {
    GlobalRegionIcon {}
}