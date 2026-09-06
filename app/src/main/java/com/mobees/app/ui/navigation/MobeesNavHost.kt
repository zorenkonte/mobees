package com.mobees.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.ui.detail.MovieDetailScreen
import com.mobees.app.ui.detail.TvDetailScreen
import com.mobees.app.ui.home.HomeScreen
import com.mobees.app.ui.saved.SavedScreen
import com.mobees.app.ui.search.SearchScreen
import com.mobees.app.ui.settings.SettingsScreen
import com.mobees.app.ui.status.StatusScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SAVED = "saved"
    const val MOVIE = "movie/{id}"
    const val TV = "tv/{id}"
    const val SETTINGS = "settings"
    const val STATUS = "status"

    fun movie(id: Int) = "movie/$id"
    fun tv(id: Int) = "tv/$id"
    fun forTitle(title: TitleSummary) = when (title.mediaType) {
        MediaType.MOVIE -> movie(title.id)
        MediaType.TV -> tv(title.id)
    }
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

private val topLevel = listOf(
    TopLevelDestination(Routes.HOME, "Home", Icons.Outlined.Home, Icons.Rounded.Home),
    TopLevelDestination(Routes.SEARCH, "Search", Icons.Outlined.Search, Icons.Rounded.Search),
    TopLevelDestination(Routes.SAVED, "Saved", Icons.Outlined.BookmarkBorder, Icons.Rounded.Bookmark),
)

@Composable
fun MobeesApp(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBar = topLevel.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                ) {
                    topLevel.forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (selected) dest.selectedIcon else dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        val openTitle: (TitleSummary) -> Unit = { navController.navigate(Routes.forTitle(it)) }
        NavHost(navController = navController, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenTitle = openTitle,
                    onOpenSearch = { navController.navigate(Routes.SEARCH) { launchSingleTop = true } },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                    onOpenStatus = { navController.navigate(Routes.STATUS) { launchSingleTop = true } },
                    contentPadding = padding,
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(onOpenTitle = openTitle, contentPadding = padding)
            }
            composable(Routes.SAVED) {
                SavedScreen(onOpenTitle = openTitle, contentPadding = padding)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenStatus = { navController.navigate(Routes.STATUS) { launchSingleTop = true } },
                )
            }
            composable(Routes.STATUS) {
                StatusScreen(onBack = { navController.popBackStack() })
            }
            composable(
                Routes.MOVIE,
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: return@composable
                MovieDetailScreen(movieId = id, onBack = { navController.popBackStack() }, onOpenTitle = openTitle)
            }
            composable(
                Routes.TV,
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: return@composable
                TvDetailScreen(tvId = id, onBack = { navController.popBackStack() })
            }
        }
    }
}
