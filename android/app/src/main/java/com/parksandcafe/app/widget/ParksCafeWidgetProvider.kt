package com.parksandcafe.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.parksandcafe.app.PlaceType
import com.parksandcafe.app.R
import com.parksandcafe.app.WidgetActions


/**
 * Home-screen widget: two large Park / Cafe buttons for one-tap GPS save.
 */
class ParksCafeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val type = PlaceType.fromAction(intent.action) ?: return
        SavePlaceService.enqueue(context, type)
    }

    companion object {
        fun buildViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_parks_cafe)
            views.setOnClickPendingIntent(
                R.id.btn_park,
                pendingAction(context, WidgetActions.ACTION_SAVE_PARK, 1)
            )
            views.setOnClickPendingIntent(
                R.id.btn_cafe,
                pendingAction(context, WidgetActions.ACTION_SAVE_CAFE, 2)
            )
            return views
        }

        private fun pendingAction(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, ParksCafeWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
