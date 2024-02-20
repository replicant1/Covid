package com.rodbailey.covid

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.ui.MainViewModel
import com.rodbailey.covid.ui.theme.CovidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CovidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    
                    LaunchedEffect(Unit) {
                        viewModel.errorMessage.collect { message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG ).show()
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
                        TextField(
                            value = searchText,
                            onValueChange = viewModel::onSearchTextChanged,
                            modifier = Modifier.fillMaxWidth().testTag("tag.text.search"),
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
                                    clickCallback = { viewModel.
                                    loadReportDataForRegion(region.name, region.iso3Code) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Data panel shows covid stats for current country or global.
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
    }
}

@Composable
fun GlobalRegion(clickCallback: () -> Unit) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Global",
        modifier = Modifier.clickable(onClick = clickCallback).testTag("tag.icon.global")
    )
}

@Preview
@Composable
fun RegionSearchResultItemPreview() {
    RegionSearchResultItem(
        region = Region("ELO", "Electric Light Orchestra")
    ) {}
}

/**
 * @param region The country represented by this item
 * @param clickCallback Invoked when user clicks on this item
 */
@Composable
fun RegionSearchResultItem(region: Region, clickCallback: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable(
                onClick = clickCallback
            )
    ) {
        Icon(
            modifier = Modifier.padding(end = 8.dp),
            imageVector = Icons.Default.AccountBox,
            contentDescription = "Icon"
        )
        Text(
            text = region.name
        )
    }
}

@Preview
@Composable
fun RegionDataPanelPreviewNotLoading() {
    RegionDataPanel(
        title = "Big Country",
        reportData = ReportData(
            confirmed = 10000L,
            deaths = 200L,
            recovered = 1234L,
            active = 9876L,
            fatalityRate = 0.56F
        ),
        clickCallback = {},
        isLoading = false
    )
}

@Preview
@Composable
fun RegionDataPanelPreviewLoading() {
    RegionDataPanel(
        title = "Big Country",
        reportData = ReportData(
            confirmed = 10000L,
            deaths = 200L,
            recovered = 1234L,
            active = 9876L,
            fatalityRate = 0.56F
        ),
        clickCallback = {},
        isLoading = true
    )
}

/**
 * A cell in the data table part of a [RegionDataPanel]
 * @param text Textual contents of the table cell
 * @param weight Proportional width of this cell
 */
@Composable
fun RowScope.TableCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(8.dp)
    )
}

/**
 * Panel of covid statistics for a particular region.
 *
 * @param title Name of the region - appears above data table
 * @param reportData Covid stats to put in the data table
 * @param clickCallback Invoked when user clicks on this panel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionDataPanel(
    title: String,
    reportData: ReportData,
    clickCallback: () -> Unit,
    isLoading: Boolean
) {
    val tableData = mutableListOf<Pair<String, String>>()
    tableData.add(Pair("Confirmed:", "${reportData.confirmed}"))
    tableData.add(Pair("Deaths:", "${reportData.deaths}"))
    tableData.add(Pair("Active:", "${reportData.active}"))
    tableData.add(Pair("Fatality Rate:", "${reportData.fatalityRate}"))

    Card(onClick = clickCallback, modifier = Modifier.testTag("tag.card")) {
        Box() {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .alpha(if (isLoading) 1f else 0f)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Column(
                modifier = Modifier.alpha(if (isLoading) 0f else 1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 24.dp, bottom = 8.dp, top = 16.dp, end = 16.dp)
                        .testTag("tag.card.title")
                )
                LazyColumn(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {

                    items(tableData) {
                        val (id, text) = it
                        Row() {
                            TableCell(text = id, weight = .2f)
                            TableCell(text = text, weight = .2f)
                        }
                    } // items
                } // LazyColumn
            } // Column
        } // Box
    } // Card
}