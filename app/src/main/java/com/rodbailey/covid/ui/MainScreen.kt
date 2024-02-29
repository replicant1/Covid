package com.rodbailey.covid.ui

import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodbailey.covid.ui.theme.CovidTheme

/**
 * Main (only) screen of the covid application. Search field at top of screen, list of matching
 * regions in middle of screen, animated data panel at bottom of screen showing covid statistics
 * for the selected region.
 */
@Composable
fun MainScreen() {
    CovidTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val context = LocalContext.current
            val viewModel : MainViewModel = viewModel()

            // Error toast
            LaunchedEffect(Unit) {
                viewModel.errorMessage.collect { message ->
                    Toast.makeText(
                        context,
                        message.asString(context),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            val searchText by viewModel.searchText.collectAsState()
            val regions by viewModel.matchingRegions.collectAsState()
            val reportData by viewModel.reportData.collectAsState()
            val reportDataTitle by viewModel.reportDataTitle.collectAsState()
            val isDataPanelExpanded by viewModel.isDataPanelExpanded.collectAsState()
            val isDataPanelLoading by viewModel.isDataPanelLoading.collectAsState()
            val isRegionListLoading by viewModel.isRegionListLoading.collectAsState()

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search text field with global icon at right
                TextField(
                    value = searchText,
                    onValueChange = viewModel::onSearchTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tag.text.search"),
                    trailingIcon = {
                        GlobalRegion() {
                            viewModel.loadReportDataForGlobal()
                        }
                    },
                    placeholder = { Text(text = "Search country") }
                )

                // Tracks progress of region list loading
                LinearProgressIndicator(
                    modifier = Modifier
                        .testTag("tag.progress.search")
                        .fillMaxWidth()
                        .height(16.dp)
                        .alpha(if (isRegionListLoading) 1f else 0f)
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // List of countries that match current contents of search field
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("tag.lazy.column.search")
                ) {
                    items(regions) { region ->
                        RegionSearchResultItem(
                            region = region,
                            clickCallback = {
                                viewModel.loadReportDataForRegion(region.name, region.iso3Code)
                            })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Data panel shows covid stats for current country or global.
                // Clicking on the data panel collapses it.
                AnimatedVisibility(visible = isDataPanelExpanded) {
                    RegionDataPanel(
                        title = reportDataTitle, reportData = reportData,
                        clickCallback = { viewModel.collapseDataPanel() },
                        isLoading = isDataPanelLoading
                    )
                }
            }
        }
    }
}