package com.r2h_widget.widget.music

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class MusicPlayerApp(
    val packageName: String,
    val label: String,
)

object MusicPlayerDiscovery {

    @Suppress("DEPRECATION")
    fun installedPlayers(context: Context): List<MusicPlayerApp> {
        val packageManager = context.packageManager
        val packages = linkedSetOf<String>()

        val musicIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_MUSIC)
        }

        packageManager.queryIntentActivities(
            musicIntent,
            PackageManager.MATCH_DEFAULT_ONLY,
        ).forEach { info ->
            info.activityInfo?.packageName?.let(packages::add)
        }

        val audioIntent = Intent(Intent.ACTION_VIEW).apply {
            type = "audio/*"
        }

        packageManager.queryIntentActivities(
            audioIntent,
            PackageManager.MATCH_DEFAULT_ONLY,
        ).forEach { info ->
            info.activityInfo?.packageName?.let(packages::add)
        }

        val mediaBrowserIntent = Intent(
            "android.media.browse.MediaBrowserService",
        )

        packageManager.queryIntentServices(
            mediaBrowserIntent,
            0,
        ).forEach { info ->
            info.serviceInfo?.packageName?.let(packages::add)
        }

        return packages
            .asSequence()
            .filter { it != context.packageName }
            .mapNotNull { packageName ->
                runCatching {
                    val applicationInfo =
                        packageManager.getApplicationInfo(packageName, 0)

                    if (!applicationInfo.enabled) {
                        return@runCatching null
                    }

                    MusicPlayerApp(
                        packageName = packageName,
                        label = packageManager
                            .getApplicationLabel(applicationInfo)
                            .toString()
                            .ifBlank { packageName },
                    )
                }.getOrNull()
            }
            .filterNotNull()
            .distinctBy { it.packageName }
            .sortedWith(
                compareBy(
                    String.CASE_INSENSITIVE_ORDER,
                    MusicPlayerApp::label,
                ),
            )
            .toList()
    }
}
