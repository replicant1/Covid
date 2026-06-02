package com.rodbailey.covid.presentation.main

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import androidx.compose.ui.tooling.preview.Preview
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelClosed

/**
 * Main (only) screen of the covid application. Stateful entry point — owns the ViewModel,
 * collects state, and delegates rendering to [MainScreenContent].
 */
@Composable
fun MainScreen(onNavigateToCacheStats: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: MainViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show errors in a Snackbar while the UI is at least STARTED. Using Snackbar rather
    // than Toast ensures the message respects edge-to-edge insets, inherits the app theme,
    // and cancels any in-flight message before showing the next one.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.errorFlow.collect { uiText ->
                snackbarHostState.showSnackbar(uiText.asString(context))
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Stable lambda references — captured only once per ViewModel lifetime so that
    // remember(region, onRegionClicked) inside the LazyColumn retains its per-item
    // callback across recompositions instead of always missing due to a new reference.
    val onSearchTextChanged: (String) -> Unit = remember(viewModel) {
        { viewModel.processIntent(OnSearchTextChanged(it)) }
    }
    val onGlobalClicked: () -> Unit = remember(viewModel) {
        { viewModel.processIntent(LoadReportDataForGlobal) }
    }
    val onRegionClicked: (Region) -> Unit = remember(viewModel) {
        { region ->
            viewModel.processIntent(
                LoadReportDataForRegion(UIText.DynamicString(region.name), region.toRegionCode())
            )
        }
    }
    val onDataPanelCollapsed: () -> Unit = remember(viewModel) {
        { viewModel.processIntent(MainViewModel.MainIntent.CollapseDataPanel) }
    }

    MainScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSearchTextChanged = onSearchTextChanged,
        onGlobalClicked = onGlobalClicked,
        onRegionClicked = onRegionClicked,
        onDataPanelCollapsed = onDataPanelCollapsed,
        onTripleTap = onNavigateToCacheStats
    )
}

/** Time window in milliseconds within which 3 taps must occur to count as a triple-tap. */
private const val TRIPLE_TAP_TIMEOUT_MS = 500L

/**
 * Stateless content for the main screen. Receives all state and callbacks as parameters,
 * making it independently previewable and testable without a ViewModel.
 *
 * @param uiState Current UI state
 * @param snackbarHostState Drives the Scaffold's SnackbarHost for error messages
 * @param onSearchTextChanged Invoked when the search field text changes
 * @param onGlobalClicked Invoked when the global stats icon is tapped
 * @param onRegionClicked Invoked when a region list item is tapped
 * @param onDataPanelCollapsed Invoked when the data panel is tapped to collapse it
 * @param onTripleTap Invoked when three taps are detected within [TRIPLE_TAP_TIMEOUT_MS]
 */

@Composable
fun MainScreenContent(
    uiState: MainViewModel.UIState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onSearchTextChanged: (String) -> Unit,
    onGlobalClicked: () -> Unit,
    onRegionClicked: (Region) -> Unit,
    onDataPanelCollapsed: () -> Unit,
    onTripleTap: () -> Unit = {}
) {
    // Triple-tap state tracked at the composable level so it survives recomposition.
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    // Observe pointer presses using PointerEventPass.Initial so child elements (search
    // field, list items, data panel) still receive all their own touch events unaffected.
    val tripleTapModifier = Modifier.pointerInput(onTripleTap) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type == PointerEventType.Press) {
                    val now = System.currentTimeMillis()
                    tapCount = if (now - lastTapTime > TRIPLE_TAP_TIMEOUT_MS) 1 else tapCount + 1
                    lastTapTime = now
                    if (tapCount >= 3) {
                        tapCount = 0
                        onTripleTap()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(tripleTapModifier),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    .then(if (uiState.matchingRegions !is Loading) Modifier.semantics { hideFromAccessibility() } else Modifier)
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
                    is Success -> items(regions.data, key = { it.iso3Code }) { region ->
                        val clickCallback =
                            remember(region, onRegionClicked) { { onRegionClicked(region) } }
                        RegionSearchResultItem(
                            region = region,
                            clickCallback = clickCallback
                        )
                    }

                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data panel shows covid stats for current country or global.
            // Clicking on the data panel collapses it.
            // lastVisibleState retains the most recent non-closed state so that
            // RegionDataPanel holds its content during the exit animation rather
            // than blanking immediately when dataPanelUIState becomes DataPanelClosed.
            var lastVisibleState by remember { mutableStateOf(uiState.dataPanelUIState) }
            LaunchedEffect(uiState.dataPanelUIState) {
                if (uiState.dataPanelUIState !is DataPanelClosed) {
                    lastVisibleState = uiState.dataPanelUIState
                }
            }
            AnimatedVisibility(visible = uiState.dataPanelUIState is DataPanelOpenWithData || uiState.dataPanelUIState is DataPanelOpenWithLoading) {
                RegionDataPanel(
                    dataPanelUIState = lastVisibleState,
                    clickCallback = onDataPanelCollapsed
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewRegions = listOf(
    Region("AUS", "Australia"),
    Region("BRA", "Brazil"),
    Region("CAN", "Canada"),
    Region("DEU", "Germany"),
    Region("IND", "India"),
)

private val previewReportData = ReportData(
    confirmed = 10_423_776L,
    deaths = 187_934L,
    recovered = 9_876_543L,
    active = 359_299L,
    fatalityRate = 0.018f
)

/** Initial state — region list is still loading from network/database. */
@Preview(showBackground = true, name = "Regions Loading")
@Composable
fun PreviewMainScreenRegionsLoading() {
    MainScreenContent(
        uiState = MainViewModel.UIState(
            matchingRegions = Loading,
            dataPanelUIState = DataPanelClosed
        ),
        onSearchTextChanged = {},
        onGlobalClicked = {},
        onRegionClicked = {},
        onDataPanelCollapsed = {}
    )
}

/** Regions loaded, search field empty, data panel closed. */
@Preview(showBackground = true, name = "Regions Loaded")
@Composable
fun PreviewMainScreenRegionsLoaded() {
    MainScreenContent(
        uiState = MainViewModel.UIState(
            searchText = "",
            matchingRegions = Success(previewRegions),
            dataPanelUIState = DataPanelClosed
        ),
        onSearchTextChanged = {},
        onGlobalClicked = {},
        onRegionClicked = {},
        onDataPanelCollapsed = {}
    )
}

/** User has typed in the search field, list is filtered to matching regions. */
@Preview(showBackground = true, name = "Search Active")
@Composable
fun PreviewMainScreenSearchActive() {
    MainScreenContent(
        uiState = MainViewModel.UIState(
            searchText = "a",
            matchingRegions = Success(
                previewRegions.filter { it.name.contains("a", ignoreCase = true) }
            ),
            dataPanelUIState = DataPanelClosed
        ),
        onSearchTextChanged = {},
        onGlobalClicked = {},
        onRegionClicked = {},
        onDataPanelCollapsed = {}
    )
}

/** User tapped a region — data panel is open and showing a loading spinner. */
@Preview(showBackground = true, name = "Data Panel Loading")
@Composable
fun PreviewMainScreenDataPanelLoading() {
    MainScreenContent(
        uiState = MainViewModel.UIState(
            matchingRegions = Success(previewRegions),
            dataPanelUIState = MainViewModel.DataPanelUIState.DataPanelOpenWithLoading
        ),
        onSearchTextChanged = {},
        onGlobalClicked = {},
        onRegionClicked = {},
        onDataPanelCollapsed = {}
    )
}

/** Data panel open with stats for a specific region. */
@Preview(showBackground = true, name = "Data Panel Open With Data")
@Composable
fun PreviewMainScreenDataPanelOpen() {
    MainScreenContent(
        uiState = MainViewModel.UIState(
            matchingRegions = Success(previewRegions),
            dataPanelUIState = MainViewModel.DataPanelUIState.DataPanelOpenWithData(
                reportDataTitle = UIText.DynamicString("Australia"),
                reportData = previewReportData
            )
        ),
        onSearchTextChanged = {},
        onGlobalClicked = {},
        onRegionClicked = {},
        onDataPanelCollapsed = {}
    )
}

/** Region list failed to load — network or database error. */
@Preview(showBackground = true, name = "Regions Error")
@Composable
fun PreviewMainScreenRegionsError() {
    MainScreenContent(
        uiState = MainViewModel.UIState(
            matchingRegions = Error(),
            dataPanelUIState = DataPanelClosed
        ),
        onSearchTextChanged = {},
        onGlobalClicked = {},
        onRegionClicked = {},
        onDataPanelCollapsed = {}
    )
}
