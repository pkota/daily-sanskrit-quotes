package com.dailysanskritquotes.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object DailyQuote : Screen("daily_quote", "Today", Icons.Filled.Home)
    data object Favorites : Screen("favorites", "Favorites", Icons.Filled.Favorite)
    data object Search : Screen("search", "Search", Icons.Filled.Search)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val bottomNavItems = listOf(DailyQuote, Favorites, Search, Settings)
    }
}
