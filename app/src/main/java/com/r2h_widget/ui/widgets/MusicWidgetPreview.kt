package com.r2h_widget.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.r2h_widget.widget.music.MusicPlaybackSnapshot
import com.r2h_widget.widget.music.MusicSessionAccess
import com.r2h_widget.widget.music.MusicWidgetConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun MusicWidgetPreview(
    modifier: Modifier = Modifier,
    configuration: MusicWidgetConfiguration =
        MusicWidgetConfiguration(),
) {
    val context = LocalContext.current

    var snapshot by remember(
        configuration.selectedPlayerPackage,
    ) {
        mutableStateOf<MusicPlaybackSnapshot?>(null)
    }

    LaunchedEffect(configuration.selectedPlayerPackage) {
        val packageName =
            configuration.selectedPlayerPackage

        if (packageName.isNullOrBlank()) {
            snapshot = null
            return@LaunchedEffect
        }

        while (true) {
            snapshot =
                withContext(Dispatchers.IO) {
                    MusicSessionAccess.snapshot(
                        context.applicationContext,
                        packageName,
                    )
                }

            delay(1_000L)
        }
    }

    fun color(
        value: String,
        fallback: Long,
    ): Color =
        runCatching {
            Color(value.toColorInt())
        }.getOrDefault(Color(fallback))

    val background =
        color(
            configuration.background.solidColorHex,
            0xFF121216,
        ).copy(
            alpha =
                configuration.background.solidOpacity
                    .coerceIn(0f, 1f),
        )

    val titleColor =
        color(configuration.titleColorHex, 0xFFF7F5FB)

    val artistColor =
        color(configuration.artistColorHex, 0xFFA7A4B5)

    val timeColor =
        color(configuration.timeColorHex, 0xFF8B8798)

    val controlColor =
        color(configuration.controlColorHex, 0xFFF7F5FB)

    val progressColor =
        color(configuration.progressColorHex, 0xFFA78BFA)

    val trackColor =
        color(configuration.progressTrackColorHex, 0xFF4A4656)

    val title =
        snapshot?.title
            ?: configuration.selectedPlayerLabel
            ?: "Music Player"

    val artist =
        snapshot?.artist
            ?: snapshot?.album
            ?: if (
                configuration.selectedPlayerPackage.isNullOrBlank()
            ) {
                "Choose a player"
            } else {
                "Nothing playing"
            }

    val duration =
        snapshot?.durationMs
            ?.coerceAtLeast(0L)
            ?: 0L

    val position =
        snapshot?.positionMs
            ?.coerceAtLeast(0L)
            ?: 0L

    val progressFraction =
        if (duration > 0L) {
            position
                .toFloat()
                .div(duration.toFloat())
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(
                RoundedCornerShape(
                    configuration.background.cornerRadiusDp.dp,
                ),
            )
            .background(background)
            .padding(configuration.contentPaddingDp.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(14.dp),
        ) {
            if (configuration.showAlbumArt) {
                val art = snapshot?.albumArt

                Box(
                    modifier = Modifier
                        .size(configuration.albumArtSizeDp.dp)
                        .clip(
                            RoundedCornerShape(
                                configuration
                                    .albumArtCornerRadiusDp.dp,
                            ),
                        )
                        .background(Color(0xFF24212B)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (art != null) {
                        Image(
                            bitmap = art.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = "♪",
                            color = controlColor.copy(alpha = 0.75f),
                            fontSize = 34.sp,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement =
                    Arrangement.SpaceBetween,
            ) {
                Column {
                    if (configuration.showTrackTitle) {
                        Text(
                            text = title,
                            color = titleColor,
                            fontSize =
                                (
                                    16f *
                                        configuration.textScale
                                ).sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (configuration.showArtist) {
                        Text(
                            text = artist,
                            color = artistColor,
                            fontSize =
                                (
                                    12f *
                                        configuration.textScale
                                ).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (configuration.showProgress) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    configuration
                                        .progressThicknessDp.dp,
                                )
                                .clip(CircleShape)
                                .background(trackColor),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        progressFraction,
                                    )
                                    .fillMaxHeight()
                                    .background(progressColor),
                            )
                        }

                        if (configuration.showTime) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = formatMusicTime(
                                        position,
                                    ),
                                    color = timeColor,
                                    fontSize =
                                        (
                                            9f *
                                                configuration
                                                    .textScale
                                        ).sp,
                                )

                                Text(
                                    text =
                                        if (duration > 0L) {
                                            formatMusicTime(
                                                duration,
                                            )
                                        } else {
                                            "--:--"
                                        },
                                    color = timeColor,
                                    fontSize =
                                        (
                                            9f *
                                                configuration
                                                    .textScale
                                        ).sp,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    val controlSize =
                        18f * configuration.controlScale

                    if (configuration.showPrevious) {
                        Text(
                            text = "|◀",
                            color = controlColor,
                            fontSize = controlSize.sp,
                        )
                    }

                    if (configuration.showPlayPause) {
                        Spacer(Modifier.width(24.dp))

                        Box(
                            modifier = Modifier
                                .size(
                                    (
                                        44f *
                                            configuration
                                                .controlScale
                                    ).dp,
                                )
                                .clip(CircleShape)
                                .background(
                                    controlColor.copy(
                                        alpha = 0.13f,
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text =
                                    if (
                                        snapshot?.isPlaying ==
                                        true
                                    ) {
                                        "Ⅱ"
                                    } else {
                                        "▶"
                                    },
                                color = controlColor,
                                fontSize = controlSize.sp,
                            )
                        }

                        Spacer(Modifier.width(24.dp))
                    }

                    if (configuration.showNext) {
                        Text(
                            text = "▶|",
                            color = controlColor,
                            fontSize = controlSize.sp,
                        )
                    }
                }
            }
        }
    }
}

private fun formatMusicTime(
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
