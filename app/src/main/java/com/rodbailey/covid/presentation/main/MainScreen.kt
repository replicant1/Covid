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
import com.rodbailey.covid.domain.toRegionCode
import com.rodbailey.covid.presentation.MainViewModel
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelOpenWithData
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelOpenWithLoading
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.LoadReportDataForGlobal
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.LoadReportDataForRegion
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.OnSearchTextChanged
import com.rodbailey.covid.presentation.Result.*
import com.rodbailey.covid.presentation.core.UIText
import com.rodbailey.covid.presentation.theme.CovidTheme

/**
 * Main (only) screen of the covid application. Stateful entry point — owns the ViewModel,
 * collects state, and delegates rendering to [MainScreenContent].
 */
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()

    // Error toast
    LaunchedEffect(viewModel) {
        viewModel.errorFlow.collect { uiText ->
            Toast.makeText(
                context,
                uiText.asString(context),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    MainScreenContent(
        uiState = uiState,
        onSearchTextChanged = { viewModel.processIntent(OnSearchTextChanged(it)) },
        onGlobalClicked = { viewModel.processIntent(LoadReportDataForGlobal) },
        onRegionClicked = { region ->
            viewModel.processIntent(
                LoadReportDataForRegion(UIText.DynamicString(region.name), region.toRegionCode())
            )
        },
        onDataPanelCollapsed = { viewModel.processIntent(MainViewModel.MainIntent.CollapseDataPanel) }
    )
}

/**
 * Stateless content for the main screen. Receives all state and callbacks as parameters,
 * making it independently previewable and testable without a ViewModel.
 *
 * @param uiState Current UI state
 * @param onSearchTextChanged Invoked when the search field text changes
 * @param onGlobalClicked Invoked when the global stats icon is tapped
 * @param onRegionClicked Invoked when a region list item is tapped
 * @param onDataPanelCollapsed Invoked when the data panel is tapped to collapse it
 */
@Composable
fun MainScreenContent(
    uiState: MainViewModel.UIState,
    onSearchTextChanged: (String) -> Unit,
    onGlobalClicked: () -> Unit,
    onRegionClicked: (Region) -> Unit,
    onDataPanelCollapsed: () -> Unit
) {
    CovidTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search text field with global icon at right
                TextField(
                    value = uiState.searchText,
                    onValueChange = onSearchTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(MainScreenTag.TAG_TEXT_SEARCH.tag),
                    trailingIcon = {
                        GlobalRegionIcon(clickCallback = onGlobalClicked)
                    },
                    placeholder = { Text(text = stringResource(R.string.search_field_hint)) }
                )

                // Tracks progress of region list loading
                LinearProgressIndicator(
                    modifier = Modifier
                        .testTag(MainScreenTag.TAG_PROGRESS_SEARCH.tag)
                        .fillMaxWidth()
                        .height(16.dp)
                        .alpha(if (uiState.matchingRegions is Loading) 1f else 0f)
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
                    when (val regions = uiState.matchingRegions) {
                        is Success -> items(regions.data) { region ->
                            RegionSearchResultItem(
                                region = region,
                                clickCallback = { onRegionClicked(region) }
                            )
                        }
                        else -> {}
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Data panel shows covid stats for current country or global.
                // Clicking on the data panel collapses it.
                AnimatedVisibility(visible = uiState.dataPanelUIState is DataPanelOpenWithData || uiState.dataPanelUIState is DataPanelOpenWithLoading) {
                    RegionDataPanel(
                        title = (uiState.dataPanelUIState as? DataPanelOpenWithData)?.reportDataTitle?.asString()
                            ?: "",
                        reportData = (uiState.dataPanelUIState as? DataPanelOpenWithData)?.reportData
                            ?: ReportData(),
                        clickCallback = onDataPanelCollapsed,
                        isLoading = uiState.dataPanelUIState is DataPanelOpenWithLoading
                    )
                }
            }
        }
    }
}
