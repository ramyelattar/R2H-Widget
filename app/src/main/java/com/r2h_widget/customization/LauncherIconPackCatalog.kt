package com.r2h_widget.customization

/**
 * Static launcher package identities for the four packs. Nothing Launcher
 * treats an APK as one icon-pack source, so each independently selectable
 * style is shipped as a companion APK. The APK asset name is part of this
 * contract so the main app can install the complete set in one transaction.
 */
data class LauncherIconPackArtifact(
    val packId: String,
    val packageName: String,
    val label: String,
    val assetFileName: String,
)

object LauncherIconPackCatalog {
    val builtIn: List<LauncherIconPackArtifact> = listOf(
        LauncherIconPackArtifact(
            packId = IconPackCatalog.FLUFFY_ID,
            packageName = "com.r2h_widget.iconpack.fluffy",
            label = "Fluffy",
            assetFileName = "fluffy.apk",
        ),
        LauncherIconPackArtifact(
            packId = IconPackCatalog.LIQUID_DARK_GLASS_ID,
            packageName = "com.r2h_widget.iconpack.darkglass",
            label = "Liquid Dark Glass",
            assetFileName = "liquid-dark-glass.apk",
        ),
        LauncherIconPackArtifact(
            packId = IconPackCatalog.LIQUID_SIRI_GLASS_ID,
            packageName = "com.r2h_widget.iconpack.siriglass",
            label = "Liquid Siri Glass",
            assetFileName = "liquid-siri-glass.apk",
        ),
        LauncherIconPackArtifact(
            packId = IconPackCatalog.LUXURY_VIP_ID,
            packageName = "com.r2h_widget.iconpack.luxuryvip",
            label = "Luxury VIP",
            assetFileName = "luxury-vip.apk",
        ),
    )

    fun forPack(packId: String): LauncherIconPackArtifact? =
        builtIn.firstOrNull { it.packId == packId }
}
