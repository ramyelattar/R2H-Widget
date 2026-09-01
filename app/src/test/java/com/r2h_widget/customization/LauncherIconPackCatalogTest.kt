package com.r2h_widget.customization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherIconPackCatalogTest {

    @Test
    fun fourLauncherArtifactsHaveUniqueStableIdentities() {
        val artifacts = LauncherIconPackCatalog.builtIn

        assertEquals(
            listOf("fluffy", "liquid-dark-glass", "liquid-siri-glass", "luxury-vip"),
            artifacts.map { it.packId },
        )
        assertEquals(artifacts.size, artifacts.map { it.packageName }.toSet().size)
        assertTrue(artifacts.all { it.packageName.startsWith("com.r2h_widget.iconpack.") })
    }

    @Test
    fun everyCatalogPackResolvesToOneLauncherArtifact() {
        IconPackCatalog.builtIn.forEach { pack ->
            val artifact = LauncherIconPackCatalog.forPack(pack.id)
            assertEquals(pack.name, artifact?.label)
        }
    }
}
