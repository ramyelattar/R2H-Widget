package com.r2h_widget.customization

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Describes the one authoritative foreground placement used by the runtime
 * icon renderer. The destination contains the expanded source crop; the
 * original visible bounds are retained so callers/tests can reason about the
 * actual artwork occupancy separately from the safety margin.
 */
data class IconForegroundPlacement(
    val visibleBounds: Rect,
    val croppedBounds: Rect,
    val destination: RectF,
)

/**
 * Removes transparent canvas padding and fits the remaining launcher artwork
 * into a predictable foreground area without changing its aspect ratio.
 *
 * The static icon-pack generator mirrors these constants and the same
 * alpha-bound/crop/fit contract because that tool is intentionally compiled
 * independently from the Android app module.
 */
object IconForegroundNormalizer {
    const val DEFAULT_ALPHA_THRESHOLD = 12
    const val TARGET_VISIBLE_FRACTION = 0.84f
    const val MAX_VISIBLE_FRACTION = 0.88f
    const val SAFETY_EXPANSION_FRACTION = 0.02f

    fun visibleBounds(
        bitmap: Bitmap,
        alphaThreshold: Int = DEFAULT_ALPHA_THRESHOLD,
    ): Rect? {
        val threshold = alphaThreshold.coerceIn(0, 255)
        var minX = bitmap.width
        var minY = bitmap.height
        var maxX = -1
        var maxY = -1

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if ((bitmap.getPixel(x, y) ushr 24) > threshold) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }

        return if (maxX < minX || maxY < minY) {
            null
        } else {
            Rect(minX, minY, maxX + 1, maxY + 1)
        }
    }

    fun placementFor(
        bitmap: Bitmap,
        targetRect: RectF,
        alphaThreshold: Int = DEFAULT_ALPHA_THRESHOLD,
    ): IconForegroundPlacement? {
        require(targetRect.width() > 0f && targetRect.height() > 0f) {
            "targetRect must have positive dimensions"
        }

        val visible = visibleBounds(bitmap, alphaThreshold) ?: return null
        val cropped = expandAndClamp(
            visible,
            bitmap.width,
            bitmap.height,
        )

        val cropWidth = cropped.width().toFloat()
        val cropHeight = cropped.height().toFloat()
        val visibleWidth = visible.width().toFloat()
        val visibleHeight = visible.height().toFloat()
        val cropIsWide = cropWidth / cropHeight >= targetRect.width() / targetRect.height()
        val targetLongAxis = if (cropIsWide) targetRect.width() else targetRect.height()
        val visibleLongFraction = if (cropIsWide) {
            visibleWidth / cropWidth
        } else {
            visibleHeight / cropHeight
        }

        // Scale the expanded crop so the detected artwork, rather than its
        // transparent safety margin, lands at the canonical target occupancy.
        // The cap protects unusual tiny glyphs from being over-enlarged.
        val desiredCropLongAxis = min(
            targetLongAxis * MAX_VISIBLE_FRACTION,
            targetLongAxis * TARGET_VISIBLE_FRACTION / visibleLongFraction,
        )
        val scale = desiredCropLongAxis / if (cropIsWide) cropWidth else cropHeight
        val destinationWidth = cropWidth * scale
        val destinationHeight = cropHeight * scale
        val left = targetRect.centerX() - destinationWidth / 2f
        val top = targetRect.centerY() - destinationHeight / 2f

        return IconForegroundPlacement(
            visibleBounds = Rect(visible),
            croppedBounds = Rect(cropped),
            destination = RectF(
                left,
                top,
                left + destinationWidth,
                top + destinationHeight,
            ),
        )
    }

    /**
     * Returns a bitmap with the original canvas dimensions and normalized
     * artwork positioned inside [targetRect]. Empty/fully transparent sources
     * are copied unchanged instead of being stretched or fabricated.
     */
    fun normalize(
        bitmap: Bitmap,
        targetRect: RectF,
        alphaThreshold: Int = DEFAULT_ALPHA_THRESHOLD,
    ): Bitmap {
        val placement = placementFor(bitmap, targetRect, alphaThreshold) ?: return bitmap.copy(
            Bitmap.Config.ARGB_8888,
            false,
        )
        val cropped = Bitmap.createBitmap(
            bitmap,
            placement.croppedBounds.left,
            placement.croppedBounds.top,
            placement.croppedBounds.width(),
            placement.croppedBounds.height(),
        )
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(
            cropped,
            null,
            placement.destination,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG),
        )
        return result
    }

    private fun expandAndClamp(
        visible: Rect,
        bitmapWidth: Int,
        bitmapHeight: Int,
    ): Rect {
        val expandX = max(1, ceil(visible.width() * SAFETY_EXPANSION_FRACTION).toInt())
        val expandY = max(1, ceil(visible.height() * SAFETY_EXPANSION_FRACTION).toInt())
        return Rect(
            (visible.left - expandX).coerceAtLeast(0),
            (visible.top - expandY).coerceAtLeast(0),
            (visible.right + expandX).coerceAtMost(bitmapWidth),
            (visible.bottom + expandY).coerceAtMost(bitmapHeight),
        )
    }
}
