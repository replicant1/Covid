package com.rodbailey.covid.presentation.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rodbailey.covid.presentation.cachestats.CacheStatsScreen
import com.rodbailey.covid.presentation.main.MainScreen
import com.rodbailey.covid.presentation.navigation.CacheStatsRoute
import com.rodbailey.covid.presentation.navigation.MainRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = MainRoute) {
                composable<MainRoute> {
                    MainScreen(
                        onNavigateToCacheStats = { navController.navigate(CacheStatsRoute) }
                    )
                }
                composable<CacheStatsRoute> {
                    CacheStatsScreen(
                        onDismiss = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
