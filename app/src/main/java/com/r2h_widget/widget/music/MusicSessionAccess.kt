package com.r2h_widget.widget.music

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import android.os.SystemClock

data class MusicPlaybackSnapshot(
    val packageName: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArt: Bitmap?,
    val durationMs: Long,
    val positionMs: Long,
    val isPlaying: Boolean,
)

enum class MusicTransportCommand {
    PREVIOUS,
    PLAY_PAUSE,
    NEXT,
}

object MusicSessionAccess {

    fun listenerComponent(context: Context): ComponentName =
        ComponentName(
            context,
            MusicNotificationListenerService::class.java,
        )

    fun isGranted(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(NotificationManager::class.java)

        return notificationManager.isNotificationListenerAccessGranted(
            listenerComponent(context),
        )
    }

    fun settingsIntent(context: Context): Intent {
        val component = listenerComponent(context)

        val detailIntent =
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    component.flattenToString(),
                )

        return if (
            detailIntent.resolveActivity(context.packageManager) != null
        ) {
            detailIntent
        } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
    }

    fun activeController(
        context: Context,
        packageName: String?,
    ): MediaController? {
        val targetPackage =
            packageName?.trim()?.takeIf { it.isNotEmpty() }
                ?: return null

        if (!isGranted(context)) return null

        val manager =
            context.getSystemService(MediaSessionManager::class.java)

        return try {
            manager
                .getActiveSessions(listenerComponent(context))
                .firstOrNull { controller ->
                    controller.packageName == targetPackage
                }
        } catch (_: SecurityException) {
            null
        }
    }

    fun snapshot(
        context: Context,
        packageName: String?,
    ): MusicPlaybackSnapshot? {
        val controller =
            activeController(context, packageName)
                ?: return null

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        fun metadataText(vararg keys: String): String? {
            keys.forEach { key ->
                metadata
                    ?.getString(key)
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { return it }
            }

            return null
        }

        @Suppress("DEPRECATION")
        val artwork =
            metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val duration =
            metadata
                ?.getLong(MediaMetadata.METADATA_KEY_DURATION)
                ?.takeIf { it > 0L }
                ?: 0L

        val rawPosition =
            playbackState
                ?.position
                ?.coerceAtLeast(0L)
                ?: 0L

        val position =
            if (
                playbackState != null &&
                playbackState.state == PlaybackState.STATE_PLAYING &&
                playbackState.lastPositionUpdateTime > 0L
            ) {
                val elapsed =
                    (
                        SystemClock.elapsedRealtime() -
                            playbackState.lastPositionUpdateTime
                    ).coerceAtLeast(0L)

                (
                    rawPosition +
                        elapsed * playbackState.playbackSpeed
                )
                    .toLong()
                    .coerceAtLeast(0L)
                    .let { value ->
                        if (duration > 0L) {
                            value.coerceAtMost(duration)
                        } else {
                            value
                        }
                    }
            } else {
                rawPosition
            }

        return MusicPlaybackSnapshot(
            packageName = controller.packageName,

            title = metadataText(
                MediaMetadata.METADATA_KEY_TITLE,
                MediaMetadata.METADATA_KEY_DISPLAY_TITLE,
            ),

            artist = metadataText(
                MediaMetadata.METADATA_KEY_ARTIST,
                MediaMetadata.METADATA_KEY_ALBUM_ARTIST,
            ),

            album = metadataText(
                MediaMetadata.METADATA_KEY_ALBUM,
            ),

            albumArt = artwork,

            durationMs = duration,
            positionMs = position,

            isPlaying =
                playbackState?.state ==
                    PlaybackState.STATE_PLAYING,
        )
    }

    fun dispatch(
        context: Context,
        packageName: String?,
        command: MusicTransportCommand,
    ): Boolean {
        val controller =
            activeController(context, packageName)
                ?: return false

        val controls = controller.transportControls

        return try {
            when (command) {
                MusicTransportCommand.PREVIOUS -> {
                    controls.skipToPrevious()
                }

                MusicTransportCommand.NEXT -> {
                    controls.skipToNext()
                }

                MusicTransportCommand.PLAY_PAUSE -> {
                    if (
                        controller.playbackState?.state ==
                        PlaybackState.STATE_PLAYING
                    ) {
                        controls.pause()
                    } else {
                        controls.play()
                    }
                }
            }

            true
        } catch (_: RuntimeException) {
            false
        }
    }
}


