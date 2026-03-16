package com.dailysanskritquotes

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.dailysanskritquotes.data.BundledQuoteImporter
import com.dailysanskritquotes.data.sync.QuoteSyncWorker
import com.dailysanskritquotes.domain.QuoteNotificationManager
import com.dailysanskritquotes.ui.navigation.Screen
import com.dailysanskritquotes.ui.screens.DailyQuoteScreen
import com.dailysanskritquotes.ui.screens.FavoritesScreen
import com.dailysanskritquotes.ui.screens.SearchScreen
import com.dailysanskritquotes.ui.screens.SettingsScreen
import com.dailysanskritquotes.ui.theme.DailySanskritQuotesTheme
import com.dailysanskritquotes.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bundledQuoteImporter: BundledQuoteImporter

    @Inject
    lateinit var quoteNotificationManager: QuoteNotificationManager

    private var fromNotification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fromNotification = intent?.getBooleanExtra("from_notification", false) == true

        // Schedule background sync worker
        QuoteSyncWorker.schedule(WorkManager.getInstance(this))

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            // First-launch initialization
            LaunchedEffect(Unit) {
                bundledQuoteImporter.importIfNeeded()
            }

            // Schedule default notification on first launch
            LaunchedEffect(Unit) {
                val prefs = getSharedPreferences("quote_prefs", MODE_PRIVATE)
                val isFirstLaunch = !prefs.contains("first_launch_done")
                if (isFirstLaunch) {
                    quoteNotificationManager.scheduleDailyNotification()
                    prefs.edit().putBoolean("first_launch_done", true).apply()
                }
            }

            // Handle orientation lock
            LaunchedEffect(settingsState.orientationLocked) {
                requestedOrientation = if (settingsState.orientationLocked) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            DailySanskritQuotesTheme(
                darkTheme = settingsState.darkModeEnabled,
                colorTheme = settingsState.colorTheme,
                textSizeOption = settingsState.textSize
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScaffold(
                        fromNotification = fromNotification,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppScaffold(
    fromNotification: Boolean = false,
    settingsViewModel: SettingsViewModel? = null
) {
    val navController = rememberNavController()

    // Handle notification deep link — navigate to daily quote
    LaunchedEffect(fromNotification) {
        if (fromNotification) {
            navController.navigate(Screen.DailyQuote.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Screen.bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.DailyQuote.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.DailyQuote.route) { DailyQuoteScreen() }
            composable(Screen.Favorites.route) { FavoritesScreen() }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Settings.route) {
                if (settingsViewModel != null) {
                    SettingsScreen(viewModel = settingsViewModel)
                } else {
                    SettingsScreen()
                }
            }
        }
    }
}
