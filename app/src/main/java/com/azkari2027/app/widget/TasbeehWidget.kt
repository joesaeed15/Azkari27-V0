package com.azkari2027.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.azkari2027.app.AzkariApp
import kotlinx.coroutines.flow.first

class TasbeehWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as AzkariApp
        val settings = app.settingsStore.settingsFlow.first()
        val list = app.azkariDb.getTasbeehCategories(settings.languageId)
        val savedId = app.settingsStore.widgetTasbeehIdFlow.first()
        val current = list.firstOrNull { it.id == savedId } ?: list.firstOrNull()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF081C15))
                    .cornerRadius(22.dp)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = current?.name ?: "سبحان الله",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF5F2EA)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                Spacer(GlanceModifier.height(8.dp))
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(Color(0xFF132A23))
                        .cornerRadius(16.dp)
                        .clickable(actionRunCallback<IncrementTasbeehAction>())
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${current?.count ?: 0}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFE0B956)),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(GlanceModifier.height(6.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (settings.isArabic) "التسبيحة التالية ↺" else "Next Tasbeeh ↺",
                        modifier = GlanceModifier.clickable(actionRunCallback<NextTasbeehAction>()).padding(4.dp),
                        style = TextStyle(color = ColorProvider(Color(0xFF8FB8A8)), fontSize = 12.sp)
                    )
                }
            }
        }
    }
}

class IncrementTasbeehAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as AzkariApp
        val settings = app.settingsStore.settingsFlow.first()
        val list = app.azkariDb.getTasbeehCategories(settings.languageId)
        val savedId = app.settingsStore.widgetTasbeehIdFlow.first()
        val current = list.firstOrNull { it.id == savedId } ?: list.firstOrNull() ?: return
        app.azkariDb.incrementCategoryCountAndTime(current.id, 1, 1L)
        TasbeehWidget().update(context, glanceId)
    }
}

class NextTasbeehAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as AzkariApp
        val settings = app.settingsStore.settingsFlow.first()
        val list = app.azkariDb.getTasbeehCategories(settings.languageId)
        if (list.isEmpty()) return
        val savedId = app.settingsStore.widgetTasbeehIdFlow.first()
        val idx = list.indexOfFirst { it.id == savedId }.let { if (it < 0) 0 else it }
        val next = list[(idx + 1) % list.size]
        app.settingsStore.setWidgetTasbeehId(next.id)
        TasbeehWidget().update(context, glanceId)
    }
}

class TasbeehWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasbeehWidget()
}
