package com.r2h_widget.customization

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device-side source export used by the deterministic icon-pack generator.
 *
 * The launcher owns the original application artwork, so the build-time pack
 * generator needs a real PackageManager-rendered source icon for components
 * that are not in the checked-in reference inventory. This test intentionally
 * writes only to the app's external test files directory; it does not affect
 * the production app or launcher state.
 */
@RunWith(AndroidJUnit4::class)
class DeviceIconAssetExportTest {

    @Test
    fun exportCurrentLauncherIconsForPackGeneration() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val component = ComponentName(activityInfo.packageName, activityInfo.name)
                component to resolveInfo.loadIcon(packageManager)
            }
            .distinctBy { it.first.flattenToString() }
            .sortedBy { it.first.flattenToString().lowercase(Locale.ROOT) }

        val outputDir = File(context.filesDir, "icon-pack-source/device-icons")
            .apply { mkdirs() }
        val inventory = File(outputDir, "inventory.tsv")
        val inventoryCsv = File(outputDir, "CURRENT_DEVICE_LAUNCHER_INVENTORY.csv")

        inventory.bufferedWriter().use { writer ->
            inventoryCsv.bufferedWriter().use { csv ->
                csv.appendLine("label,packageName,activityName,component,sourceIconExported,fluffyStatic,darkGlassStatic,siriGlassStatic,luxuryVipStatic")
                activities.forEachIndexed { index, (component, drawable) ->
                val fileName = "device_${index.toString().padStart(4, '0')}.png"
                val bitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.TRANSPARENT)
                val canvas = Canvas(bitmap)
                val inset = 32
                drawable.bounds = Rect(inset, inset, 1024 - inset, 1024 - inset)
                drawable.draw(canvas)
                FileOutputStream(File(outputDir, fileName)).use { stream ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                        "Unable to encode $component"
                    }
                }
                bitmap.recycle()
                writer.append(component.flattenToString())
                    .append('\t')
                    .append(fileName)
                    .append('\n')
                val label = packageManager.getActivityInfo(component, 0).loadLabel(packageManager)
                    ?.toString()
                    ?.replace("\"", "\"\"")
                    .orEmpty()
                csv.append('"').append(label).append("\",")
                    .append(component.packageName).append(',')
                    .append(component.className).append(',')
                    .append(component.flattenToString()).append(',')
                    .append(fileName).append(",,,,")
                    .appendLine()
                }
            }
        }

        assertTrue("No launcher components were exported", activities.isNotEmpty())
        assertTrue("Inventory was not written", inventory.isFile)
    }
}
