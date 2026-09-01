package com.r2h_widget.customization

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.r2h_widget.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IconPackEngineTest {
    private val sourceBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888).also { bitmap ->
        Canvas(bitmap).drawColor(Color.rgb(36, 168, 220))
        Canvas(bitmap).drawCircle(48f, 48f, 25f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
    }

    private val unknownApp = DiscoveredAppIcon(
        packageName = "com.example.runtimefallback",
        activityName = "com.example.runtimefallback.MainActivity",
        label = "Runtime fallback",
        bitmap = sourceBitmap,
        mapped = false,
    )

    @Test
    fun runtimeFallback_producesAStyledBitmapForEveryPrimaryPack() {
        val engine = IconPackEngine(RuntimeEnvironment.getApplication())

        IconPackCatalog.builtIn.forEach { pack ->
            val rendered = engine.renderIcon(unknownApp, pack)

            assertNotNull("${pack.id} should render a runtime fallback", rendered)
            assertNotSame("${pack.id} should not return the raw source bitmap", sourceBitmap, rendered)
            assertEquals(rendered.width, rendered.height)
            assert(rendered.width > 0)
        }
    }

    @Test
    fun coverage_countsEveryLaunchableAppAsStyledForEveryPrimaryPack() {
        val engine = IconPackEngine(RuntimeEnvironment.getApplication())
        val apps = listOf(unknownApp)

        IconPackCatalog.builtIn.forEach { pack ->
            val coverage = engine.coverage(pack, apps)

            assertEquals(1, coverage.totalCount)
            assertEquals(1, coverage.runtimeStyledCount)
            assertEquals(1, coverage.styledCount)
            assertEquals(100, coverage.percent)
        }
    }

    @Test
    fun runtimeFallback_createsIndependentStyledBitmapForEveryPrimaryPack() {
        val engine = IconPackEngine(RuntimeEnvironment.getApplication())
        val rendered = IconPackCatalog.builtIn.associate { pack ->
            pack.id to engine.renderIcon(unknownApp, pack)
        }

        assertEquals(4, rendered.size)
        assertEquals(4, rendered.values.map { System.identityHashCode(it) }.distinct().size)
        rendered.values.forEach { bitmap ->
            assertTrue(
                "A styled bitmap should have a real render size",
                bitmap.width > 0 && bitmap.height > 0,
            )
        }
    }

    @Test
    fun runtimeFallback_reusesOnlyTheMatchingPackCacheEntry() {
        val engine = IconPackEngine(RuntimeEnvironment.getApplication())
        val fluffy = IconPackCatalog.builtIn.first { it.id == IconPackCatalog.FLUFFY_ID }
        val dark = IconPackCatalog.builtIn.first { it.id == IconPackCatalog.LIQUID_DARK_GLASS_ID }

        val firstFluffy = engine.renderIcon(unknownApp, fluffy)
        val firstDark = engine.renderIcon(unknownApp, dark)
        val cachedFluffy = engine.renderIcon(unknownApp, fluffy)

        assertNotSame(firstFluffy, firstDark)
        assertSame(firstFluffy, cachedFluffy)
    }

    @Test
    fun approvedPhase1Assets_replaceProceduralPreviewForCoveredComponent() {
        val engine = IconPackEngine(RuntimeEnvironment.getApplication())
        val coveredApp = unknownApp.copy(
            packageName = "com.android.chrome",
            activityName = "com.google.android.apps.chrome.Main",
        )
        val expected = mapOf(
            IconPackCatalog.FLUFFY_ID to R.drawable.r2h_fluffy_chrome,
            IconPackCatalog.LIQUID_DARK_GLASS_ID to R.drawable.r2h_darkglass_chrome,
            IconPackCatalog.LIQUID_SIRI_GLASS_ID to R.drawable.r2h_siriglass_chrome,
            IconPackCatalog.LUXURY_VIP_ID to R.drawable.r2h_luxuryvip_chrome,
        )

        IconPackCatalog.builtIn.forEach { pack ->
            val expectedBitmap = BitmapFactory.decodeResource(
                RuntimeEnvironment.getApplication().resources,
                expected.getValue(pack.id),
            )
            val actualBitmap = engine.renderIcon(coveredApp, pack)

            assertBitmapsEqual(
                "${pack.id} must return the approved static phase-1 asset exactly",
                expectedBitmap,
                actualBitmap,
            )
        }
    }

    @Test
    fun approvedPhase1Coverage_keepsUncoveredAppsOnRuntimeFallback() {
        val engine = IconPackEngine(RuntimeEnvironment.getApplication())
        val coveredApp = unknownApp.copy(
            packageName = "com.android.chrome",
            activityName = "com.google.android.apps.chrome.Main",
        )

        IconPackCatalog.builtIn.forEach { pack ->
            val coverage = engine.coverage(pack, listOf(coveredApp, unknownApp))

            assertEquals("${pack.id} mapped phase-1 count", 1, coverage.mappedCount)
            assertEquals("${pack.id} fallback count", 1, coverage.runtimeStyledCount)
            assertEquals(2, coverage.styledCount)
        }
    }

    private fun assertBitmapsEqual(message: String, expected: Bitmap?, actual: Bitmap) {
        assertNotNull(message, expected)
        requireNotNull(expected)
        assertEquals(message, expected.width, actual.width)
        assertEquals(message, expected.height, actual.height)
        for (y in 0 until expected.height) {
            for (x in 0 until expected.width) {
                assertEquals("$message at ($x,$y)", expected.getPixel(x, y), actual.getPixel(x, y))
            }
        }
    }
}
