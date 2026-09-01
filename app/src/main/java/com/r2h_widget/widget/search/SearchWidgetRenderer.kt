package com.r2h_widget.widget.search

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.RemoteViews
import com.r2h_widget.R
import com.r2h_widget.widget.WidgetEditAction
import com.r2h_widget.widget.common.BackgroundRenderRequest
import com.r2h_widget.widget.common.WidgetBackgroundRenderer

/**
 * Builds the launcher RemoteViews for the Search product: a styled bar that launches
 * the configured search target in one tap. The bar is a RemoteViews surface, so text
 * entry stays in the search experience the bar opens.
 */
object SearchWidgetRenderer {

    fun render(
        context: Context,
        configuration: SearchWidgetConfiguration,
        options: Bundle? = null,
        appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        productId: String = "search",
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_search)
        val density = context.resources.displayMetrics.density

        views.setImageViewBitmap(
            R.id.search_background,
            WidgetBackgroundRenderer.renderBlocking(
                BackgroundRenderRequest(
                    widthPx = ((options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250) ?: 250) * density).toInt().coerceAtLeast(1),
                    heightPx = ((options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 50) ?: 50) * density).toInt().coerceAtLeast(1),
                    density = density,
                    configuration = configuration.background,
                ),
            ),
        )

        views.setViewVisibility(R.id.search_icon, if (configuration.showSearchIcon) View.VISIBLE else View.GONE)
        views.setColorStateList(R.id.search_icon, "setImageTintList", ColorStateList.valueOf(parseColor(configuration.iconColorHex)))

        views.setTextViewText(R.id.search_hint, configuration.hintText.ifBlank { "Search" })
        views.setTextColor(R.id.search_hint, parseColor(configuration.hintColorHex))
        views.setTextViewTextSize(
            R.id.search_hint,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            15f * configuration.textScale,
        )

        views.setViewVisibility(R.id.search_voice_icon, if (configuration.showVoiceIcon) View.VISIBLE else View.GONE)
        views.setColorStateList(R.id.search_voice_icon, "setImageTintList", ColorStateList.valueOf(parseColor(configuration.iconColorHex)))

        val padding = (configuration.contentPaddingDp * density).toInt()
        views.setViewPadding(R.id.search_content, padding, padding / 2, padding, padding / 2)
        val gravity = if (configuration.alignStart) Gravity.START or Gravity.CENTER_VERTICAL else Gravity.CENTER
        views.setInt(R.id.search_content, "setGravity", gravity)

        val tapPendingIntent = when (configuration.tapAction) {
            SearchTapAction.RUN_SEARCH -> searchPendingIntent(context, configuration, appWidgetId)
            SearchTapAction.OPEN_EDITOR -> null
        } ?: WidgetEditAction.pendingEditIntent(context, appWidgetId, productId)
        if (tapPendingIntent != null) {
            views.setOnClickPendingIntent(R.id.search_root, tapPendingIntent)
        }
        return views
    }

    /**
     * The intent that runs the configured search. Web falls back from the system web
     * search activity to a browser URL; custom apps fall back to the voice assistant
     * when the package cannot launch.
     */
    internal fun searchIntentFor(context: Context, configuration: SearchWidgetConfiguration): Intent? = when (configuration.target) {
        SearchTarget.WEB -> {
            val webSearch = Intent(Intent.ACTION_WEB_SEARCH)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val resolves = webSearch.resolveActivity(context.packageManager) != null
            if (resolves) {
                webSearch
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q="))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        SearchTarget.ASSISTANT -> Intent(Intent.ACTION_VOICE_COMMAND)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        SearchTarget.CUSTOM_APP -> configuration.customAppPackage
            ?.let { context.packageManager.getLaunchIntentForPackage(it) }
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: Intent(Intent.ACTION_VOICE_COMMAND).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun searchPendingIntent(context: Context, configuration: SearchWidgetConfiguration, appWidgetId: Int): PendingIntent? {
        val intent = searchIntentFor(context, configuration) ?: return null
        return PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun parseColor(hex: String): Int = runCatching { Color.parseColor(hex) }.getOrDefault(Color.WHITE)
}
