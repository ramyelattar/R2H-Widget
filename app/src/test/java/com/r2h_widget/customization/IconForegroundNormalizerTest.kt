package com.r2h_widget.customization

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IconForegroundNormalizerTest {
    private val target = RectF(0f, 0f, 100f, 100f)

    @Test
    fun transparentTwentyPercentPadding_isCroppedAndFillsTargetRange() {
        val bitmap = opaqueRect(100, Rect(20, 20, 80, 80))

        val placement = requireNotNull(IconForegroundNormalizer.placementFor(bitmap, target))

        assertEquals(Rect(20, 20, 80, 80), placement.visibleBounds)
        assertTrue(placement.croppedBounds.left < placement.visibleBounds.left)
        assertTrue(placement.croppedBounds.right > placement.visibleBounds.right)
        assertOccupancyInTargetRange(placement)
        assertFits(placement.destination, target)
    }

    @Test
    fun transparentThirtyFivePercentPadding_isNotDoubleInset() {
        val bitmap = opaqueRect(100, Rect(35, 35, 65, 65))

        val placement = requireNotNull(IconForegroundNormalizer.placementFor(bitmap, target))

        assertEquals(Rect(35, 35, 65, 65), placement.visibleBounds)
        assertOccupancyInTargetRange(placement)
        assertFits(placement.destination, target)
    }

    @Test
    fun alreadyFullIcon_receivesOnlyCanonicalBreathingMargin() {
        val bitmap = opaqueRect(100, Rect(0, 0, 100, 100))

        val placement = requireNotNull(IconForegroundNormalizer.placementFor(bitmap, target))

        assertEquals(Rect(0, 0, 100, 100), placement.visibleBounds)
        assertEquals(Rect(0, 0, 100, 100), placement.croppedBounds)
        assertEquals(84f, placement.destination.width(), 0.001f)
        assertEquals(84f, placement.destination.height(), 0.001f)
        assertEquals(8f, placement.destination.left, 0.001f)
        assertEquals(8f, placement.destination.top, 0.001f)
        assertOccupancyInTargetRange(placement)
    }

    @Test
    fun wideIcon_preservesAspectRatioAndDoesNotClip() {
        val bitmap = opaqueRect(120, Rect(10, 40, 110, 80))

        val placement = requireNotNull(IconForegroundNormalizer.placementFor(bitmap, target))

        assertEquals(
            placement.croppedBounds.width().toFloat() / placement.croppedBounds.height(),
            placement.destination.width() / placement.destination.height(),
            0.001f,
        )
        assertTrue(placement.destination.width() > placement.destination.height())
        assertOccupancyInTargetRange(placement)
        assertFits(placement.destination, target)
    }

    @Test
    fun tallIcon_preservesAspectRatioAndDoesNotClip() {
        val bitmap = opaqueRect(120, Rect(40, 10, 80, 110))

        val placement = requireNotNull(IconForegroundNormalizer.placementFor(bitmap, target))

        assertEquals(
            placement.croppedBounds.width().toFloat() / placement.croppedBounds.height(),
            placement.destination.width() / placement.destination.height(),
            0.001f,
        )
        assertTrue(placement.destination.height() > placement.destination.width())
        assertOccupancyInTargetRange(placement)
        assertFits(placement.destination, target)
    }

    @Test
    fun alphaNoiseBelowThreshold_isIgnoredWhenFindingContentBounds() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.setPixel(1, 1, Color.argb(4, 255, 255, 255))
        bitmap.setPixel(98, 98, Color.argb(8, 255, 255, 255))
        fill(bitmap, Rect(25, 25, 75, 75), Color.argb(20, 16, 160, 220))

        val bounds = IconForegroundNormalizer.visibleBounds(bitmap)

        assertNotNull(bounds)
        assertEquals(Rect(25, 25, 75, 75), bounds)
    }

    @Test
    fun emptyBitmap_normalizeReturnsSameDimensionsWithoutInventingArtwork() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val normalized = IconForegroundNormalizer.normalize(bitmap, target)

        assertEquals(100, normalized.width)
        assertEquals(100, normalized.height)
        assertTrue(normalized.getPixel(50, 50) == Color.TRANSPARENT)
    }

    private fun assertOccupancyInTargetRange(placement: IconForegroundPlacement) {
        val cropIsWide = placement.croppedBounds.width().toFloat() / placement.croppedBounds.height() >= 1f
        val visibleFraction = if (cropIsWide) {
            placement.visibleBounds.width().toFloat() / placement.croppedBounds.width()
        } else {
            placement.visibleBounds.height().toFloat() / placement.croppedBounds.height()
        }
        val visibleOccupancy = if (cropIsWide) {
            placement.destination.width() * visibleFraction / target.width()
        } else {
            placement.destination.height() * visibleFraction / target.height()
        }
        assertTrue("visible occupancy=$visibleOccupancy", visibleOccupancy in 0.82f..0.88f)
    }

    private fun assertFits(destination: RectF, container: RectF) {
        assertTrue(destination.left >= container.left - 0.001f)
        assertTrue(destination.top >= container.top - 0.001f)
        assertTrue(destination.right <= container.right + 0.001f)
        assertTrue(destination.bottom <= container.bottom + 0.001f)
    }

    private fun opaqueRect(size: Int, rect: Rect): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        fill(bitmap, rect, Color.argb(255, 64, 160, 220))
        return bitmap
    }

    private fun fill(bitmap: Bitmap, rect: Rect, color: Int) {
        for (y in rect.top until rect.bottom) {
            for (x in rect.left until rect.right) {
                bitmap.setPixel(x, y, color)
            }
        }
    }
}
