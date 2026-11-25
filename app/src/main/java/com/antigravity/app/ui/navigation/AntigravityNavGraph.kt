package com.antigravity.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antigravity.app.AppContainer
import com.antigravity.app.ui.components.MiniPlayerBar
import com.antigravity.app.ui.screens.DpPadding
import com.antigravity.app.ui.screens.HomeScreen
import com.antigravity.app.ui.screens.HomeViewModel
import com.antigravity.app.ui.screens.PlayerScreen
import com.antigravity.app.ui.screens.PlayerViewModel
import com.antigravity.app.ui.screens.TodoScreen
import com.antigravity.app.ui.screens.TodoViewModel
import com.antigravity.app.ui.theme.AntigravityBackdrop
import com.antigravity.app.ui.theme.CyanAccent
import com.antigravity.app.ui.theme.CyanMuted
import com.antigravity.app.ui.theme.GlassBorder
import com.antigravity.app.ui.theme.GlassSurfaceElevated
import com.antigravity.app.ui.theme.TextMuted

@Composable
fun AntigravityNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.HOME.route

    val playbackState by container.audioController.playbackState.collectAsStateWithLifecycle()

    val navigateToTab: (Screen) -> Unit = { target ->
        navController.navigate(target.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    AntigravityBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            val showMiniPlayer = currentRoute != Screen.PLAYER.route && playbackState.hasActiveTrack
            val listBottomInset = if (showMiniPlayer) 172.dp else 96.dp

            NavHost(
                navController = navController,
                startDestination = Screen.HOME.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.HOME.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.factory(
                            taskRepository = container.taskRepository,
                            audioRepository = container.audioRepository,
                            audioController = container.audioController
                        )
                    )
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToTodo = { navigateToTab(Screen.TODO) },
                        onNavigateToPlayer = { navigateToTab(Screen.PLAYER) },
                        bottomContentPadding = DpPadding(listBottomInset)
                    )
                }

                composable(Screen.TODO.route) {
                    val todoViewModel: TodoViewModel = viewModel(
                        factory = TodoViewModel.factory(
                            taskRepository = container.taskRepository,
                            audioController = container.audioController
                        )
                    )
                    TodoScreen(
                        viewModel = todoViewModel,
                        bottomInsetPadding = listBottomInset
                    )
                }

                composable(Screen.PLAYER.route) {
                    val playerViewModel: PlayerViewModel = viewModel(
                        factory = PlayerViewModel.factory(
                            audioRepository = container.audioRepository,
                            audioController = container.audioController,
                            preferences = container.playbackPreferences
                        )
                    )
                    PlayerScreen(
                        viewModel = playerViewModel,
                        bottomInsetPadding = 104.dp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                MiniPlayerBar(
                    playbackState = playbackState,
                    visible = showMiniPlayer,
                    onExpandClick = { navigateToTab(Screen.PLAYER) },
                    onTogglePlayPause = { container.audioController.togglePlayPause() },
                    onNextClick = { container.audioController.next() }
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = GlassSurfaceElevated,
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        Screen.entries.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(screen) },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label
                                    )
                                },
                                label = { Text(screen.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyanAccent,
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = CyanMuted,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
