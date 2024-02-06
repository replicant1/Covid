package com.rodbailey.covid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodbailey.covid.dom.RegionList
import com.rodbailey.covid.dom.Report
import com.rodbailey.covid.net.CovidAPI
import com.rodbailey.covid.net.CovidAPIClient
import com.rodbailey.covid.ui.MainViewModel
import com.rodbailey.covid.ui.theme.CovidTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    private val viewModel : MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val covidAPI = CovidAPIClient().getAPIClient()?.create(CovidAPI::class.java)
//        val call : Call<RegionList>? = covidAPI?.getRegions()
//        call?.enqueue(
//            object : Callback<RegionList> {
//                override fun onResponse(call: Call<RegionList>?, response: Response<RegionList>?) {
//                    println("*** onResponse: region count =  ${response?.body()?.regions?.size}")
//                    if (response != null) {
//                        for (region in  response.body().regions) {
//                            println("region = $region")
//                        }
//                    }
//                }
//
//                override fun onFailure(call: Call<RegionList>?, t: Throwable?) {
//                    println("*** onFailure: $t")
//                }
//            }
//        )

//        val call : Call<Report>? = covidAPI?.getReport(null)
//        call?.enqueue(
//            object : Callback<Report> {
//                override fun onResponse(call: Call<Report>?, response: Response<Report>?) {
//                    println("** onResponse = response")
//                    if (response != null) {
//                        val reportData = response.body().data
//                        println("** reportData(null) = $reportData")
//                    }
//                }
//
//                override fun onFailure(call: Call<Report>?, t: Throwable?) {
//                    println("** onFailure $t")
//                }
//            }
//        )

        setContent {
            CovidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val searchText by viewModel.searchText.collectAsState()
                    val regions by viewModel.regions.collectAsState()
                    val isSearching by viewModel.isSearching.collectAsState()
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TextField(
                            value = searchText,
                            onValueChange = viewModel::onSearchTextChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "Search country")}
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                           items(regions) { region ->
                               Text(
                                   text = "${region.name}",
                                   modifier = Modifier.fillMaxWidth().padding(16.dp))
                           }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CovidTheme {
        Greeting("Android")
    }
}