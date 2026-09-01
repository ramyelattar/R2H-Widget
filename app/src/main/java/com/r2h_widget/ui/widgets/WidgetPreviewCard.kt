package com.r2h_widget.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.r2h_widget.catalog.WidgetDefinition
import com.r2h_widget.catalog.WidgetSize
import com.r2h_widget.ui.theme.AccentViolet
import com.r2h_widget.ui.theme.AppCorners
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.GlassOutline
import com.r2h_widget.ui.theme.GlassSurface
import com.r2h_widget.ui.theme.TextMuted
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.battery.BatteryStateReader

/**
 * One wide feature card per product. The catalog shows six base widgets; each card
 * carries a live representative preview — real battery state included — instead of a
 * grid of near-identical presets.
 */
@Composable
fun WidgetPreviewCard(
    definition: WidgetDefinition,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    // Real device battery level for the Battery card; other products don't need it.
    val batteryState = if (definition.rendererKey == com.r2h_widget.catalog.WidgetPreviewRendererRegistry.BATTERY) {
        val context = LocalContext.current
        androidx.compose.runtime.remember { runCatching { BatteryStateReader.read(context) }.getOrNull() }
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.card))
            .background(GlassSurface)
            .border(1.dp, GlassOutline, RoundedCornerShape(AppCorners.card))
            .clickable(
                onClick = onClick,
                role = Role.Button,
            )
            .semantics { role = Role.Button },
    ) {
        Box(modifier = Modifier.padding(AppSpacing.md)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (definition.size == WidgetSize.TWO_BY_ONE) 2.6f else 1.55f)
                    .clip(RoundedCornerShape(AppCorners.button)),
            ) {
                WidgetPreviewRenderer(
                    definition = definition,
                    batteryState = batteryState,
                    live = true,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg)
                .padding(bottom = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = definition.displayName,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = definition.description,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
                Row(
                    modifier = Modifier.padding(top = AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = definition.size.label,
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.width(AppSpacing.sm))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AccentViolet.copy(alpha = 0.6f)),
                    )
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text(
                        text = "Fully customizable",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            FavoriteButton(
                isFavorite = isFavorite,
                onToggle = onToggleFavorite,
            )
        }
    }
}

@Composable
fun FavoriteButton(
    isFavorite: Boolean = false,
    onToggle: () -> Unit = {},
) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(48.dp)
            .semantics {
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites"
            },
    ) {
        Text(
            text = if (isFavorite) "♥" else "♡",
            color = if (isFavorite) AccentViolet else TextMuted,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

