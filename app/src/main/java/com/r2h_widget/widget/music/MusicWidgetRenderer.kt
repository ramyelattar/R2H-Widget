package com.r2h_widget.widget.music

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.r2h_widget.MainActivity
import com.r2h_widget.R
import com.r2h_widget.catalog.WidgetProductIds
import com.r2h_widget.widget.clock.EXTRA_EDIT_APP_WIDGET_ID
import com.r2h_widget.widget.clock.EXTRA_EDIT_PRODUCT_ID
import kotlin.math.roundToInt

object MusicWidgetRenderer {

    fun render(
        context: Context,
        appWidgetId: Int,
        configuration: MusicWidgetConfiguration,
        snapshot: MusicPlaybackSnapshot?,
        interactive: Boolean = true,
    ): RemoteViews {
        val views =
            RemoteViews(
                context.packageName,
                R.layout.widget_music,
            )

        val title =
            snapshot?.title
                ?: configuration.selectedPlayerLabel
                ?: "Music Player"

        val artist =
            snapshot?.artist
                ?: snapshot?.album
                ?: if (
                    configuration
                        .selectedPlayerPackage
                        .isNullOrBlank()
                ) {
                    "Choose a player in R2H Widget"
                } else {
                    "Nothing playing"
                }

        val titleColor =
            parseColor(
                configuration.titleColorHex,
                Color.WHITE,
            )

        val artistColor =
            parseColor(
                configuration.artistColorHex,
                0xFFA7A4B5.toInt(),
            )

        val timeColor =
            parseColor(
                configuration.timeColorHex,
                0xFF8B8798.toInt(),
            )

        val controlColor =
            parseColor(
                configuration.controlColorHex,
                Color.WHITE,
            )

        val progressColor =
            parseColor(
                configuration.progressColorHex,
                0xFFA78BFA.toInt(),
            )

        val trackColor =
            parseColor(
                configuration.progressTrackColorHex,
                0xFF4A4656.toInt(),
            )

        val baseBackground =
            parseColor(
                configuration.background
                    .solidColorHex,
                0xFF121216.toInt(),
            )

        val backgroundColor =
            applyOpacity(
                baseBackground,
                configuration.background
                    .solidOpacity,
            )

        views.setInt(
            R.id.music_root,
            "setBackgroundColor",
            backgroundColor,
        )

        views.setBoolean(
            R.id.music_root,
            "setClipToOutline",
            true,
        )

        views.setViewOutlinePreferredRadius(
            R.id.music_root,
            configuration.background
                .cornerRadiusDp,
            TypedValue.COMPLEX_UNIT_DIP,
        )

        val paddingPx =
            dpToPx(
                context,
                configuration.contentPaddingDp,
            )

        views.setViewPadding(
            R.id.music_root,
            paddingPx,
            paddingPx,
            paddingPx,
            paddingPx,
        )

        views.setTextViewText(
            R.id.music_title,
            title,
        )

        views.setTextViewText(
            R.id.music_artist,
            artist,
        )

        views.setTextColor(
            R.id.music_title,
            titleColor,
        )

        views.setTextColor(
            R.id.music_artist,
            artistColor,
        )

        views.setTextColor(
            R.id.music_elapsed,
            timeColor,
        )

        views.setTextColor(
            R.id.music_duration,
            timeColor,
        )

        views.setTextColor(
            R.id.music_previous,
            controlColor,
        )

        views.setTextColor(
            R.id.music_play_pause,
            controlColor,
        )

        views.setTextColor(
            R.id.music_next,
            controlColor,
        )

        views.setTextViewTextSize(
            R.id.music_title,
            TypedValue.COMPLEX_UNIT_SP,
            16f * configuration.textScale,
        )

        views.setTextViewTextSize(
            R.id.music_artist,
            TypedValue.COMPLEX_UNIT_SP,
            12f * configuration.textScale,
        )

        views.setTextViewTextSize(
            R.id.music_elapsed,
            TypedValue.COMPLEX_UNIT_SP,
            9f * configuration.textScale,
        )

        views.setTextViewTextSize(
            R.id.music_duration,
            TypedValue.COMPLEX_UNIT_SP,
            9f * configuration.textScale,
        )

        val controlTextSize =
            18f * configuration.controlScale

        views.setTextViewTextSize(
            R.id.music_previous,
            TypedValue.COMPLEX_UNIT_SP,
            controlTextSize,
        )

        views.setTextViewTextSize(
            R.id.music_play_pause,
            TypedValue.COMPLEX_UNIT_SP,
            controlTextSize,
        )

        views.setTextViewTextSize(
            R.id.music_next,
            TypedValue.COMPLEX_UNIT_SP,
            controlTextSize,
        )

        views.setViewLayoutWidth(
            R.id.music_album_art,
            configuration.albumArtSizeDp,
            TypedValue.COMPLEX_UNIT_DIP,
        )

        views.setViewLayoutHeight(
            R.id.music_album_art,
            configuration.albumArtSizeDp,
            TypedValue.COMPLEX_UNIT_DIP,
        )

        views.setBoolean(
            R.id.music_album_art,
            "setClipToOutline",
            true,
        )

        views.setViewOutlinePreferredRadius(
            R.id.music_album_art,
            configuration.albumArtCornerRadiusDp,
            TypedValue.COMPLEX_UNIT_DIP,
        )

        views.setViewLayoutHeight(
            R.id.music_progress,
            configuration.progressThicknessDp,
            TypedValue.COMPLEX_UNIT_DIP,
        )

        views.setColorStateList(
            R.id.music_progress,
            "setProgressTintList",
            ColorStateList.valueOf(
                progressColor,
            ),
        )

        views.setColorStateList(
            R.id.music_progress,
            "setProgressBackgroundTintList",
            ColorStateList.valueOf(
                trackColor,
            ),
        )

        setVisible(
            views,
            R.id.music_album_art,
            configuration.showAlbumArt,
        )

        setVisible(
            views,
            R.id.music_title,
            configuration.showTrackTitle,
        )

        setVisible(
            views,
            R.id.music_artist,
            configuration.showArtist,
        )

        setVisible(
            views,
            R.id.music_progress,
            configuration.showProgress,
        )

        setVisible(
            views,
            R.id.music_time_row,
            configuration.showTime,
        )

        setVisible(
            views,
            R.id.music_previous,
            configuration.showPrevious,
        )

        setVisible(
            views,
            R.id.music_play_pause,
            configuration.showPlayPause,
        )

        setVisible(
            views,
            R.id.music_next,
            configuration.showNext,
        )

        val artwork =
            snapshot?.albumArt

        if (
            configuration.showAlbumArt &&
            artwork != null
        ) {
            views.setImageViewBitmap(
                R.id.music_album_art,
                scaledArtwork(artwork),
            )
        } else {
            views.setImageViewResource(
                R.id.music_album_art,
                R.drawable.ic_widget_music_note,
            )
        }

        val duration =
            snapshot?.durationMs
                ?.coerceAtLeast(0L)
                ?: 0L

        val position =
            snapshot?.positionMs
                ?.coerceIn(
                    0L,
                    duration
                        .takeIf { it > 0L }
                        ?: Long.MAX_VALUE,
                )
                ?: 0L

        val progress =
            if (duration > 0L) {
                (
                    position.toDouble() /
                        duration.toDouble() *
                        1000.0
                )
                    .toInt()
                    .coerceIn(0, 1000)
            } else {
                0
            }

        views.setProgressBar(
            R.id.music_progress,
            1000,
            progress,
            false,
        )

        views.setTextViewText(
            R.id.music_elapsed,
            formatTime(position),
        )

        views.setTextViewText(
            R.id.music_duration,
            if (duration > 0L) {
                formatTime(duration)
            } else {
                "--:--"
            },
        )

        views.setTextViewText(
            R.id.music_play_pause,
            if (snapshot?.isPlaying == true) {
                "Ⅱ"
            } else {
                "▶"
            },
        )

        if (interactive) {
            views.setOnClickPendingIntent(
                R.id.music_previous,
                actionPendingIntent(
                    context,
                    appWidgetId,
                    MusicWidgetReceiver.ACTION_PREVIOUS,
                    1,
                ),
            )

            views.setOnClickPendingIntent(
                R.id.music_play_pause,
                actionPendingIntent(
                    context,
                    appWidgetId,
                    MusicWidgetReceiver.ACTION_PLAY_PAUSE,
                    2,
                ),
            )

            views.setOnClickPendingIntent(
                R.id.music_next,
                actionPendingIntent(
                    context,
                    appWidgetId,
                    MusicWidgetReceiver.ACTION_NEXT,
                    3,
                ),
            )

            views.setOnClickPendingIntent(
                R.id.music_root,
                editPendingIntent(
                    context,
                    appWidgetId,
                ),
            )
        }

        return views
    }

    private fun editPendingIntent(
        context: Context,
        appWidgetId: Int,
    ): PendingIntent {
        val intent =
            Intent(
                context,
                MainActivity::class.java,
            )
                .putExtra(
                    EXTRA_EDIT_APP_WIDGET_ID,
                    appWidgetId,
                )
                .putExtra(
                    EXTRA_EDIT_PRODUCT_ID,
                    WidgetProductIds.MUSIC,
                )
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP,
                )

        return PendingIntent.getActivity(
            context,
            appWidgetId * 10 + 9,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun actionPendingIntent(
        context: Context,
        appWidgetId: Int,
        action: String,
        offset: Int,
    ): PendingIntent {
        val intent =
            Intent(
                context,
                MusicWidgetReceiver::class.java,
            )
                .setAction(action)
                .putExtra(
                    android.appwidget
                        .AppWidgetManager
                        .EXTRA_APPWIDGET_ID,
                    appWidgetId,
                )

        return PendingIntent.getBroadcast(
            context,
            appWidgetId * 10 + offset,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun setVisible(
        views: RemoteViews,
        viewId: Int,
        visible: Boolean,
    ) {
        views.setViewVisibility(
            viewId,
            if (visible) {
                View.VISIBLE
            } else {
                View.GONE
            },
        )
    }

    private fun parseColor(
        value: String,
        fallback: Int,
    ): Int =
        runCatching {
            Color.parseColor(value)
        }.getOrDefault(fallback)

    private fun applyOpacity(
        color: Int,
        opacity: Float,
    ): Int {
        val alpha =
            (
                255f *
                    opacity.coerceIn(0f, 1f)
            ).roundToInt()

        return Color.argb(
            alpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color),
        )
    }

    private fun dpToPx(
        context: Context,
        value: Float,
    ): Int =
        (
            value *
                context.resources
                    .displayMetrics.density
        ).roundToInt()

    private fun formatTime(
        milliseconds: Long,
    ): String {
        val totalSeconds =
            (milliseconds / 1000L)
                .coerceAtLeast(0L)

        val minutes =
            totalSeconds / 60L

        val seconds =
            totalSeconds % 60L

        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    private fun scaledArtwork(
        bitmap: Bitmap,
    ): Bitmap {
        val maxSize = 384

        if (
            bitmap.width <= maxSize &&
            bitmap.height <= maxSize
        ) {
            return bitmap
        }

        val ratio =
            minOf(
                maxSize.toFloat() / bitmap.width,
                maxSize.toFloat() / bitmap.height,
            )

        val width =
            (bitmap.width * ratio)
                .toInt()
                .coerceAtLeast(1)

        val height =
            (bitmap.height * ratio)
                .toInt()
                .coerceAtLeast(1)

        return Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            true,
        )
    }
}
