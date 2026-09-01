package com.r2h_widget.customization

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IconMaterialMathTest {
    @Test
    fun profile_ignoresTransparentCanvasAndReportsBoundedMaterialSignals() {
        val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        for (y in 4 until 20) {
            for (x in 4 until 20) {
                bitmap.setPixel(x, y, Color.argb(255, 220, 80, 120))
            }
        }

        val profile = IconMaterialMath.profile(bitmap)

        assertEquals(Color.rgb(220, 80, 120), profile.averageColor)
        assertTrue(profile.luminance in 0f..1f)
        assertTrue(profile.saturation in 0f..1f)
        assertTrue(profile.alphaOccupancy in 0f..1f)
    }

    @Test
    fun profile_reportsPartialAlphaOccupancyForSoftArtwork() {
        val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        for (y in 6 until 18) {
            for (x in 6 until 18) {
                bitmap.setPixel(x, y, Color.argb(140, 80, 180, 240))
            }
        }

        val profile = IconMaterialMath.profile(bitmap)

        assertTrue(profile.alphaOccupancy == 0f)
        assertTrue(profile.averageColor != Color.TRANSPARENT)
    }

    @Test
    fun colorOperations_preserveMaterialMathBounds() {
        val source = Color.rgb(40, 80, 120)
        val mixed = IconMaterialMath.blend(source, Color.WHITE, 0.5f)
        val translucent = IconMaterialMath.withAlpha(mixed, 270)

        assertEquals(Color.argb(255, 147, 167, 187), translucent)
        assertEquals(255, Color.alpha(translucent))
        assertTrue(IconMaterialMath.luminance(mixed) in 0f..1f)
    }
}
