package com.rodbailey.covid.presentation.main

import android.widget.Toast
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodbailey.covid.R
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.presentation.MainViewModel
import com.rodbailey.covid.presentation.MainViewModel.SecondUIState.DataPanelClosed
import com.rodbailey.covid.presentation.MainViewModel.SecondUIState.DataPanelLoaded
import com.rodbailey.covid.presentation.MainViewModel.SecondUIState.DataPanelLoading
import com.rodbailey.covid.presentation.MainViewModel.SecondUIState.RegionListLoading
import com.rodbailey.covid.presentation.core.UIText
import com.rodbailey.covid.presentation.theme.CovidTheme

/**
 * Main (only) screen of the covid application. Search field at top of screen, list of matching
 * regions in middle of screen, animated data panel at bottom of screen showing covid statistics
 * for the selected region or global statistics.
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
                viewModel.errorFlow.collect { uiText ->
                    Toast.makeText(
                        context,
                        uiText.asString(context),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            //val uiState by viewModel.uiState.collectAsState()

            val secondUIState by viewModel.secondUIState.collectAsState()

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search text field with global icon at right
                var contents = ""
                if (secondUIState is MainViewModel.SecondUIState.RegionListSearching) {
                    contents = (secondUIState as MainViewModel.SecondUIState.RegionListSearching).searchText
                }
                TextField(
                    value = contents,
                    onValueChange = viewModel::onSearchTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(MainScreenTag.TAG_TEXT_SEARCH.tag),
                    trailingIcon = {
                        GlobalRegionIcon() {
                            viewModel.loadReportDataForGlobal()
                        }
                    },
                    placeholder = { Text(text = stringResource(R.string.search_field_hint)) }
                )

                // Tracks progress of region list loading
                LinearProgressIndicator(
                    modifier = Modifier
                        .testTag(MainScreenTag.TAG_PROGRESS_SEARCH.tag)
                        .fillMaxWidth()
                        .height(16.dp)
                        .alpha(if (secondUIState is RegionListLoading) 1f else 0f)
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // List of countries that match current contents of search field
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag(MainScreenTag.TAG_LAZY_COLUMN_SEARCH.tag)
                ) {
                    var matchingRegions = emptyList<Region>()
                    if (secondUIState is MainViewModel.SecondUIState.RegionListSearching) {
                        matchingRegions = (secondUIState as MainViewModel.SecondUIState.RegionListSearching).matchingRegions
                    }
                    items(matchingRegions) { region ->
                        RegionSearchResultItem(
                            region = region,
                            clickCallback = {
                                viewModel.loadReportDataForRegion(UIText.DynamicString(region.name), region.iso3Code)
                            })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Data panel shows covid stats for current country or global.
                // Clicking on the data panel collapses it.
                if (secondUIState is DataPanelLoading) {
                    AnimatedVisibility(visible = true) {
                        RegionDataPanel(
                            title = "",
                            reportData = ReportData(),
                            clickCallback = { viewModel.collapseDataPanel() },
                            isLoading = true
                        )
                    }
                } else if (secondUIState is DataPanelLoaded) {
                    AnimatedVisibility(visible = true) {
                        RegionDataPanel(
                            title = (secondUIState as DataPanelLoaded).reportDataTitle.asString(),
                            reportData = (secondUIState as DataPanelLoaded).reportData,
                            clickCallback = { viewModel.collapseDataPanel() },
                            isLoading = false
                        )
                    }
                } else if (secondUIState is DataPanelClosed) {
                    // Do nothing
                }
            }
        }
    }
}