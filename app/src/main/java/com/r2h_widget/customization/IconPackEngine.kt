package com.r2h_widget.customization

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import com.r2h_widget.R
import org.xmlpull.v1.XmlPullParser
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

data class IconPackCoverage(
    val mappedCount: Int,
    val runtimeStyledCount: Int,
    val styledCount: Int,
    val totalCount: Int,
) {
    val percent: Int
        get() = if (totalCount == 0) 0 else ((styledCount * 100f) / totalCount).roundToInt()
}

/**
 * Offline icon data and rendering boundary.
 *
 * Bundled icon assets are used where a known package mapping exists. Every remaining
 * app keeps its real launcher artwork and receives the selected pack's surface treatment,
 * so recognition is never replaced by arbitrary initials.
 */
class IconPackEngine(
    private val context: Context,
) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val darkGlassMappings by lazy { loadGraphiteMappings() }
    private val approvedStaticMappings by lazy {
        mapOf(
            IconPackCatalog.FLUFFY_ID to loadDrawableMappings(R.xml.approved_static_fluffy),
            IconPackCatalog.LIQUID_DARK_GLASS_ID to loadDrawableMappings(R.xml.approved_static_darkglass),
            IconPackCatalog.LIQUID_SIRI_GLASS_ID to loadDrawableMappings(R.xml.approved_static_siriglass),
            IconPackCatalog.LUXURY_VIP_ID to loadDrawableMappings(R.xml.approved_static_luxuryvip),
        )
    }
    private val renderedIconCache = java.util.concurrent.ConcurrentHashMap<RenderCacheKey, Bitmap>()

    private val essentialsMappings: Map<String, Int> by lazy {
        mapOf(
            "com.google.android.dialer" to R.drawable.fluffy_ess_phone,
            "com.android.dialer" to R.drawable.fluffy_ess_phone,
            "com.truecaller" to R.drawable.fluffy_ess_phone,
            "com.hearthborn.studios.ndial" to R.drawable.fluffy_ess_phone,
            "com.google.android.apps.messaging" to R.drawable.fluffy_ess_messages,
            "com.nothing.camera" to R.drawable.fluffy_ess_camera,
            "com.hinnka.mycamera" to R.drawable.fluffy_ess_camera,
            "com.google.android.apps.photos" to R.drawable.fluffy_ess_photos,
            "com.nothing.gallery" to R.drawable.fluffy_ess_gallery,
            "com.appslab.gallery" to R.drawable.fluffy_ess_gallery,
            "com.android.chrome" to R.drawable.fluffy_ess_browser,
            "com.android.settings" to R.drawable.fluffy_ess_settings,
            "com.google.android.deskclock" to R.drawable.fluffy_ess_clock,
            "com.nothing.weather" to R.drawable.fluffy_ess_weather,
            "com.metrolist.music" to R.drawable.fluffy_ess_music,
            "com.maxrave.simpmusic" to R.drawable.fluffy_ess_music,
            "com.cyanchill.missingcore.music" to R.drawable.fluffy_ess_music,
            "app.morphe.android.apps.youtube.music" to R.drawable.fluffy_ess_music,
            "com.google.android.apps.nbu.files" to R.drawable.fluffy_ess_files,
            "com.hearthborn.studios.nfiles" to R.drawable.fluffy_ess_files,
            "com.google.android.keep" to R.drawable.fluffy_ess_notes,
            "com.hearthborn.studios.notingnotes" to R.drawable.fluffy_ess_notes,
            "com.google.android.contacts" to R.drawable.fluffy_ess_contacts,
            "com.google.android.apps.maps" to R.drawable.fluffy_ess_maps,
            "com.hearthborn.studios.ncalc" to R.drawable.fluffy_ess_calculator,
            "com.google.android.gm" to R.drawable.fluffy_ess_mail,
            "com.android.vending" to R.drawable.fluffy_ess_shopping,
            "com.google.android.youtube" to R.drawable.fluffy_ess_video,
            "com.google.android.videos" to R.drawable.fluffy_ess_video,
            "app.igames.ps2" to R.drawable.fluffy_ess_game,
        )
    }

    private val socialMappings: Map<String, Int> by lazy {
        mapOf(
            "com.instagram.android" to R.drawable.fluffy_social_instagram,
            "com.facebook.katana" to R.drawable.fluffy_social_facebook,
            "com.zhiliaoapp.musically" to R.drawable.fluffy_social_tiktok,
            "com.twitter.android" to R.drawable.fluffy_social_x,
            "org.telegram.messenger" to R.drawable.fluffy_social_telegram,
            "ir.ilmili.telegraph" to R.drawable.fluffy_social_telegram,
            "com.whatsapp" to R.drawable.fluffy_social_whatsapp,
            "com.whatsapp.w4b" to R.drawable.fluffy_social_whatsapp,
            "com.discord" to R.drawable.fluffy_social_discord,
            "com.reddit.frontpage" to R.drawable.fluffy_social_reddit,
            "com.snapchat.android" to R.drawable.fluffy_social_snapchat,
            "com.pinterest" to R.drawable.fluffy_social_pinterest,
            "com.facebook.orca" to R.drawable.fluffy_social_messenger,
            "com.skype.raider" to R.drawable.fluffy_social_skype,
            "tv.twitch.android.app" to R.drawable.fluffy_social_twitch,
            "com.google.android.youtube" to R.drawable.fluffy_social_youtube,
            "com.instagram.barcelona" to R.drawable.fluffy_social_threads,
            "com.linkedin.android" to R.drawable.fluffy_social_linkedin,
            "com.tumblr" to R.drawable.fluffy_social_tumblr,
            "com.viber.voip" to R.drawable.fluffy_social_viber,
            "com.tencent.mm" to R.drawable.fluffy_social_wechat,
            "com.Slack" to R.drawable.fluffy_social_community,
        )
    }

    private val googleMappings: Map<String, Int> by lazy {
        mapOf(
            "com.google.android.googlequicksearchbox" to R.drawable.fluffy_google_google,
            "com.android.chrome" to R.drawable.fluffy_google_chrome,
            "com.google.android.gm" to R.drawable.fluffy_google_gmail,
            "com.google.android.apps.maps" to R.drawable.fluffy_google_maps,
            "com.google.android.apps.docs" to R.drawable.fluffy_google_drive,
            "com.google.android.apps.photos" to R.drawable.fluffy_google_photos,
            "com.android.vending" to R.drawable.fluffy_google_playstore,
            "com.google.android.youtube" to R.drawable.fluffy_google_youtube,
            "com.google.android.apps.chromecast.app" to R.drawable.fluffy_google_home,
            "com.google.android.apps.tachyon" to R.drawable.fluffy_google_meet,
            "com.google.android.calendar" to R.drawable.fluffy_google_calendar,
            "com.google.android.keep" to R.drawable.fluffy_google_keep,
            "com.google.android.apps.docs.editors.docs" to R.drawable.fluffy_google_docs,
            "com.google.android.apps.docs.editors.sheets" to R.drawable.fluffy_google_sheets,
            "com.google.android.apps.docs.editors.slides" to R.drawable.fluffy_google_slides,
            "com.google.android.apps.bard" to R.drawable.fluffy_google_gemini,
            "com.google.android.apps.nbu.files" to R.drawable.fluffy_google_files,
            "com.google.android.apps.messaging" to R.drawable.fluffy_google_messages,
            "com.google.android.apps.subscriptions.red" to R.drawable.fluffy_google_one,
            "com.google.android.play.games" to R.drawable.fluffy_google_playgames,
        )
    }

    fun discoverLaunchableApps(): List<DiscoveredAppIcon> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .asSequence()
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val component = ComponentName(activityInfo.packageName, activityInfo.name)
                if (component.packageName == appContext.packageName) return@mapNotNull null
                val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: activityInfo.packageName
                val mappedDrawableId = mappedDrawableFor(
                    IconPackCatalog.LIQUID_DARK_GLASS_ID,
                    component.packageName,
                    component.flattenToString(),
                )
                DiscoveredAppIcon(
                    packageName = component.packageName,
                    activityName = component.className,
                    label = label,
                    bitmap = renderIcon(
                        resolveInfo.loadIcon(packageManager),
                        mappedDrawableId,
                        IconPackCatalog.LIQUID_DARK_GLASS_ID,
                    ),
                    mapped = mappedDrawableId != null,
                )
            }
            .distinctBy { it.packageName to it.activityName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
            .toList()
    }

    fun renderIcon(app: DiscoveredAppIcon, pack: IconPackDefinition): Bitmap {
        val component = ComponentName(app.packageName, app.activityName).flattenToString()
        val key = RenderCacheKey(
            packageName = app.packageName,
            activityName = app.activityName,
            packId = pack.id,
            packageVersion = packageVersion(app.packageName),
        )
        return renderedIconCache[key] ?: renderedIconCache.computeIfAbsent(key) {
            val mappedDrawableId = mappedDrawableFor(pack.id, app.packageName, component)
            if (mappedDrawableId != null) {
                BitmapFactory.decodeResource(appContext.resources, mappedDrawableId)?.let { return@computeIfAbsent it }
            }

            val source = runCatching {
                packageManager.getActivityInfo(ComponentName(app.packageName, app.activityName), 0)
                    .loadIcon(packageManager)
            }.getOrElse {
                BitmapDrawable(appContext.resources, app.bitmap)
            }
            renderIcon(source, null, pack.id)
        }
    }

    fun coverage(pack: IconPackDefinition, apps: List<DiscoveredAppIcon>): IconPackCoverage {
        val mapped = apps.count { app ->
            val component = ComponentName(app.packageName, app.activityName).flattenToString()
            mappedDrawableFor(pack.id, app.packageName, component) != null
        }
        return IconPackCoverage(
            mappedCount = mapped,
            runtimeStyledCount = apps.size - mapped,
            styledCount = apps.size,
            totalCount = apps.size,
        )
    }

    fun launcherApplyResult(pack: IconPackDefinition): IconApplicationResult {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val launcherPackage = packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo
            ?.packageName
        return if (launcherPackage == "com.nothing.launcher") {
            val artifact = LauncherIconPackCatalog.forPack(pack.id)
            IconApplicationResult.ManualSelectionRequired(
                if (artifact != null) {
                    "${pack.name} is ready. Open Home > Customise > Icon Pack and choose ${artifact.label}."
                } else {
                    "Nothing Launcher does not expose a public direct icon-pack apply API. " +
                        "Choose ${pack.name} from the launcher icon settings."
                },
            )
        } else {
            IconApplicationResult.ManualSelectionRequired(
                "Choose ${pack.name} from your launcher's icon-pack settings. " +
                    "The launcher owns icon application and may not support external packs.",
            )
        }
    }

    private fun mappedDrawableFor(packId: String, packageName: String, component: String): Int? =
        approvedStaticMappings[packId]?.get(component)
            ?: when (packId) {
                IconPackCatalog.FLUFFY_ID -> essentialsMappings[packageName]
                    ?: socialMappings[packageName]
                    ?: googleMappings[packageName]

                IconPackCatalog.LIQUID_DARK_GLASS_ID -> darkGlassMappings[component]
                else -> null
            }

    private fun packageVersion(packageName: String): Long = runCatching {
        packageManager.getPackageInfo(packageName, 0).lastUpdateTime
    }.getOrDefault(0L)

    private fun loadGraphiteMappings(): Map<String, Int> {
        return loadDrawableMappings(R.xml.appfilter)
    }

    private fun loadDrawableMappings(resourceId: Int): Map<String, Int> {
        val parser = appContext.resources.getXml(resourceId)
        val result = mutableMapOf<String, Int>()
        try {
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawableName = parser.getAttributeValue(null, "drawable")
                    if (component?.startsWith("ComponentInfo{") == true && drawableName != null) {
                        val flattened = component.removePrefix("ComponentInfo{").removeSuffix("}")
                        val drawableId = appContext.resources.getIdentifier(
                            drawableName,
                            "drawable",
                            appContext.packageName,
                        )
                        if (drawableId != 0) result[flattened] = drawableId
                    }
                }
                event = parser.next()
            }
        } finally {
            parser.close()
        }
        return result
    }

    private fun renderIcon(
        source: Drawable,
        mappedDrawableId: Int?,
        packId: String,
    ): Bitmap {
        val size = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            88f,
            appContext.resources.displayMetrics,
        ).roundToInt().coerceAtLeast(64)

        if (mappedDrawableId != null) {
            BitmapFactory.decodeResource(appContext.resources, mappedDrawableId)?.let { return it }
        }

        val sourceBitmap = drawableBitmap(source, size)
        val normalizedSourceBitmap = IconForegroundNormalizer.normalize(
            sourceBitmap,
            foregroundTargetRect(size, packId),
        )
        return when (packId) {
            IconPackCatalog.FLUFFY_ID -> renderPlushIcon(normalizedSourceBitmap, size, packId)
            IconPackCatalog.LIQUID_DARK_GLASS_ID -> renderLiquidDarkGlassIcon(normalizedSourceBitmap, size)
            IconPackCatalog.LIQUID_SIRI_GLASS_ID -> renderLiquidSiriGlassIcon(normalizedSourceBitmap, size)
            IconPackCatalog.LUXURY_VIP_ID -> renderLuxuryVipIcon(normalizedSourceBitmap, size)
            else -> renderLiquidDarkGlassIcon(normalizedSourceBitmap, size)
        }
    }

    private fun foregroundTargetRect(size: Int, packId: String): RectF {
        val inset = when (packId) {
            IconPackCatalog.LIQUID_DARK_GLASS_ID -> 0.025f
            IconPackCatalog.LIQUID_SIRI_GLASS_ID -> 0.035f
            IconPackCatalog.LUXURY_VIP_ID -> 0.045f
            else -> 0.045f
        }
        return RectF(
            size * inset,
            size * inset,
            size * (1f - inset),
            size * (1f - inset),
        )
    }

    private fun renderLiquidDarkGlassIcon(sourceBitmap: Bitmap, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bounds = RectF(size * 0.025f, size * 0.025f, size * 0.975f, size * 0.975f)
        val radius = size * 0.22f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                0f,
                0f,
                size.toFloat(),
                size.toFloat(),
                Color.rgb(5, 8, 16),
                Color.rgb(48, 38, 78),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(bounds, radius, radius, paint)

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                size * 0.28f,
                size * 0.18f,
                size * 0.85f,
                intArrayOf(Color.argb(110, 155, 125, 255), Color.argb(28, 76, 179, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 0.46f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(bounds, radius, radius, glow)

        canvas.save()
        canvas.clipPath(android.graphics.Path().apply { addRoundRect(bounds, radius, radius, android.graphics.Path.Direction.CW) })
        val reflection = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(70, 255, 255, 255)
            strokeWidth = size * 0.055f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(size * 0.08f, size * 0.30f, size * 0.76f, size * 0.08f, reflection)
        reflection.color = Color.argb(52, 100, 213, 255)
        reflection.strokeWidth = size * 0.025f
        canvas.drawLine(size * 0.04f, size * 0.82f, size * 0.95f, size * 0.25f, reflection)
        canvas.restore()

        paint.style = Paint.Style.STROKE
        paint.shader = null
        paint.strokeWidth = size * 0.025f
        paint.color = Color.rgb(139, 124, 255)
        paint.setShadowLayer(size * 0.075f, 0f, 0f, Color.rgb(205, 111, 255))
        canvas.drawRoundRect(bounds.insetRect(size * 0.012f, size * 0.012f), radius * 0.95f, radius * 0.95f, paint)
        paint.clearShadowLayer()

        val iconBounds = RectF(0f, 0f, size.toFloat(), size.toFloat())
        drawMaterializedArtwork(canvas, sourceBitmap, iconBounds, IconPackCatalog.LIQUID_DARK_GLASS_ID, size)
        return bitmap
    }

    private fun renderLiquidSiriGlassIcon(sourceBitmap: Bitmap, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bounds = RectF(size * 0.035f, size * 0.035f, size * 0.965f, size * 0.965f)
        val radius = size * 0.23f
        val fill = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                size.toFloat(),
                size.toFloat(),
                Color.argb(238, 224, 255, 255),
                Color.argb(232, 191, 150, 255),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(bounds, radius, radius, fill)

        val bloom = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                size * 0.30f,
                size * 0.16f,
                size * 0.90f,
                intArrayOf(Color.argb(150, 255, 255, 255), Color.argb(48, 118, 226, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 0.42f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(bounds, radius, radius, bloom)

        canvas.save()
        canvas.clipPath(android.graphics.Path().apply { addRoundRect(bounds, radius, radius, android.graphics.Path.Direction.CW) })
        val caustic = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(88, 255, 255, 255)
            strokeWidth = size * 0.065f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(size * 0.04f, size * 0.29f, size * 0.78f, size * 0.07f, caustic)
        caustic.color = Color.argb(58, 255, 131, 225)
        caustic.strokeWidth = size * 0.034f
        canvas.drawLine(size * 0.05f, size * 0.83f, size * 0.98f, size * 0.28f, caustic)
        canvas.restore()

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.025f
            color = Color.argb(235, 250, 255, 255)
            setShadowLayer(size * 0.075f, 0f, size * 0.02f, Color.rgb(116, 199, 255))
        }
        canvas.drawRoundRect(bounds.insetRect(size * 0.012f, size * 0.012f), radius * 0.96f, radius * 0.96f, glow)
        glow.clearShadowLayer()

        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(104, 255, 255, 255)
        }
        canvas.drawOval(
            RectF(size * 0.12f, size * 0.08f, size * 0.80f, size * 0.25f),
            highlight,
        )

        val iconBounds = RectF(0f, 0f, size.toFloat(), size.toFloat())
        drawMaterializedArtwork(canvas, sourceBitmap, iconBounds, IconPackCatalog.LIQUID_SIRI_GLASS_ID, size)
        return bitmap
    }

    private fun renderLuxuryVipIcon(sourceBitmap: Bitmap, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bounds = RectF(size * 0.045f, size * 0.045f, size * 0.955f, size * 0.955f)
        val radius = size * 0.20f

        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(110, 0, 0, 0)
            maskFilter = BlurMaskFilter(size * 0.045f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            RectF(bounds.left, bounds.top + size * 0.035f, bounds.right, bounds.bottom + size * 0.035f),
            radius,
            radius,
            shadow,
        )

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                bounds.top,
                0f,
                bounds.bottom,
                Color.rgb(60, 61, 63),
                Color.rgb(8, 9, 11),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(bounds, radius, radius, fill)

        val sheen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                size * 0.28f,
                size * 0.18f,
                size * 0.90f,
                intArrayOf(Color.argb(120, 255, 226, 152), Color.argb(30, 160, 112, 24), Color.TRANSPARENT),
                floatArrayOf(0f, 0.40f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(bounds, radius, radius, sheen)

        canvas.save()
        canvas.clipPath(android.graphics.Path().apply { addRoundRect(bounds, radius, radius, android.graphics.Path.Direction.CW) })
        val metal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(62, 255, 234, 160)
            strokeWidth = size * 0.018f
        }
        var line = -size * 0.2f
        while (line < size * 1.2f) {
            canvas.drawLine(line, size * 0.04f, line + size * 0.70f, size * 0.96f, metal)
            line += size * 0.075f
        }
        canvas.restore()

        val gold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.022f
            color = Color.rgb(212, 175, 55)
        }
        canvas.drawRoundRect(bounds.insetRect(size * 0.012f, size * 0.012f), radius * 0.96f, radius * 0.96f, gold)
        gold.color = Color.argb(140, 255, 235, 177)
        gold.strokeWidth = size * 0.008f
        canvas.drawRoundRect(bounds.insetRect(size * 0.055f, size * 0.055f), radius * 0.78f, radius * 0.78f, gold)

        val iconBounds = RectF(0f, 0f, size.toFloat(), size.toFloat())
        drawMaterializedArtwork(canvas, sourceBitmap, iconBounds, IconPackCatalog.LUXURY_VIP_ID, size)
        return bitmap
    }

    private fun renderPlushIcon(sourceBitmap: Bitmap, size: Int, packId: String): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val dominant = averageOpaqueColor(sourceBitmap)

        val base = when (packId) {
            IconPackCatalog.FLUFFY_ID -> blendColors(dominant, Color.WHITE, 0.20f)
            else -> blendColors(dominant, Color.WHITE, 0.20f)
        }
        val border = blendColors(base, Color.WHITE, 0.35f)
        val bounds = RectF(size * 0.045f, size * 0.045f, size * 0.955f, size * 0.955f)
        val radius = size * 0.24f

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(62, 0, 0, 0)
            maskFilter = BlurMaskFilter(size * 0.035f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            RectF(bounds.left + size * 0.012f, bounds.top + size * 0.025f, bounds.right + size * 0.012f, bounds.bottom + size * 0.025f),
            radius,
            radius,
            shadowPaint,
        )

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = base
        }
        canvas.drawRoundRect(bounds, radius, radius, fill)

        val save = canvas.save()
        val clipPath = Path().apply {
            addRoundRect(bounds, radius, radius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        val random = Random(packId.hashCode() xor dominant)
        val fiberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
        }
        repeat((size * 2.8f).roundToInt().coerceAtLeast(180)) {
            val x = random.nextFloat() * size
            val y = random.nextFloat() * size
            val length = size * (0.010f + random.nextFloat() * 0.020f)
            fiberPaint.strokeWidth = size * (0.0035f + random.nextFloat() * 0.0045f)
            fiberPaint.color = if (random.nextBoolean()) {
                Color.argb(30 + random.nextInt(30), 255, 255, 255)
            } else {
                Color.argb(14 + random.nextInt(18), 0, 0, 0)
            }
            canvas.drawLine(x, y, x + length, y + random.nextFloat() * length * 0.45f, fiberPaint)
        }
        canvas.restoreToCount(save)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.018f
            color = border
        }
        canvas.drawRoundRect(bounds.insetRect(size * 0.012f, size * 0.012f), radius * 0.94f, radius * 0.94f, borderPaint)

        val iconRect = RectF(0f, 0f, size.toFloat(), size.toFloat())

        drawMaterializedArtwork(canvas, sourceBitmap, iconRect, IconPackCatalog.FLUFFY_ID, size)

        return bitmap
    }

    private fun drawMaterializedArtwork(
        canvas: Canvas,
        sourceBitmap: Bitmap,
        iconRect: RectF,
        style: String,
        size: Int,
    ) {
        val profile = IconMaterialMath.profile(sourceBitmap)
        when (style) {
            IconPackCatalog.FLUFFY_ID -> drawFluffyForeground(canvas, sourceBitmap, iconRect, size)
            IconPackCatalog.LIQUID_DARK_GLASS_ID -> drawDarkGlassForeground(canvas, sourceBitmap, iconRect, size, profile)
            IconPackCatalog.LIQUID_SIRI_GLASS_ID -> drawSiriGlassForeground(canvas, sourceBitmap, iconRect, size)
            IconPackCatalog.LUXURY_VIP_ID -> drawLuxuryForeground(canvas, sourceBitmap, iconRect, size, profile)
        }
    }

    private fun drawFluffyForeground(
        canvas: Canvas,
        sourceBitmap: Bitmap,
        iconRect: RectF,
        size: Int,
    ) {
        val layer = canvas.saveLayer(iconRect, null)
        drawTintedBitmap(canvas, sourceBitmap, iconRect, Color.BLACK, 92, size * 0.014f, size * 0.024f, size * 0.014f)
        drawTintedBitmap(canvas, sourceBitmap, iconRect, Color.WHITE, 70, -size * 0.008f, -size * 0.012f, size * 0.010f)
        drawBaseBitmap(canvas, sourceBitmap, iconRect, 0.90f)

        val overlay = foregroundOverlayPaint()
        overlay.shader = RadialGradient(
            iconRect.left + iconRect.width() * 0.34f,
            iconRect.top + iconRect.height() * 0.28f,
            iconRect.width() * 0.86f,
            intArrayOf(
                Color.argb(125, 255, 255, 255),
                Color.argb(55, 255, 224, 242),
                Color.argb(78, 160, 72, 125),
            ),
            floatArrayOf(0f, 0.46f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(iconRect, overlay)
        overlay.shader = LinearGradient(
            iconRect.left,
            iconRect.top,
            iconRect.right,
            iconRect.bottom,
            intArrayOf(
                Color.argb(72, 255, 255, 255),
                Color.argb(26, 255, 177, 215),
                Color.argb(92, 118, 48, 94),
            ),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(iconRect, overlay)
        overlay.shader = null
        drawFiberTexture(canvas, iconRect, size, 0x51F1, 220, 70, 34)
        canvas.restoreToCount(layer)
    }

    private fun drawDarkGlassForeground(
        canvas: Canvas,
        sourceBitmap: Bitmap,
        iconRect: RectF,
        size: Int,
        profile: IconColorProfile,
    ) {
        val layer = canvas.saveLayer(iconRect, null)
        drawTintedBitmap(canvas, sourceBitmap, iconRect, Color.BLACK, 150, size * 0.012f, size * 0.026f, size * 0.016f)
        drawTintedBitmap(canvas, sourceBitmap, iconRect, Color.WHITE, 105, -size * 0.010f, -size * 0.012f, size * 0.008f)
        drawBaseBitmap(canvas, sourceBitmap, iconRect, 0.72f)

        val overlay = foregroundOverlayPaint()
        overlay.shader = LinearGradient(
            iconRect.left,
            iconRect.top,
            iconRect.right,
            iconRect.bottom,
            intArrayOf(
                Color.argb(92, 204, 229, 255),
                Color.argb(34, 58, 69, 112),
                Color.argb(135, 48, 24, 132),
            ),
            floatArrayOf(0f, 0.46f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(iconRect, overlay)
        overlay.shader = RadialGradient(
            iconRect.left + iconRect.width() * 0.26f,
            iconRect.top + iconRect.height() * 0.20f,
            iconRect.width() * 0.82f,
            intArrayOf(
                IconMaterialMath.withAlpha(profile.averageColor, 112),
                Color.argb(32, 106, 159, 255),
                Color.TRANSPARENT,
            ),
            floatArrayOf(0f, 0.38f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(iconRect, overlay)
        overlay.shader = null
        drawGlassStreak(canvas, iconRect, size, Color.argb(188, 255, 255, 255), size * 0.045f, 0.25f)
        drawGlassStreak(canvas, iconRect, size, Color.argb(92, 94, 215, 255), size * 0.018f, 0.72f)
        drawInnerEdge(canvas, iconRect, size, Color.argb(120, 12, 15, 34), 0.78f)
        canvas.restoreToCount(layer)
    }

    private fun drawSiriGlassForeground(
        canvas: Canvas,
        sourceBitmap: Bitmap,
        iconRect: RectF,
        size: Int,
    ) {
        val layer = canvas.saveLayer(iconRect, null)
        drawTintedBitmap(canvas, sourceBitmap, iconRect, Color.BLACK, 58, size * 0.010f, size * 0.020f, size * 0.010f)
        drawTintedBitmap(canvas, sourceBitmap, iconRect, Color.WHITE, 120, -size * 0.010f, -size * 0.014f, size * 0.008f)
        drawBaseBitmap(canvas, sourceBitmap, iconRect, 0.80f)

        val overlay = foregroundOverlayPaint()
        overlay.shader = LinearGradient(
            iconRect.left,
            iconRect.top,
            iconRect.right,
            iconRect.bottom,
            intArrayOf(
                Color.argb(150, 255, 255, 255),
                Color.argb(92, 119, 222, 255),
                Color.argb(104, 190, 164, 255),
                Color.argb(82, 255, 164, 221),
            ),
            floatArrayOf(0f, 0.32f, 0.68f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(iconRect, overlay)
        overlay.shader = RadialGradient(
            iconRect.left + iconRect.width() * 0.30f,
            iconRect.top + iconRect.height() * 0.16f,
            iconRect.width() * 0.90f,
            intArrayOf(Color.argb(130, 255, 255, 255), Color.argb(35, 131, 226, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(iconRect, overlay)
        overlay.shader = null
        drawGlassStreak(canvas, iconRect, size, Color.argb(218, 255, 255, 255), size * 0.052f, 0.24f)
        drawGlassStreak(canvas, iconRect, size, Color.argb(92, 255, 145, 225), size * 0.020f, 0.74f)
        drawInnerEdge(canvas, iconRect, size, Color.argb(80, 88, 174, 220), 0.80f)
        canvas.restoreToCount(layer)
    }

    private fun drawLuxuryForeground(
        canvas: Canvas,
        sourceBitmap: Bitmap,
        iconRect: RectF,
        size: Int,
        profile: IconColorProfile,
    ) {
        val layer = canvas.saveLayer(iconRect, null)
        drawTintedBitmap(canvas, sourceBitmap, iconRect, Color.BLACK, 172, size * 0.016f, size * 0.028f, size * 0.012f)
        drawTintedBitmap(canvas, sourceBitmap, iconRect, Color.rgb(255, 238, 176), 112, -size * 0.008f, -size * 0.010f, size * 0.006f)
        drawBaseBitmap(canvas, sourceBitmap, iconRect, (0.32f + profile.saturation * 0.16f).coerceIn(0.28f, 0.50f))

        val overlay = foregroundOverlayPaint()
        overlay.shader = LinearGradient(
            iconRect.left,
            iconRect.top,
            iconRect.right,
            iconRect.bottom,
            intArrayOf(
                Color.argb(168, 73, 47, 14),
                Color.argb(188, 255, 213, 105),
                Color.argb(205, 255, 244, 188),
                Color.argb(160, 167, 107, 24),
                Color.argb(178, 39, 25, 10),
            ),
            floatArrayOf(0f, 0.27f, 0.48f, 0.72f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(iconRect, overlay)
        overlay.shader = RadialGradient(
            iconRect.left + iconRect.width() * 0.28f,
            iconRect.top + iconRect.height() * 0.16f,
            iconRect.width() * 0.82f,
            intArrayOf(
                Color.argb(88, 255, 231, 147),
                Color.argb(34, 212, 175, 55),
                Color.TRANSPARENT,
            ),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(iconRect, overlay)
        overlay.shader = null
        drawGlassStreak(canvas, iconRect, size, Color.argb(178, 255, 250, 218), size * 0.028f, 0.22f)
        drawInnerEdge(canvas, iconRect, size, Color.argb(155, 38, 23, 7), 0.82f)
        drawMetallicEngraving(canvas, iconRect, size)
        canvas.restoreToCount(layer)
    }

    private fun drawTintedBitmap(
        canvas: Canvas,
        sourceBitmap: Bitmap,
        rect: RectF,
        color: Int,
        alpha: Int,
        dx: Float,
        dy: Float,
        blur: Float,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            this.alpha = alpha.coerceIn(0, 255)
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            if (blur > 0f) maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawBitmap(
            sourceBitmap,
            null,
            RectF(rect.left + dx, rect.top + dy, rect.right + dx, rect.bottom + dy),
            paint,
        )
    }

    private fun drawBaseBitmap(canvas: Canvas, sourceBitmap: Bitmap, rect: RectF, saturation: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        if (saturation != 1f) {
            paint.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(saturation) })
        }
        canvas.drawBitmap(sourceBitmap, null, rect, paint)
    }

    private fun foregroundOverlayPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    }

    private fun drawFiberTexture(
        canvas: Canvas,
        rect: RectF,
        size: Int,
        seed: Int,
        count: Int,
        lightAlpha: Int,
        darkAlpha: Int,
    ) {
        val fiber = foregroundOverlayPaint().apply { strokeCap = Paint.Cap.ROUND }
        val random = Random(seed xor size)
        repeat(count) {
            val x = rect.left + random.nextFloat() * rect.width()
            val y = rect.top + random.nextFloat() * rect.height()
            val length = size * (0.010f + random.nextFloat() * 0.028f)
            fiber.strokeWidth = size * (0.0028f + random.nextFloat() * 0.0048f)
            fiber.color = if (random.nextBoolean()) {
                Color.argb(lightAlpha, 255, 255, 255)
            } else {
                Color.argb(darkAlpha, 48, 16, 42)
            }
            canvas.drawLine(x, y, x + length, y - length * 0.30f, fiber)
        }
    }

    private fun drawGlassStreak(canvas: Canvas, rect: RectF, size: Int, color: Int, strokeWidth: Float, heightFraction: Float) {
        val paint = foregroundOverlayPaint().apply {
            this.color = color
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        val path = Path().apply {
            moveTo(rect.left - size * 0.08f, rect.top + rect.height() * heightFraction)
            cubicTo(
                rect.left + rect.width() * 0.24f,
                rect.top + rect.height() * (heightFraction - 0.08f),
                rect.left + rect.width() * 0.56f,
                rect.top + rect.height() * 0.12f,
                rect.right + size * 0.04f,
                rect.top - size * 0.02f,
            )
        }
        canvas.drawPath(path, paint)
    }

    private fun drawInnerEdge(canvas: Canvas, rect: RectF, size: Int, color: Int, startFraction: Float) {
        val paint = foregroundOverlayPaint().apply {
            this.color = color
            strokeWidth = size * 0.020f
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        val path = Path().apply {
            moveTo(rect.left, rect.top + rect.height() * startFraction)
            cubicTo(
                rect.left + rect.width() * 0.30f,
                rect.bottom + size * 0.01f,
                rect.right - rect.width() * 0.14f,
                rect.bottom - rect.height() * 0.08f,
                rect.right,
                rect.top + rect.height() * 0.22f,
            )
        }
        canvas.drawPath(path, paint)
    }

    private fun drawMetallicEngraving(canvas: Canvas, rect: RectF, size: Int) {
        val engraving = foregroundOverlayPaint().apply {
            color = Color.argb(102, 255, 241, 171)
            strokeWidth = size * 0.008f
            strokeCap = Paint.Cap.ROUND
        }
        var y = rect.top - size * 0.05f
        while (y < rect.bottom + size * 0.10f) {
            canvas.drawLine(rect.left, y, rect.right, y - size * 0.12f, engraving)
            y += size * 0.075f
        }
    }

    private fun drawableBitmap(source: Drawable, size: Int): Bitmap {
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        source.bounds = Rect(0, 0, size, size)
        source.draw(canvas)
        return result
    }

    private fun averageOpaqueColor(bitmap: Bitmap): Int {
        return IconMaterialMath.profile(bitmap).averageColor
    }

    private fun blendColors(from: Int, to: Int, amount: Float): Int {
        val t = amount.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(from) + (Color.red(to) - Color.red(from)) * t).roundToInt().coerceIn(0, 255),
            (Color.green(from) + (Color.green(to) - Color.green(from)) * t).roundToInt().coerceIn(0, 255),
            (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t).roundToInt().coerceIn(0, 255),
        )
    }
}

private data class RenderCacheKey(
    val packageName: String,
    val activityName: String,
    val packId: String,
    val packageVersion: Long,
)

private fun RectF.insetRect(dx: Float, dy: Float): RectF =
    RectF(left + dx, top + dy, right - dx, bottom - dy)
