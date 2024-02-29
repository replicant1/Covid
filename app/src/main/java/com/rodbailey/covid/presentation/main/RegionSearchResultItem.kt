package com.rodbailey.covid.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodbailey.covid.domain.Region

/**
 * @param region The country represented by this item
 * @param clickCallback Invoked when user clicks on this item
 */
@Composable
fun RegionSearchResultItem(region: Region, clickCallback: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .clickable(
                onClick = clickCallback
            )
    ) {
        Icon(
            modifier = Modifier.padding(start =  16.dp, end = 8.dp),
            imageVector = Icons.Default.AccountBox,
            contentDescription = "Icon"
        )
        Text(
            fontSize = 16.sp,
            text = region.name,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Preview()
@Composable
fun RegionSearchResultItemPreview() {
    RegionSearchResultItem(
        region = Region("ELO", "Electric Light Orchestra")
    ) {
        println()
    }
}