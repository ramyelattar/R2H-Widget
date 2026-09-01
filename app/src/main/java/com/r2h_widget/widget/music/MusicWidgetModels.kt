package com.r2h_widget.widget.music

import com.r2h_widget.widget.common.WidgetBackgroundConfiguration

data class MusicWidgetConfiguration(
    val selectedPlayerPackage: String? = null,
    val selectedPlayerLabel: String? = null,

    val showAlbumArt: Boolean = true,
    val showTrackTitle: Boolean = true,
    val showArtist: Boolean = true,
    val showProgress: Boolean = true,
    val showTime: Boolean = true,

    val showPrevious: Boolean = true,
    val showPlayPause: Boolean = true,
    val showNext: Boolean = true,

    val textScale: Float = 1f,
    val contentPaddingDp: Float = 14f,

    val albumArtSizeDp: Float = 86f,
    val albumArtCornerRadiusDp: Float = 12f,
    val controlScale: Float = 1f,
    val progressThicknessDp: Float = 4f,

    val titleColorHex: String = "#F7F5FB",
    val artistColorHex: String = "#A7A4B5",
    val timeColorHex: String = "#8B8798",
    val controlColorHex: String = "#F7F5FB",
    val progressColorHex: String = "#A78BFA",
    val progressTrackColorHex: String = "#4A4656",

    val background: WidgetBackgroundConfiguration =
        WidgetBackgroundConfiguration(),
)
