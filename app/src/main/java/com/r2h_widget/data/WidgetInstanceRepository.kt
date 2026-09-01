package com.r2h_widget.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.r2h_widget.catalog.WidgetProductIds
import com.r2h_widget.widget.clock.ClockAccent
import com.r2h_widget.widget.clock.ClockDateAlignment
import com.r2h_widget.widget.clock.ClockDateFormatPreset
import com.r2h_widget.widget.clock.ClockDatePlacement
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.ClockMonthStyle
import com.r2h_widget.widget.clock.ClockTimeAlignment
import com.r2h_widget.widget.clock.ClockTimeSeparator
import com.r2h_widget.widget.clock.ClockWeekdayStyle
import com.r2h_widget.widget.clock.ClockYearStyle
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.clock.ResponsiveWidgetConfiguration
import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetActionType
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundFill
import com.r2h_widget.widget.common.WidgetBackgroundShape
import com.r2h_widget.widget.common.WidgetEffectsConfiguration
import com.r2h_widget.widget.clock.WidgetLayoutConfiguration
import com.r2h_widget.widget.clock.WidgetTypographyConfiguration
import com.r2h_widget.widget.clock.ClockContentConfiguration
import com.r2h_widget.widget.clock.ClockDateConfiguration
import kotlinx.coroutines.flow.first

data class WidgetInstance(
    val appWidgetId: Int,
    val productId: String,
    val configuration: DigitalClockConfiguration,
)

interface WidgetInstanceRepository {
    suspend fun get(appWidgetId: Int): WidgetInstance?

    suspend fun save(instance: WidgetInstance)

    suspend fun delete(appWidgetId: Int)
}

object WidgetInstanceCodec {
    private const val PREFIX = "#*!"
    private const val CURRENT_VERSION = "2"
    private const val FIELD_SEPARATOR = "|"
    private const val KEY_VALUE_SEPARATOR = ";"
    private const val ASSIGNMENT = "="

    /**
     * Version 2 is intentionally explicit and key-based. This keeps new optional fields
     * migration-safe without making the domain layer a bag of strings.
     */
    fun encode(instance: WidgetInstance): String {
        val configuration = instance.configuration
        val values = linkedMapOf(
            "use24" to configuration.content.use24HourFormat,
            "ampm" to configuration.content.showAmPm,
            "leadingZero" to configuration.content.leadingZero,
            "separator" to configuration.content.separator.name,
            "timeAlign" to configuration.content.alignment.name,
            "seconds" to configuration.content.showSeconds,
            "dateEnabled" to configuration.date.enabled,
            "datePlacement" to configuration.date.placement.name,
            "dateAlign" to configuration.date.alignment.name,
            "weekday" to configuration.date.weekdayStyle.name,
            "month" to configuration.date.monthStyle.name,
            "year" to configuration.date.yearStyle.name,
            "datePreset" to configuration.date.formatPreset.name,
            "fill" to configuration.background.fill.name,
            "solid" to configuration.background.solidColorHex,
            "solidOpacity" to configuration.background.solidOpacity,
            "gradientStart" to configuration.background.gradientStartHex,
            "gradientEnd" to configuration.background.gradientEndHex,
            "gradientAngle" to configuration.background.gradientAngleDegrees,
            "backgroundOpacity" to configuration.background.opacity,
            "shape" to configuration.background.shape.name,
            "cornerRadius" to configuration.background.cornerRadiusDp,
            "borderEnabled" to configuration.background.borderEnabled,
            "borderWidth" to configuration.background.borderWidthDp,
            "borderColor" to configuration.background.borderColorHex,
            "borderOpacity" to configuration.background.borderOpacity,
            "highlightEnabled" to configuration.background.highlightEnabled,
            "highlightColor" to configuration.background.highlightColorHex,
            "highlightOpacity" to configuration.background.highlightOpacity,
            "timeFont" to configuration.typography.timeFontFamily.name,
            "timeWeight" to configuration.typography.timeFontWeight,
            "timeScale" to configuration.typography.timeScale,
            "timeColor" to configuration.typography.timeColorHex,
            "letterSpacing" to configuration.typography.letterSpacing,
            "dateFont" to configuration.typography.dateFontFamily.name,
            "dateWeight" to configuration.typography.dateFontWeight,
            "dateScale" to configuration.typography.dateScale,
            "dateColor" to configuration.typography.dateColorHex,
            "dateOpacity" to configuration.typography.dateOpacity,
            "padding" to configuration.layout.contentPaddingDp,
            "horizontalOffset" to configuration.layout.horizontalOffsetDp,
            "verticalOffset" to configuration.layout.verticalOffsetDp,
            "timeDateGap" to configuration.layout.timeDateGapDp,
            "layoutAlign" to configuration.layout.alignment.name,
            "glowEnabled" to configuration.effects.glowEnabled,
            "glowColor" to configuration.effects.glowColorHex,
            "glowStrength" to configuration.effects.glowStrength,
            "action" to configuration.action.type.name,
            "actionPackage" to configuration.action.packageName.orEmpty(),
            "compactScale" to configuration.responsive.compactScale,
            "mediumScale" to configuration.responsive.mediumScale,
            "expandedScale" to configuration.responsive.expandedScale,
            "compactShowDate" to configuration.responsive.compactShowDate,
            "presetId" to configuration.presetId.orEmpty(),
        )
        val payload = values.entries.joinToString(KEY_VALUE_SEPARATOR) { (key, value) ->
            "$key$ASSIGNMENT${value.toString().replace(";", "")}" 
        }
        return listOf(PREFIX + CURRENT_VERSION, instance.productId, payload).joinToString(FIELD_SEPARATOR)
    }

    fun decode(appWidgetId: Int, value: String): WidgetInstance? {
        val fields = value.split(FIELD_SEPARATOR, limit = 3)
        if (fields.size < 3) return decodeLegacyPipeValue(appWidgetId, value)
        return when (fields[0]) {
            PREFIX + CURRENT_VERSION -> decodeVersionTwo(appWidgetId, fields[1], fields[2])
            PREFIX + "1" -> decodeLegacyPipeValue(appWidgetId, value.removePrefix(PREFIX))
            "1" -> decodeLegacyPipeValue(appWidgetId, value)
            else -> null
        }
    }

    fun isCurrent(value: String): Boolean = value.startsWith(PREFIX + CURRENT_VERSION + FIELD_SEPARATOR)

    private fun decodeVersionTwo(appWidgetId: Int, productId: String, payload: String): WidgetInstance? {
        if (productId.isBlank()) return null
        val values = payload.split(KEY_VALUE_SEPARATOR).mapNotNull { entry ->
            val separatorIndex = entry.indexOf(ASSIGNMENT)
            if (separatorIndex <= 0) null else entry.substring(0, separatorIndex) to entry.substring(separatorIndex + 1)
        }.toMap()
        val defaults = DigitalClockConfiguration()
        fun bool(key: String, default: Boolean) = values[key]?.toBooleanStrictOrNull() ?: default
        fun float(key: String, default: Float) = values[key]?.toFloatOrNull() ?: default
        fun int(key: String, default: Int) = values[key]?.toIntOrNull() ?: default
        fun text(key: String, default: String) = values[key].orEmpty().ifBlank { default }
        fun <T : Enum<T>> enum(key: String, default: T, values: Array<T>): T = values.firstOrNull {
            it.name == text(key, default.name)
        } ?: default

        val accent = ClockAccent.fromHex(text("timeColor", defaults.typography.timeColorHex))
        val configuration = DigitalClockConfiguration(
            content = ClockContentConfiguration(
                use24HourFormat = bool("use24", defaults.content.use24HourFormat),
                showAmPm = bool("ampm", defaults.content.showAmPm),
                leadingZero = bool("leadingZero", defaults.content.leadingZero),
                separator = enum("separator", defaults.content.separator, ClockTimeSeparator.entries.toTypedArray()),
                showSeconds = bool("seconds", defaults.content.showSeconds),
                alignment = enum("timeAlign", defaults.content.alignment, ClockTimeAlignment.entries.toTypedArray()),
            ),
            date = ClockDateConfiguration(
                enabled = bool("dateEnabled", defaults.date.enabled),
                placement = enum("datePlacement", defaults.date.placement, ClockDatePlacement.entries.toTypedArray()),
                alignment = enum("dateAlign", defaults.date.alignment, ClockDateAlignment.entries.toTypedArray()),
                weekdayStyle = enum("weekday", defaults.date.weekdayStyle, ClockWeekdayStyle.entries.toTypedArray()),
                monthStyle = enum("month", defaults.date.monthStyle, ClockMonthStyle.entries.toTypedArray()),
                yearStyle = enum("year", defaults.date.yearStyle, ClockYearStyle.entries.toTypedArray()),
                formatPreset = enum("datePreset", defaults.date.formatPreset, ClockDateFormatPreset.entries.toTypedArray()),
            ),
            background = WidgetBackgroundConfiguration(
                fill = enum("fill", defaults.background.fill, WidgetBackgroundFill.entries.toTypedArray()),
                solidColorHex = text("solid", defaults.background.solidColorHex),
                solidOpacity = float("solidOpacity", defaults.background.solidOpacity),
                gradientStartHex = text("gradientStart", defaults.background.gradientStartHex),
                gradientEndHex = text("gradientEnd", defaults.background.gradientEndHex),
                gradientAngleDegrees = float("gradientAngle", defaults.background.gradientAngleDegrees),
                opacity = float("backgroundOpacity", defaults.background.opacity),
                shape = enum("shape", defaults.background.shape, WidgetBackgroundShape.entries.toTypedArray()),
                cornerRadiusDp = float("cornerRadius", defaults.background.cornerRadiusDp),
                borderEnabled = bool("borderEnabled", defaults.background.borderEnabled),
                borderWidthDp = float("borderWidth", defaults.background.borderWidthDp),
                borderColorHex = text("borderColor", defaults.background.borderColorHex),
                borderOpacity = float("borderOpacity", defaults.background.borderOpacity),
                highlightEnabled = bool("highlightEnabled", defaults.background.highlightEnabled),
                highlightColorHex = text("highlightColor", defaults.background.highlightColorHex),
                highlightOpacity = float("highlightOpacity", defaults.background.highlightOpacity),
            ),
            typography = WidgetTypographyConfiguration(
                timeFontFamily = enum("timeFont", defaults.typography.timeFontFamily, ClockFontFamily.entries.toTypedArray()),
                timeFontWeight = int("timeWeight", defaults.typography.timeFontWeight),
                timeScale = float("timeScale", defaults.typography.timeScale),
                timeColorHex = text("timeColor", accent.colorHex),
                letterSpacing = float("letterSpacing", defaults.typography.letterSpacing),
                dateFontFamily = enum("dateFont", defaults.typography.dateFontFamily, ClockFontFamily.entries.toTypedArray()),
                dateFontWeight = int("dateWeight", defaults.typography.dateFontWeight),
                dateScale = float("dateScale", defaults.typography.dateScale),
                dateColorHex = text("dateColor", defaults.typography.dateColorHex),
                dateOpacity = float("dateOpacity", defaults.typography.dateOpacity),
            ),
            layout = WidgetLayoutConfiguration(
                contentPaddingDp = float("padding", defaults.layout.contentPaddingDp),
                horizontalOffsetDp = float("horizontalOffset", defaults.layout.horizontalOffsetDp),
                verticalOffsetDp = float("verticalOffset", defaults.layout.verticalOffsetDp),
                timeDateGapDp = float("timeDateGap", defaults.layout.timeDateGapDp),
                alignment = enum("layoutAlign", defaults.layout.alignment, ClockTimeAlignment.entries.toTypedArray()),
            ),
            effects = WidgetEffectsConfiguration(
                glowEnabled = bool("glowEnabled", defaults.effects.glowEnabled),
                glowColorHex = text("glowColor", defaults.effects.glowColorHex),
                glowStrength = float("glowStrength", defaults.effects.glowStrength),
            ),
            action = WidgetActionConfiguration(
                type = enum("action", defaults.action.type, WidgetActionType.entries.toTypedArray()),
                packageName = values["actionPackage"]?.ifBlank { null },
            ),
            responsive = ResponsiveWidgetConfiguration(
                compactScale = float("compactScale", defaults.responsive.compactScale),
                mediumScale = float("mediumScale", defaults.responsive.mediumScale),
                expandedScale = float("expandedScale", defaults.responsive.expandedScale),
                compactShowDate = bool("compactShowDate", defaults.responsive.compactShowDate),
            ),
            presetId = values["presetId"]?.ifBlank { null },
        )
        return WidgetInstance(appWidgetId, WidgetProductIds.canonicalize(productId), configuration)
    }

    private fun decodeLegacyPipeValue(appWidgetId: Int, value: String): WidgetInstance? {
        val fields = value.split(FIELD_SEPARATOR)
        if (fields.size != 6 || fields[0] != "1") return null
        val use24HourFormat = fields[2].toBooleanStrictOrNull() ?: return null
        val showDate = fields[3].toBooleanStrictOrNull() ?: return null
        val showWeekday = fields[4].toBooleanStrictOrNull() ?: return null
        val accent = runCatching { ClockAccent.valueOf(fields[5]) }.getOrNull() ?: return null
        return WidgetInstance(
            appWidgetId = appWidgetId,
            productId = WidgetProductIds.canonicalize(fields[1]),
            configuration = DigitalClockConfiguration(
                use24HourFormat = use24HourFormat,
                showDate = showDate,
                showWeekday = showWeekday,
                accent = accent,
            ),
        )
    }
}

class InMemoryWidgetInstanceRepository : WidgetInstanceRepository {
    private val instances = mutableMapOf<Int, WidgetInstance>()

    override suspend fun get(appWidgetId: Int): WidgetInstance? = instances[appWidgetId]

    override suspend fun save(instance: WidgetInstance) {
        instances[instance.appWidgetId] = instance
    }

    override suspend fun delete(appWidgetId: Int) {
        instances.remove(appWidgetId)
    }
}

class DataStoreWidgetInstanceRepository(
    private val preferencesDataStore: DataStore<Preferences>,
) : WidgetInstanceRepository {
    override suspend fun get(appWidgetId: Int): WidgetInstance? {
        val value = preferencesDataStore.data.first()[keyFor(appWidgetId)] ?: return null
        val instance = WidgetInstanceCodec.decode(appWidgetId, value) ?: return null
        // Re-save when the stored form is outdated, including legacy preset product IDs.
        if (WidgetInstanceCodec.encode(instance) != value) save(instance)
        return instance
    }

    override suspend fun save(instance: WidgetInstance) {
        preferencesDataStore.edit { preferences ->
            preferences[keyFor(instance.appWidgetId)] = WidgetInstanceCodec.encode(instance)
        }
    }

    override suspend fun delete(appWidgetId: Int) {
        preferencesDataStore.edit { preferences ->
            preferences.remove(keyFor(appWidgetId))
        }
    }

    private fun keyFor(appWidgetId: Int): Preferences.Key<String> =
        stringPreferencesKey("widget_instance_$appWidgetId")
}
