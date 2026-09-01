package com.r2h_widget.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.search.SearchTapAction
import com.r2h_widget.widget.search.SearchTarget
import com.r2h_widget.widget.search.SearchWidgetConfiguration

/**
 * Search studio: target app, hint, icons, bar styling, and the shared background
 * section. Tapping the finished widget launches the chosen search experience.
 */
@Composable
fun SearchWidgetEditor(
    configuration: SearchWidgetConfiguration,
    recentColors: List<String>,
    setConfiguration: (SearchWidgetConfiguration) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        EditorCard("Search", "Choose where a tap takes you.") {
            EnumProperty(
                label = "Opens",
                value = configuration.target.label,
                selected = configuration.target,
                options = SearchTarget.entries,
                optionLabel = { it.label },
                onSelected = { value -> setConfiguration(configuration.copy(target = value)) },
            )
            if (configuration.target == SearchTarget.CUSTOM_APP) {
                TextProperty(
                    label = "App package",
                    value = configuration.customAppPackage.orEmpty(),
                    onValueChange = { value -> setConfiguration(configuration.copy(customAppPackage = value.ifBlank { null })) },
                )
                Text(
                    "Example: com.android.chrome. If the app can't launch, the assistant opens instead.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            EnumProperty(
                label = "Tap behavior",
                value = configuration.tapAction.readableStudioLabel(),
                selected = configuration.tapAction,
                options = SearchTapAction.entries,
                optionLabel = { it.readableStudioLabel() },
                onSelected = { value -> setConfiguration(configuration.copy(tapAction = value)) },
            )
        }

        EditorCard("Bar", "Dial in what the bar says and shows.") {
            TextProperty(
                label = "Hint text",
                value = configuration.hintText,
                onValueChange = { value -> setConfiguration(configuration.copy(hintText = value)) },
            )
            ToggleControl("Search icon", configuration.showSearchIcon) { value ->
                setConfiguration(configuration.copy(showSearchIcon = value))
            }
            ToggleControl("Voice icon", configuration.showVoiceIcon) { value ->
                setConfiguration(configuration.copy(showVoiceIcon = value))
            }
            SliderControl("Text size", configuration.textScale, 0.7f..1.4f, ::formatStudioScale, step = 0.05f) { value ->
                setConfiguration(configuration.copy(textScale = value))
            }
            SliderControl("Padding", configuration.contentPaddingDp, 4f..32f, ::formatStudioNumber, step = 1f) { value ->
                setConfiguration(configuration.copy(contentPaddingDp = value))
            }
            ToggleControl("Align start", configuration.alignStart) { value ->
                setConfiguration(configuration.copy(alignStart = value))
            }
        }

        EditorCard("Colors", "Match the bar to your wallpaper.") {
            ColorProperty("Hint color", configuration.hintColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(hintColorHex = value))
            }
            ColorProperty("Icon color", configuration.iconColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(iconColorHex = value))
            }
        }

        BackgroundEditorSection(
            background = configuration.background,
            effects = com.r2h_widget.widget.common.WidgetEffectsConfiguration(),
            recentColors = recentColors,
            showEffects = false,
            setBackground = { value -> setConfiguration(configuration.copy(background = value)) },
            setEffects = { },
        )
    }
}
