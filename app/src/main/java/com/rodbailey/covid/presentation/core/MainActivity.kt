package com.rodbailey.covid.presentation.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rodbailey.covid.presentation.MainViewModel
import com.rodbailey.covid.presentation.main.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen()
        }

        viewModel.loadRegionList()
    }
}
