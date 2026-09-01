package com.r2h_widget.ui.widgets

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.r2h_widget.ui.theme.AppCorners
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.GlassOutline
import com.r2h_widget.ui.theme.GlassSurface
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.music.MusicPlayerApp
import com.r2h_widget.widget.music.MusicPlayerDiscovery
import com.r2h_widget.widget.music.MusicSessionAccess
import com.r2h_widget.widget.music.MusicWidgetConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun MusicWidgetEditor(
    configuration: MusicWidgetConfiguration,
    setConfiguration: (MusicWidgetConfiguration) -> Unit,
) {
    val context = LocalContext.current

    var mediaAccessGranted by remember {
        mutableStateOf(
            MusicSessionAccess.isGranted(context),
        )
    }

    val mediaAccessLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            mediaAccessGranted =
                MusicSessionAccess.isGranted(context)
        }

    var players by remember {
        mutableStateOf<List<MusicPlayerApp>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        players =
            withContext(Dispatchers.IO) {
                MusicPlayerDiscovery.installedPlayers(
                    context.applicationContext,
                )
            }

        loading = false
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(AppSpacing.md),
    ) {
        MusicEditorCard(
            title = "Media access",
            subtitle =
                if (mediaAccessGranted) {
                    "Enabled. Live metadata and playback controls are available."
                } else {
                    "Required to read and control the selected player's media session."
                },
        ) {
            if (!mediaAccessGranted) {
                OutlinedButton(
                    onClick = {
                        mediaAccessLauncher.launch(
                            MusicSessionAccess.settingsIntent(
                                context,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Enable media access")
                }
            }
        }

        MusicEditorCard(
            title = "Music player",
            subtitle =
                "Choose the app this widget controls.",
        ) {
            Box {
                OutlinedButton(
                    onClick = {
                        if (
                            !loading &&
                            players.isNotEmpty()
                        ) {
                            menuExpanded = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        when {
                            loading ->
                                "Searching for players…"

                            configuration
                                .selectedPlayerLabel
                                ?.isNotBlank() ==
                                true ->
                                configuration
                                    .selectedPlayerLabel!!

                            players.isEmpty() ->
                                "No media players detected"

                            else ->
                                "Choose player"
                        },
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                    },
                ) {
                    players.forEach { player ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        player.label,
                                        color = TextPrimary,
                                    )

                                    Text(
                                        player.packageName,
                                        color = TextSecondary,
                                        style =
                                            MaterialTheme
                                                .typography
                                                .labelSmall,
                                    )
                                }
                            },
                            onClick = {
                                menuExpanded = false

                                setConfiguration(
                                    configuration.copy(
                                        selectedPlayerPackage =
                                            player.packageName,
                                        selectedPlayerLabel =
                                            player.label,
                                    ),
                                )
                            },
                        )
                    }
                }
            }

            configuration
                .selectedPlayerPackage
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(
                        text = it,
                        color = TextSecondary,
                        style =
                            MaterialTheme.typography.labelSmall,
                    )
                }
        }

        MusicEditorCard(
            title = "Layout & sizing",
            subtitle =
                "Change spacing and proportions of the widget.",
        ) {
            MusicSlider(
                label = "Content padding",
                value = configuration.contentPaddingDp,
                range = 6f..28f,
                suffix = " dp",
            ) {
                setConfiguration(
                    configuration.copy(
                        contentPaddingDp = it,
                    ),
                )
            }

            MusicSlider(
                label = "Album art size",
                value = configuration.albumArtSizeDp,
                range = 60f..120f,
                suffix = " dp",
            ) {
                setConfiguration(
                    configuration.copy(
                        albumArtSizeDp = it,
                    ),
                )
            }

            MusicSlider(
                label = "Album art radius",
                value =
                    configuration.albumArtCornerRadiusDp,
                range = 0f..30f,
                suffix = " dp",
            ) {
                setConfiguration(
                    configuration.copy(
                        albumArtCornerRadiusDp = it,
                    ),
                )
            }

            MusicSlider(
                label = "Text scale",
                value = configuration.textScale,
                range = 0.75f..1.4f,
                suffix = "×",
            ) {
                setConfiguration(
                    configuration.copy(
                        textScale = it,
                    ),
                )
            }

            MusicSlider(
                label = "Controls scale",
                value = configuration.controlScale,
                range = 0.75f..1.35f,
                suffix = "×",
            ) {
                setConfiguration(
                    configuration.copy(
                        controlScale = it,
                    ),
                )
            }

            MusicSlider(
                label = "Progress thickness",
                value =
                    configuration.progressThicknessDp,
                range = 2f..8f,
                suffix = " dp",
            ) {
                setConfiguration(
                    configuration.copy(
                        progressThicknessDp = it,
                    ),
                )
            }

            MusicSlider(
                label = "Widget corner radius",
                value =
                    configuration.background.cornerRadiusDp,
                range = 0f..40f,
                suffix = " dp",
            ) {
                setConfiguration(
                    configuration.copy(
                        background =
                            configuration.background.copy(
                                cornerRadiusDp = it,
                            ),
                    ),
                )
            }
        }

        MusicEditorCard(
            title = "Background",
            subtitle =
                "Customize the widget surface.",
        ) {
            MusicColorControl(
                label = "Background color",
                value =
                    configuration.background
                        .solidColorHex,
            ) {
                setConfiguration(
                    configuration.copy(
                        background =
                            configuration.background.copy(
                                solidColorHex = it,
                            ),
                    ),
                )
            }

            MusicSlider(
                label = "Transparency",
                value =
                    configuration.background.solidOpacity,
                range = 0.15f..1f,
                suffix = "%",
                displayValue = {
                    "${(it * 100f).toInt()}%"
                },
            ) {
                setConfiguration(
                    configuration.copy(
                        background =
                            configuration.background.copy(
                                solidOpacity = it,
                            ),
                    ),
                )
            }
        }

        MusicEditorCard(
            title = "Colors",
            subtitle =
                "Every visible part can use its own color.",
        ) {
            MusicColorControl(
                "Track title",
                configuration.titleColorHex,
            ) {
                setConfiguration(
                    configuration.copy(
                        titleColorHex = it,
                    ),
                )
            }

            MusicColorControl(
                "Artist",
                configuration.artistColorHex,
            ) {
                setConfiguration(
                    configuration.copy(
                        artistColorHex = it,
                    ),
                )
            }

            MusicColorControl(
                "Time",
                configuration.timeColorHex,
            ) {
                setConfiguration(
                    configuration.copy(
                        timeColorHex = it,
                    ),
                )
            }

            MusicColorControl(
                "Playback controls",
                configuration.controlColorHex,
            ) {
                setConfiguration(
                    configuration.copy(
                        controlColorHex = it,
                    ),
                )
            }

            MusicColorControl(
                "Progress",
                configuration.progressColorHex,
            ) {
                setConfiguration(
                    configuration.copy(
                        progressColorHex = it,
                    ),
                )
            }

            MusicColorControl(
                "Progress track",
                configuration.progressTrackColorHex,
            ) {
                setConfiguration(
                    configuration.copy(
                        progressTrackColorHex = it,
                    ),
                )
            }
        }

        MusicEditorCard(
            title = "Content",
        ) {
            MusicToggle(
                "Album art",
                configuration.showAlbumArt,
            ) {
                setConfiguration(
                    configuration.copy(
                        showAlbumArt = it,
                    ),
                )
            }

            MusicToggle(
                "Track title",
                configuration.showTrackTitle,
            ) {
                setConfiguration(
                    configuration.copy(
                        showTrackTitle = it,
                    ),
                )
            }

            MusicToggle(
                "Artist",
                configuration.showArtist,
            ) {
                setConfiguration(
                    configuration.copy(
                        showArtist = it,
                    ),
                )
            }

            MusicToggle(
                "Progress bar",
                configuration.showProgress,
            ) {
                setConfiguration(
                    configuration.copy(
                        showProgress = it,
                    ),
                )
            }

            MusicToggle(
                "Time",
                configuration.showTime,
            ) {
                setConfiguration(
                    configuration.copy(
                        showTime = it,
                    ),
                )
            }
        }

        MusicEditorCard(
            title = "Playback controls",
        ) {
            MusicToggle(
                "Previous",
                configuration.showPrevious,
            ) {
                setConfiguration(
                    configuration.copy(
                        showPrevious = it,
                    ),
                )
            }

            MusicToggle(
                "Play / Pause",
                configuration.showPlayPause,
            ) {
                setConfiguration(
                    configuration.copy(
                        showPlayPause = it,
                    ),
                )
            }

            MusicToggle(
                "Next",
                configuration.showNext,
            ) {
                setConfiguration(
                    configuration.copy(
                        showNext = it,
                    ),
                )
            }
        }

        OutlinedButton(
            onClick = {
                val defaults =
                    MusicWidgetConfiguration()

                setConfiguration(
                    configuration.copy(
                        textScale =
                            defaults.textScale,
                        contentPaddingDp =
                            defaults.contentPaddingDp,
                        albumArtSizeDp =
                            defaults.albumArtSizeDp,
                        albumArtCornerRadiusDp =
                            defaults.albumArtCornerRadiusDp,
                        controlScale =
                            defaults.controlScale,
                        progressThicknessDp =
                            defaults.progressThicknessDp,

                        titleColorHex =
                            defaults.titleColorHex,
                        artistColorHex =
                            defaults.artistColorHex,
                        timeColorHex =
                            defaults.timeColorHex,
                        controlColorHex =
                            defaults.controlColorHex,
                        progressColorHex =
                            defaults.progressColorHex,
                        progressTrackColorHex =
                            defaults.progressTrackColorHex,

                        background =
                            defaults.background,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Reset appearance")
        }
    }
}

@Composable
private fun MusicEditorCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = GlassSurface,
        shape = RoundedCornerShape(AppCorners.card),
        border = BorderStroke(1.dp, GlassOutline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalArrangement =
                Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    title,
                    color = TextPrimary,
                    style =
                        MaterialTheme.typography.titleMedium,
                )

                if (subtitle != null) {
                    Text(
                        subtitle,
                        color = TextSecondary,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun MusicToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun MusicSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    suffix: String,
    displayValue: ((Float) -> String)? = null,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
        ) {
            Text(
                label,
                color = TextPrimary,
            )

            Text(
                text =
                    displayValue?.invoke(value)
                        ?: String.format(
                            Locale.US,
                            "%.1f%s",
                            value,
                            suffix,
                        ),
                color = TextSecondary,
                style =
                    MaterialTheme.typography.labelMedium,
            )
        }

        Slider(
            value = value.coerceIn(
                range.start,
                range.endInclusive,
            ),
            onValueChange = onValueChange,
            valueRange = range,
        )
    }
}

@Composable
private fun MusicColorControl(
    label: String,
    value: String,
    onColorChanged: (String) -> Unit,
) {
    var draft by remember(value) {
        mutableStateOf(value)
    }

    val presets =
        listOf(
            "#F7F5FB",
            "#A78BFA",
            "#38BDF8",
            "#34D399",
            "#FBBF24",
            "#FB7185",
            "#FF7A18",
            "#121216",
        )

    Column(
        verticalArrangement =
            Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            label,
            color = TextPrimary,
        )

        OutlinedTextField(
            value = draft,
            onValueChange = { entered ->
                draft = entered

                normalizeHex(entered)
                    ?.let(onColorChanged)
            },
            singleLine = true,
            label = {
                Text("Hex color")
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(9.dp),
        ) {
            presets.forEach { hex ->
                val swatch =
                    safeColor(hex)

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            swatch,
                            CircleShape,
                        )
                        .border(
                            width =
                                if (
                                    hex.equals(
                                        value,
                                        ignoreCase = true,
                                    )
                                ) {
                                    2.dp
                                } else {
                                    1.dp
                                },
                            color =
                                if (
                                    hex.equals(
                                        value,
                                        ignoreCase = true,
                                    )
                                ) {
                                    Color.White
                                } else {
                                    Color.White.copy(
                                        alpha = 0.18f,
                                    )
                                },
                            shape = CircleShape,
                        )
                        .clickable {
                            draft = hex
                            onColorChanged(hex)
                        },
                )
            }
        }
    }
}

private fun normalizeHex(
    value: String,
): String? {
    var candidate =
        value.trim().uppercase(Locale.US)

    if (!candidate.startsWith("#")) {
        candidate = "#$candidate"
    }

    return if (
        Regex("^#[0-9A-F]{6}$")
            .matches(candidate)
    ) {
        candidate
    } else {
        null
    }
}

private fun safeColor(
    value: String,
): Color =
    runCatching {
        Color(value.toColorInt())
    }.getOrDefault(Color.DarkGray)
