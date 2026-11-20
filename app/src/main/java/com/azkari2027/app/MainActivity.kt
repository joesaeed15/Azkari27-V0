package com.azkari2027.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.azkari2027.app.data.AppThemeMode
import com.azkari2027.app.ui.navigation.AzkariNavHost
import com.azkari2027.app.ui.theme.Azkari2027Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val highestMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
            if (highestMode != null) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = highestMode.modeId
                }
            }
        }

        val app = application as AzkariApp
        val initialCategoryId = intent?.getIntExtra("EXTRA_CATEGORY_ID", -1) ?: -1

        setContent {
            val settings by app.settingsStore.settingsFlow.collectAsState(initial = app.defaultSettings)
            val isDark = when (settings.themeMode) {
                AppThemeMode.AMOLED_DARK -> true
                AppThemeMode.PARCHMENT_LIGHT -> false
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            val layoutDir = if (settings.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                Azkari2027Theme(darkTheme = isDark, fontChoice = settings.fontFamily) {
                    AzkariNavHost(
                        app = app,
                        settings = settings,
                        initialCategoryId = initialCategoryId
                    )
                }
            }
        }
    }
}
