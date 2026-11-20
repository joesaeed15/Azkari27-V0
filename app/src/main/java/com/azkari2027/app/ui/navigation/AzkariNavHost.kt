package com.azkari2027.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.azkari2027.app.AzkariApp
import com.azkari2027.app.data.AppSettings
import com.azkari2027.app.ui.screens.*

private data class NavTab(val route: String, val icon: ImageVector, val labelAr: String, val labelEn: String)

@Composable
fun AzkariNavHost(
    app: AzkariApp,
    settings: AppSettings,
    initialCategoryId: Int
) {
    val navController = rememberNavController()
    var dbRefresh by remember { mutableIntStateOf(0) }

    val azkarCategories = remember(settings.languageId, dbRefresh) {
        app.azkariDb.getAzkarCategories(settings.languageId)
    }
    val tasbeehCategories = remember(settings.languageId, dbRefresh) {
        app.azkariDb.getTasbeehCategories(settings.languageId)
    }
    val allLogged = remember(settings.languageId, dbRefresh) {
        app.azkariDb.getAllLoggedCategories(settings.languageId)
    }

    val tabs = remember {
        listOf(
            NavTab("home", Icons.Rounded.MenuBook, "الأذكار", "Azkar"),
            NavTab("tasbeeh", Icons.Rounded.TouchApp, "السبحة", "Masbaha"),
            NavTab("ramadan", Icons.Rounded.DarkMode, "رمضان", "Ramadan"),
            NavTab("log", Icons.Rounded.BarChart, "السجل", "Log"),
            NavTab("settings", Icons.Rounded.Settings, "الإعدادات", "Settings")
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
            composable("splash") {
                SplashScreen(isArabic = settings.isArabic) {
                    val dest = if (initialCategoryId > 0) "reader/$initialCategoryId" else "home"
                    navController.navigate(dest) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }

            composable("home") {
                AzkarHomeScreen(
                    categories = azkarCategories,
                    settings = settings,
                    onCategoryClick = { cat -> navController.navigate("reader/${cat.id}") }
                )
            }

            composable(
                route = "reader/{catId}",
                arguments = listOf(navArgument("catId") { type = NavType.IntType })
            ) { entry ->
                val catId = entry.arguments?.getInt("catId") ?: 1
                val category = remember(catId) { app.azkariDb.getCategoryById(catId) }
                val prayers = remember(catId, settings.languageId) {
                    app.azkariDb.getPrayersForCategory(catId, category?.languageId ?: settings.languageId)
                }
                if (category != null) {
                    PrayerReaderScreen(
                        category = category,
                        prayers = prayers,
                        settings = settings,
                        audioController = app.audioController,
                        onSessionFinished = { delta, secs ->
                            app.azkariDb.incrementCategoryCountAndTime(category.id, delta, secs)
                            dbRefresh++
                        },
                        onBack = {
                            if (!navController.popBackStack()) {
                                navController.navigate("home")
                            }
                        }
                    )
                }
            }

            composable("tasbeeh") {
                TasbeehScreen(
                    tasabeeh = tasbeehCategories,
                    settings = settings,
                    audioController = app.audioController,
                    onIncrement = { catId, delta, secs ->
                        app.azkariDb.incrementCategoryCountAndTime(catId, delta, secs)
                        dbRefresh++
                    },
                    onAddTasbeeh = { text ->
                        app.azkariDb.addUserTasbeeh(text, settings.languageId)
                        dbRefresh++
                    },
                    onDeleteTasbeeh = { catId ->
                        app.azkariDb.deleteUserTasbeeh(catId)
                        dbRefresh++
                    }
                )
            }

            composable("ramadan") {
                RamadanScreen(ramadanDb = app.ramadanDb, settings = settings)
            }

            composable("log") {
                LogScreen(
                    allCategories = allLogged,
                    settings = settings,
                    onResetLogs = {
                        app.azkariDb.resetAllLogs(settings.languageId)
                        dbRefresh++
                    }
                )
            }

            composable("settings") {
                SettingsScreen(settings = settings, settingsStore = app.settingsStore)
            }
        }

        // Floating Obsidian & Gold Capsule Bottom Navigation Bar
        if (showBottomBar) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        TextButton(
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (settings.isArabic) tab.labelAr else tab.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tint,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
