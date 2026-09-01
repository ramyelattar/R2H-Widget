package com.r2h_widget.ui.customization

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.r2h_widget.customization.CustomizationAction
import com.r2h_widget.customization.CustomizationUiState
import com.r2h_widget.customization.IconPackBundlePhase
import com.r2h_widget.customization.IconPackBundleStatus
import com.r2h_widget.customization.IconPackCatalog
import com.r2h_widget.customization.IconPackDefinition
import com.r2h_widget.customization.ThemeCatalog
import com.r2h_widget.customization.ThemeDefinition
import com.r2h_widget.customization.WallpaperCatalog
import com.r2h_widget.customization.WallpaperCategory
import com.r2h_widget.customization.WallpaperDefinition
import com.r2h_widget.customization.WallpaperTarget
import com.r2h_widget.ui.theme.AccentCyan
import com.r2h_widget.ui.theme.AccentViolet
import com.r2h_widget.ui.theme.AppCorners
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.GlassOutline
import com.r2h_widget.ui.theme.GlassSurface
import com.r2h_widget.ui.theme.GlassSurfaceStrong
import com.r2h_widget.ui.theme.TextMuted
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary

@Composable
fun IconPacksScreen(
    state: CustomizationUiState,
    onAction: (CustomizationAction) -> Unit,
) {
    val selectedPack = IconPackCatalog.findById(state.selections.iconPackId)
    val filteredApps = state.discoveredApps.filter {
        state.iconQuery.isBlank() || it.label.contains(state.iconQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Text("Icons", color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
            Text(
                "Four premium styles for every launchable app on this phone.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }
        item {
            IconPackInstallSurface(
                status = state.iconPackBundle,
                onInstall = { onAction(CustomizationAction.InstallIconPacks) },
                onOpenSettings = { onAction(CustomizationAction.OpenIconPackSettings) },
            )
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                contentPadding = PaddingValues(end = AppSpacing.md),
            ) {
                items(state.iconPacks, key = IconPackDefinition::id) { pack ->
                    IconPackCard(
                        pack = pack,
                        selected = pack.id == selectedPack?.id,
                        favorite = pack.id in state.selections.favoriteIconPackIds,
                        onSelect = { onAction(CustomizationAction.SelectIconPack(pack.id)) },
                        onFavorite = { onAction(CustomizationAction.ToggleIconPackFavorite(pack.id)) },
                        modifier = Modifier.size(width = 190.dp, height = 238.dp),
                    )
                }
            }
        }
        if (selectedPack != null) {
            item {
                val coverage = state.iconCoverage[selectedPack.id]
                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(AppCorners.button),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        Text(selectedPack.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.isLoadingIcons) "Looking for installed apps…"
                            else "Preview coverage ${coverage?.styledCount ?: 0}/${coverage?.totalCount ?: 0} (${coverage?.percent ?: 0}%) · Custom ${coverage?.mappedCount ?: 0} · Runtime styled ${coverage?.runtimeStyledCount ?: 0}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Every app keeps its recognizable launcher artwork in this preview. Choose the pack in your launcher's icon settings to apply it.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Button(
                            onClick = { onAction(CustomizationAction.ApplySelectedIconPack) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        ) { Text("Use ${selectedPack.name}") }
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.iconQuery,
                onValueChange = { onAction(CustomizationAction.SetIconQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Find an installed app") },
                placeholder = { Text("Search apps") },
            )
        }
        item {
            Text("Installed apps", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }
        if (filteredApps.isEmpty() && !state.isLoadingIcons) {
            item { Text("No launcher apps matched that search.", color = TextSecondary) }
        } else {
            item {
                val appPreview = filteredApps.take(12)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    items(appPreview, key = { "${it.packageName}/${it.activityName}" }) { app ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                bitmap = app.bitmap.asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier.size(52.dp),
                            )
                            Text(
                                app.label,
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
        state.message?.let { message ->
            item { MessageSurface(message) }
        }
    }
}

@Composable
private fun IconPackInstallSurface(
    status: IconPackBundleStatus,
    onInstall: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val installActionLabel = when (status.phase) {
        IconPackBundlePhase.UPDATE_AVAILABLE -> "Update Icon Packs"
        IconPackBundlePhase.INSTALLING,
        IconPackBundlePhase.AWAITING_USER_CONFIRMATION,
        -> "Installing…"
        else -> "Install Icon Packs"
    }
    val canInstall = status.phase !in setOf(
        IconPackBundlePhase.INSTALLING,
        IconPackBundlePhase.AWAITING_USER_CONFIRMATION,
    )
    Surface(
        color = GlassSurface,
        shape = RoundedCornerShape(AppCorners.button),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text("Icon packs", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(
                text = when (status.phase) {
                    IconPackBundlePhase.NOT_INSTALLED -> "Launcher packs not installed"
                    IconPackBundlePhase.UPDATE_AVAILABLE -> "Icon pack updates available"
                    IconPackBundlePhase.INSTALLING -> "Installing the four launcher packs…"
                    IconPackBundlePhase.AWAITING_USER_CONFIRMATION -> "Confirm the Android installation to continue"
                    IconPackBundlePhase.INSTALLED,
                    IconPackBundlePhase.INSTALLED_SUCCESSFULLY,
                    -> "${status.installedCount} / ${status.totalCount.coerceAtLeast(4)} icon packs installed"
                    IconPackBundlePhase.PERMISSION_REQUIRED -> "Android permission is required before installation"
                    IconPackBundlePhase.CANCELED -> "Icon-pack installation was canceled"
                    IconPackBundlePhase.BLOCKED -> status.message ?: "Android blocked the icon-pack installation"
                    IconPackBundlePhase.INVALID -> status.message ?: "Android rejected an invalid icon-pack package"
                    IconPackBundlePhase.CONFLICT -> status.message ?: "An installed package conflicts with an icon pack"
                    IconPackBundlePhase.STORAGE_FAILURE -> status.message ?: "There is not enough storage for the icon packs"
                    IconPackBundlePhase.INCOMPATIBLE -> status.message ?: "The icon packs are incompatible with this device"
                    IconPackBundlePhase.FAILED -> status.message ?: "Icon-pack installation is unavailable"
                },
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (status.packages.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    status.packages.forEach { pack ->
                        val marker = if (pack.phase == com.r2h_widget.customization.IconPackPackagePhase.INSTALLED) "✓" else "○"
                        Text("$marker ${pack.label}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (!status.isReady) {
                Button(
                    onClick = onInstall,
                    enabled = canInstall,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text(installActionLabel) }
            }
            if (status.isReady) {
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Open Nothing Icon Pack Settings") }
            }
        }
    }
}

@Composable
private fun IconPackCard(
    pack: IconPackDefinition,
    selected: Boolean,
    favorite: Boolean,
    onSelect: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppCorners.card))
            .background(if (selected) AccentViolet.copy(alpha = 0.14f) else GlassSurface)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) AccentViolet else GlassOutline,
                RoundedCornerShape(AppCorners.card),
            )
            .clickable(onClick = onSelect, role = Role.Button)
            .semantics { role = Role.Button },
    ) {
        Box(modifier = Modifier.padding(AppSpacing.sm)) {
            Image(
                painter = painterResource(pack.previewDrawableRes),
                contentDescription = pack.name,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(AppCorners.button)),
                contentScale = ContentScale.Crop,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = AppSpacing.md, bottom = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(pack.name, color = TextPrimary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onFavorite, modifier = Modifier.size(44.dp).semantics {
                contentDescription = if (favorite) "Remove ${pack.name} from favorites" else "Favorite ${pack.name}"
            }) { Text(if (favorite) "♥" else "♡", color = if (favorite) AccentViolet else TextMuted) }
        }
    }
}

@Composable
fun WallpapersScreen(
    state: CustomizationUiState,
    onAction: (CustomizationAction) -> Unit,
) {
    var detailWallpaper by remember { mutableStateOf<WallpaperDefinition?>(null) }
    val selectedWallpaper = WallpaperCatalog.findById(state.selections.wallpaperId)
    val wallpapers = WallpaperCatalog.forCategory(state.wallpaperCategory).filter {
        state.wallpaperQuery.isBlank() || it.name.contains(state.wallpaperQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Text("Wallpapers", color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
            Text(
                "Local, offline wallpapers with Home, Lock, or Both apply targets.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }
        item {
            OutlinedTextField(
                value = state.wallpaperQuery,
                onValueChange = { onAction(CustomizationAction.SetWallpaperQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search wallpapers") },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                WallpaperCategory.entries.forEach { category ->
                    FilterChip(
                        selected = category == state.wallpaperCategory,
                        onClick = { onAction(CustomizationAction.SelectWallpaperCategory(category)) },
                        label = { Text(category.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentViolet.copy(alpha = 0.18f),
                            containerColor = GlassSurface,
                        ),
                    )
                }
            }
        }
        item {
            val wallpaperRows = ((wallpapers.size + 1) / 2).coerceAtLeast(1)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height((wallpaperRows * 232).dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                items(wallpapers, key = WallpaperDefinition::id) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        selected = wallpaper.id == selectedWallpaper?.id,
                        favorite = wallpaper.id in state.selections.favoriteWallpaperIds,
                        onClick = {
                            onAction(CustomizationAction.SelectWallpaper(wallpaper.id))
                            detailWallpaper = wallpaper
                        },
                        onFavorite = { onAction(CustomizationAction.ToggleWallpaperFavorite(wallpaper.id)) },
                    )
                }
            }
        }
        if (selectedWallpaper != null) {
            item {
                Surface(color = GlassSurface, shape = RoundedCornerShape(AppCorners.button)) {
                    Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        Text("Apply ${selectedWallpaper.name}", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            WallpaperTarget.entries.forEach { target ->
                                OutlinedButton(
                                    onClick = { onAction(CustomizationAction.ApplyWallpaper(target)) },
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                ) { Text(target.label()) }
                            }
                        }
                    }
                }
            }
        }
        state.message?.let { message -> item { MessageSurface(message) } }
    }

    detailWallpaper?.let { wallpaper ->
        WallpaperDetailDialog(
            wallpaper = wallpaper,
            onDismiss = { detailWallpaper = null },
            onApply = { target ->
                onAction(CustomizationAction.SelectWallpaper(wallpaper.id))
                onAction(CustomizationAction.ApplyWallpaper(target))
                detailWallpaper = null
            },
        )
    }
}

@Composable
private fun WallpaperCard(
    wallpaper: WallpaperDefinition,
    selected: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(AppCorners.card))
            .border(if (selected) 2.dp else 1.dp, if (selected) AccentViolet else GlassOutline, RoundedCornerShape(AppCorners.card))
            .clickable(onClick = onClick, role = Role.Button)
            .semantics { role = Role.Button },
    ) {
        Image(
            painter = painterResource(wallpaper.drawableRes),
            contentDescription = wallpaper.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Surface(
            color = GlassSurface.copy(alpha = 0.86f),
            shape = RoundedCornerShape(topStart = AppCorners.button, topEnd = AppCorners.button),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
        ) {
            Row(Modifier.padding(start = AppSpacing.sm, end = AppSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Text(wallpaper.name, color = TextPrimary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onFavorite, modifier = Modifier.size(44.dp).semantics {
                    contentDescription = if (favorite) "Remove ${wallpaper.name} from favorites" else "Favorite ${wallpaper.name}"
                }) { Text(if (favorite) "♥" else "♡", color = if (favorite) AccentViolet else TextMuted) }
            }
        }
    }
}

@Composable
private fun WallpaperDetailDialog(
    wallpaper: WallpaperDefinition,
    onDismiss: () -> Unit,
    onApply: (WallpaperTarget) -> Unit,
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 4f)
        offsetX += pan.x
        offsetY += pan.y
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        title = { Text(wallpaper.name, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(AppCorners.button))
                        .background(TextMuted.copy(alpha = 0.12f))
                        .transformable(transformState),
                ) {
                    Image(
                        painter = painterResource(wallpaper.drawableRes),
                        contentDescription = wallpaper.name,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        },
                        contentScale = ContentScale.Crop,
                    )
                }
                Text("Pinch to zoom and drag to inspect the artwork.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = { onApply(WallpaperTarget.BOTH) }) { Text("Apply both") } },
        dismissButton = {
            Row {
                TextButton(onClick = { onApply(WallpaperTarget.HOME) }) { Text("Home") }
                TextButton(onClick = { onApply(WallpaperTarget.LOCK) }) { Text("Lock") }
            }
        },
    )
}

@Composable
fun ThemesScreen(
    state: CustomizationUiState,
    onAction: (CustomizationAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Text("Themes", color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
            Text(
                "One offline bundle for your icons, wallpaper, clock, and app accent.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }
        items(ThemeCatalog.builtIn, key = ThemeDefinition::id) { theme ->
            val selected = theme.id == state.selections.themeId
            Surface(
                color = if (selected) AccentViolet.copy(alpha = 0.12f) else GlassSurface,
                shape = RoundedCornerShape(AppCorners.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (selected) 2.dp else 1.dp, if (selected) AccentViolet else GlassOutline, RoundedCornerShape(AppCorners.card)),
            ) {
                Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(AppCorners.button)),
                    ) {
                        Image(
                            painter = painterResource(theme.previewDrawableRes),
                            contentDescription = theme.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        IconPackCatalog.findById(theme.iconPackId)?.let { pack ->
                            Image(
                                painter = painterResource(pack.previewDrawableRes),
                                contentDescription = "${pack.name} icon style",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(AppSpacing.sm)
                                    .size(82.dp)
                                    .clip(RoundedCornerShape(AppCorners.button))
                                    .border(1.dp, GlassOutline, RoundedCornerShape(AppCorners.button)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(theme.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(theme.description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "Icons · ${theme.iconPackId.replace('-', ' ')}  •  Wallpaper · ${theme.wallpaperId.replace('-', ' ')}  •  Clock · ${theme.clockPresetId}",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = AppSpacing.xs),
                            )
                        }
                        Button(onClick = { onAction(CustomizationAction.ApplyTheme(theme.id)) }) {
                            Text(if (selected) "Selected" else "Apply")
                        }
                    }
                }
            }
        }
        state.message?.let { message -> item { MessageSurface(message) } }
    }
}

@Composable
private fun MessageSurface(message: String) {
    Surface(
        color = AccentCyan.copy(alpha = 0.10f),
        shape = RoundedCornerShape(AppCorners.button),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(message, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(AppSpacing.md))
    }
}

private fun WallpaperTarget.label(): String = when (this) {
    WallpaperTarget.HOME -> "Home"
    WallpaperTarget.LOCK -> "Lock"
    WallpaperTarget.BOTH -> "Both"
}
