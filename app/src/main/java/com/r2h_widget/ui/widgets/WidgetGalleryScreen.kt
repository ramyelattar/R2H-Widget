package com.r2h_widget.ui.widgets

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r2h_widget.catalog.WidgetCatalog
import com.r2h_widget.catalog.WidgetCategory
import com.r2h_widget.catalog.WidgetDefinition
import com.r2h_widget.ui.theme.AccentViolet
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.GlassOutline
import com.r2h_widget.ui.theme.GlassSurface
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary

/**
 * The simplified catalog: six base widgets, one per section. The chips filter by
 * product family; the list itself never grows preset duplicates.
 */
@Composable
fun WidgetGalleryScreen(
    selectedCategory: WidgetCategory,
    favoriteIds: Set<String>,
    onCategorySelected: (WidgetCategory) -> Unit,
    onWidgetSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val products = WidgetCatalog.forCategory(selectedCategory)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(
                start = AppSpacing.xxl,
                end = AppSpacing.xxl,
                top = AppSpacing.xxl,
            ),
        ) {
            Text(
                text = "Widgets",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Six base widgets, each one fully yours.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xxl, vertical = AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            WidgetCatalog.categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentViolet.copy(alpha = 0.18f),
                        selectedLabelColor = TextPrimary,
                        containerColor = GlassSurface,
                        labelColor = TextSecondary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == category,
                        borderColor = GlassOutline,
                        selectedBorderColor = AccentViolet.copy(alpha = 0.58f),
                    ),
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = AppSpacing.xxl,
                end = AppSpacing.xxl,
                bottom = AppSpacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            items(
                items = products,
                key = WidgetDefinition::id,
            ) { definition ->
                WidgetPreviewCard(
                    definition = definition,
                    isFavorite = definition.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(definition.id) },
                    onClick = { onWidgetSelected(definition.id) },
                )
            }
        }
    }
}

