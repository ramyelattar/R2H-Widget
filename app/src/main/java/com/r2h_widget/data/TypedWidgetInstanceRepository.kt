package com.r2h_widget.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.r2h_widget.catalog.WidgetProductIds
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.analog.AnalogCapStyle
import com.r2h_widget.widget.analog.AnalogMarkerStyle
import com.r2h_widget.widget.analog.AnalogNumberStyle
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetLayout
import com.r2h_widget.widget.calendar.CalendarWidgetConfiguration
import com.r2h_widget.widget.clock.ClockDateAlignment
import com.r2h_widget.widget.clock.ClockDateConfiguration
import com.r2h_widget.widget.clock.ClockDateFormatPreset
import com.r2h_widget.widget.clock.ClockDatePlacement
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.ClockMonthStyle
import com.r2h_widget.widget.clock.ClockWeekdayStyle
import com.r2h_widget.widget.clock.ClockYearStyle
import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetActionType
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundFill
import com.r2h_widget.widget.common.WidgetBackgroundShape
import com.r2h_widget.widget.search.SearchTapAction
import com.r2h_widget.widget.search.SearchTarget
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import com.r2h_widget.widget.music.MusicWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherUnits
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetLayout
import kotlinx.coroutines.flow.first

/**
 * Persisted configuration for every widget family that is not the digital clock.
 * One record per launcher appWidgetId, holding the canonical product ID and the
 * fully typed studio configuration.
 */
sealed interface TypedWidgetConfiguration {
    data class Battery(val value: BatteryWidgetConfiguration) : TypedWidgetConfiguration
    data class Weather(val value: WeatherWidgetConfiguration) : TypedWidgetConfiguration
    data class Analog(val value: AnalogClockConfiguration) : TypedWidgetConfiguration
    data class Search(val value: SearchWidgetConfiguration) : TypedWidgetConfiguration
    data class Music(val value: MusicWidgetConfiguration) : TypedWidgetConfiguration

    /** Legacy records for calendar widgets pinned before the catalog was simplified. */
    data class Calendar(val value: CalendarWidgetConfiguration) : TypedWidgetConfiguration
}

data class TypedWidgetInstance(
    val appWidgetId: Int,
    val productId: String,
    val configuration: TypedWidgetConfiguration,
)

interface TypedWidgetInstanceRepository {
    suspend fun get(appWidgetId: Int): TypedWidgetInstance?

    suspend fun save(instance: TypedWidgetInstance)

    suspend fun delete(appWidgetId: Int)
}

object TypedWidgetInstanceCodec {
    private const val PREFIX = "#!"
    private const val CURRENT_VERSION = "2"
    private const val LEGACY_VERSION = "1"

    fun encode(instance: TypedWidgetInstance): String {
        val fields = FieldWriter()
        val type = when (val configuration = instance.configuration) {
            is TypedWidgetConfiguration.Battery -> {
                configuration.value.writeFields(fields)
                "BATTERY"
            }
            is TypedWidgetConfiguration.Weather -> {
                configuration.value.writeFields(fields)
                "WEATHER"
            }
            is TypedWidgetConfiguration.Analog -> {
                configuration.value.writeFields(fields)
                "ANALOG"
            }
            is TypedWidgetConfiguration.Search -> {
                configuration.value.writeFields(fields)
                "SEARCH"
            }
            is TypedWidgetConfiguration.Music -> {
                configuration.value.writeFields(fields)
                "MUSIC"
            }
            is TypedWidgetConfiguration.Calendar -> {
                configuration.value.writeFields(fields)
                "CALENDAR"
            }
        }
        return listOf(
            PREFIX + CURRENT_VERSION,
            instance.productId,
            type,
            fields.payload(),
        ).joinToString("|")
    }

    fun decode(appWidgetId: Int, value: String): TypedWidgetInstance? {
        val parts = value.split('|', limit = 4)
        if (parts.size != 4) return null
        val (header, productId, type, payload) = parts
        if (productId.isBlank()) return null
        val canonicalProductId = WidgetProductIds.canonicalize(productId)
        return when (header) {
            PREFIX + CURRENT_VERSION -> decodeVersionTwo(appWidgetId, canonicalProductId, type, payload)
            PREFIX + LEGACY_VERSION -> decodeLegacy(appWidgetId, canonicalProductId, type, payload)
            else -> null
        }
    }

    private fun decodeVersionTwo(appWidgetId: Int, productId: String, type: String, payload: String): TypedWidgetInstance? {
        val reader = FieldReader(payload)
        val configuration = when (type) {
            "BATTERY" -> TypedWidgetConfiguration.Battery(readBatteryFields(reader))
            "WEATHER" -> TypedWidgetConfiguration.Weather(readWeatherFields(reader))
            "ANALOG" -> TypedWidgetConfiguration.Analog(readAnalogFields(reader))
            "SEARCH" -> TypedWidgetConfiguration.Search(readSearchFields(reader))
            "MUSIC" -> TypedWidgetConfiguration.Music(readMusicFields(reader))
            "CALENDAR" -> TypedWidgetConfiguration.Calendar(readCalendarFields(reader))
            else -> return null
        }
        return TypedWidgetInstance(appWidgetId, productId, configuration)
    }

    private fun decodeLegacy(appWidgetId: Int, productId: String, type: String, payload: String): TypedWidgetInstance? {
        val values = payload.split(';').mapNotNull { entry ->
            val index = entry.indexOf('=')
            if (index <= 0) null else entry.substring(0, index) to entry.substring(index + 1)
        }.toMap()
        fun bool(key: String, default: Boolean) = values[key]?.toBooleanStrictOrNull() ?: default
        val configuration = when (type) {
            "BATTERY" -> TypedWidgetConfiguration.Battery(
                BatteryWidgetConfiguration(
                    showPercentage = bool("percentage", true),
                    showChargingState = bool("charging", true),
                    showPlugSource = bool("plug", false),
                ),
            )
            "WEATHER" -> TypedWidgetConfiguration.Weather(
                WeatherWidgetConfiguration(locationName = values["location"]?.ifBlank { null }),
            )
            "CALENDAR" -> TypedWidgetConfiguration.Calendar(
                CalendarWidgetConfiguration(
                    showWeekday = bool("weekday", true),
                    showMonth = bool("month", true),
                ),
            )
            else -> return null
        }
        return TypedWidgetInstance(appWidgetId, productId, configuration)
    }
}

private class FieldWriter {
    private val entries = mutableListOf<String>()

    fun add(key: String, value: Any?) {
        entries.add("$key=${value.toString().replace(";", "").replace("|", "").replace("\n", " ")}")
    }

    fun payload(): String = entries.joinToString(";")
}

private class FieldReader(payload: String) {
    private val values: Map<String, String> = payload.split(';').mapNotNull { entry ->
        val index = entry.indexOf('=')
        if (index <= 0) null else entry.substring(0, index) to entry.substring(index + 1)
    }.toMap()

    fun bool(key: String, default: Boolean): Boolean = values[key]?.toBooleanStrictOrNull() ?: default

    fun float(key: String, default: Float): Float = values[key]?.toFloatOrNull() ?: default

    fun text(key: String, default: String): String = values[key]?.ifBlank { default } ?: default

    fun optionalText(key: String): String? = values[key]?.ifBlank { null }

    fun <T : Enum<T>> enum(key: String, default: T, constants: Array<T>): T =
        constants.firstOrNull { it.name == values[key] } ?: default
}

private fun WidgetBackgroundConfiguration.writeFields(fields: FieldWriter) {
    fields.add("bgFill", fill.name)
    fields.add("bgSolid", solidColorHex)
    fields.add("bgSolidOpacity", solidOpacity)
    fields.add("bgGradStart", gradientStartHex)
    fields.add("bgGradEnd", gradientEndHex)
    fields.add("bgGradAngle", gradientAngleDegrees)
    fields.add("bgOpacity", opacity)
    fields.add("bgShape", shape.name)
    fields.add("bgRadius", cornerRadiusDp)
    fields.add("bgBorderEnabled", borderEnabled)
    fields.add("bgBorderWidth", borderWidthDp)
    fields.add("bgBorderColor", borderColorHex)
    fields.add("bgBorderOpacity", borderOpacity)
    fields.add("bgHighlightEnabled", highlightEnabled)
    fields.add("bgHighlightColor", highlightColorHex)
    fields.add("bgHighlightOpacity", highlightOpacity)
}

private fun readBackgroundFields(reader: FieldReader): WidgetBackgroundConfiguration = WidgetBackgroundConfiguration(
    fill = reader.enum("bgFill", WidgetBackgroundFill.LINEAR_GRADIENT, WidgetBackgroundFill.entries.toTypedArray()),
    solidColorHex = reader.text("bgSolid", WidgetBackgroundConfiguration().solidColorHex),
    solidOpacity = reader.float("bgSolidOpacity", 1f),
    gradientStartHex = reader.text("bgGradStart", WidgetBackgroundConfiguration().gradientStartHex),
    gradientEndHex = reader.text("bgGradEnd", WidgetBackgroundConfiguration().gradientEndHex),
    gradientAngleDegrees = reader.float("bgGradAngle", 135f),
    opacity = reader.float("bgOpacity", 1f),
    shape = reader.enum("bgShape", WidgetBackgroundShape.ROUNDED, WidgetBackgroundShape.entries.toTypedArray()),
    cornerRadiusDp = reader.float("bgRadius", 24f),
    borderEnabled = reader.bool("bgBorderEnabled", true),
    borderWidthDp = reader.float("bgBorderWidth", 1f),
    borderColorHex = reader.text("bgBorderColor", WidgetBackgroundConfiguration().borderColorHex),
    borderOpacity = reader.float("bgBorderOpacity", 0.12f),
    highlightEnabled = reader.bool("bgHighlightEnabled", true),
    highlightColorHex = reader.text("bgHighlightColor", WidgetBackgroundConfiguration().highlightColorHex),
    highlightOpacity = reader.float("bgHighlightOpacity", 0.16f),
)

private fun WidgetActionConfiguration.writeFields(fields: FieldWriter) {
    fields.add("actType", type.name)
    fields.add("actPackage", packageName.orEmpty())
}

private fun readActionFields(reader: FieldReader): WidgetActionConfiguration = WidgetActionConfiguration(
    type = reader.enum("actType", WidgetActionType.NONE, WidgetActionType.entries.toTypedArray()),
    packageName = reader.optionalText("actPackage"),
)

private fun BatteryWidgetConfiguration.writeFields(fields: FieldWriter) {
    fields.add("layout", layout.name)
    fields.add("percentage", showPercentage)
    fields.add("charging", showChargingState)
    fields.add("plug", showPlugSource)
    fields.add("icon", showIcon)
    fields.add("textScale", textScale)
    fields.add("percentColor", percentColorHex)
    fields.add("statusColor", statusColorHex)
    fields.add("progressColor", progressColorHex)
    fields.add("lowColor", lowBatteryColorHex)
    fields.add("chargingColor", chargingColorHex)
    fields.add("padding", contentPaddingDp)
    fields.add("alignStart", alignStart)
    background.writeFields(fields)
    action.writeFields(fields)
}

private fun readBatteryFields(reader: FieldReader): BatteryWidgetConfiguration {
    val defaults = BatteryWidgetConfiguration()
    return BatteryWidgetConfiguration(
        layout = reader.enum("layout", defaults.layout, BatteryWidgetLayout.entries.toTypedArray()),
        showPercentage = reader.bool("percentage", true),
        showChargingState = reader.bool("charging", true),
        showPlugSource = reader.bool("plug", false),
        showIcon = reader.bool("icon", true),
        textScale = reader.float("textScale", 1f),
        percentColorHex = reader.text("percentColor", defaults.percentColorHex),
        statusColorHex = reader.text("statusColor", defaults.statusColorHex),
        progressColorHex = reader.text("progressColor", defaults.progressColorHex),
        lowBatteryColorHex = reader.text("lowColor", defaults.lowBatteryColorHex),
        chargingColorHex = reader.text("chargingColor", defaults.chargingColorHex),
        contentPaddingDp = reader.float("padding", 16f),
        alignStart = reader.bool("alignStart", false),
        background = readBackgroundFields(reader),
        action = readActionFields(reader),
    )
}

private fun WeatherWidgetConfiguration.writeFields(fields: FieldWriter) {
    fields.add("layout", layout.name)
    fields.add("temperature", showTemperature)
    fields.add("condition", showCondition)
    fields.add("location", showLocation)
    fields.add("highLow", showHighLow)
    fields.add("icon", showIcon)
    fields.add("units", units.name)
    fields.add("textScale", textScale)
    fields.add("tempColor", temperatureColorHex)
    fields.add("secondaryColor", secondaryColorHex)
    fields.add("iconTint", iconTintHex)
    fields.add("padding", contentPaddingDp)
    fields.add("alignStart", alignStart)
    fields.add("locationName", locationName.orEmpty())
    background.writeFields(fields)
    action.writeFields(fields)
}

private fun readWeatherFields(reader: FieldReader): WeatherWidgetConfiguration {
    val defaults = WeatherWidgetConfiguration()
    return WeatherWidgetConfiguration(
        layout = reader.enum("layout", defaults.layout, WeatherWidgetLayout.entries.toTypedArray()),
        showTemperature = reader.bool("temperature", true),
        showCondition = reader.bool("condition", true),
        showLocation = reader.bool("location", true),
        showHighLow = reader.bool("highLow", false),
        showIcon = reader.bool("icon", true),
        units = reader.enum("units", defaults.units, WeatherUnits.entries.toTypedArray()),
        textScale = reader.float("textScale", 1f),
        temperatureColorHex = reader.text("tempColor", defaults.temperatureColorHex),
        secondaryColorHex = reader.text("secondaryColor", defaults.secondaryColorHex),
        iconTintHex = reader.text("iconTint", defaults.iconTintHex),
        contentPaddingDp = reader.float("padding", 16f),
        alignStart = reader.bool("alignStart", false),
        locationName = reader.optionalText("locationName"),
        background = readBackgroundFields(reader),
        action = readActionFields(reader),
    )
}

private fun AnalogClockConfiguration.writeFields(fields: FieldWriter) {
    fields.add("hourMarkers", showHourMarkers)
    fields.add("minuteMarkers", showMinuteMarkers)
    fields.add("markerStyle", markerStyle.name)
    fields.add("markerColor", markerColorHex)
    fields.add("numberStyle", numberStyle.name)
    fields.add("numberColor", numberColorHex)
    fields.add("dialFill", dialFillEnabled)
    fields.add("dialColor", dialColorHex)
    fields.add("hourHand", showHourHand)
    fields.add("minuteHand", showMinuteHand)
    fields.add("secondHand", showSecondHand)
    fields.add("hourHandColor", hourHandColorHex)
    fields.add("minuteHandColor", minuteHandColorHex)
    fields.add("secondHandColor", secondHandColorHex)
    fields.add("capStyle", capStyle.name)
    fields.add("capColor", capColorHex)
    fields.add("dateEnabled", date.enabled)
    fields.add("datePlacement", date.placement.name)
    fields.add("dateAlign", date.alignment.name)
    fields.add("dateWeekday", date.weekdayStyle.name)
    fields.add("dateMonth", date.monthStyle.name)
    fields.add("dateYear", date.yearStyle.name)
    fields.add("datePreset", date.formatPreset.name)
    fields.add("dateFont", dateFontFamily.name)
    fields.add("dateColor", dateColorHex)
    fields.add("scale", contentScale)
    fields.add("padding", contentPaddingDp)
    background.writeFields(fields)
    action.writeFields(fields)
}

private fun readAnalogFields(reader: FieldReader): AnalogClockConfiguration {
    val defaults = AnalogClockConfiguration()
    val dateDefaults = ClockDateConfiguration()
    return AnalogClockConfiguration(
        showHourMarkers = reader.bool("hourMarkers", true),
        showMinuteMarkers = reader.bool("minuteMarkers", true),
        markerStyle = reader.enum("markerStyle", defaults.markerStyle, AnalogMarkerStyle.entries.toTypedArray()),
        markerColorHex = reader.text("markerColor", defaults.markerColorHex),
        numberStyle = reader.enum("numberStyle", defaults.numberStyle, AnalogNumberStyle.entries.toTypedArray()),
        numberColorHex = reader.text("numberColor", defaults.numberColorHex),
        dialFillEnabled = reader.bool("dialFill", true),
        dialColorHex = reader.text("dialColor", defaults.dialColorHex),
        showHourHand = reader.bool("hourHand", true),
        showMinuteHand = reader.bool("minuteHand", true),
        showSecondHand = reader.bool("secondHand", true),
        hourHandColorHex = reader.text("hourHandColor", defaults.hourHandColorHex),
        minuteHandColorHex = reader.text("minuteHandColor", defaults.minuteHandColorHex),
        secondHandColorHex = reader.text("secondHandColor", defaults.secondHandColorHex),
        capStyle = reader.enum("capStyle", defaults.capStyle, AnalogCapStyle.entries.toTypedArray()),
        capColorHex = reader.text("capColor", defaults.capColorHex),
        date = ClockDateConfiguration(
            enabled = reader.bool("dateEnabled", false),
            placement = reader.enum("datePlacement", dateDefaults.placement, ClockDatePlacement.entries.toTypedArray()),
            alignment = reader.enum("dateAlign", ClockDateAlignment.CENTER, ClockDateAlignment.entries.toTypedArray()),
            weekdayStyle = reader.enum("dateWeekday", ClockWeekdayStyle.HIDDEN, ClockWeekdayStyle.entries.toTypedArray()),
            monthStyle = reader.enum("dateMonth", ClockMonthStyle.SHORT_NAME, ClockMonthStyle.entries.toTypedArray()),
            yearStyle = reader.enum("dateYear", ClockYearStyle.HIDDEN, ClockYearStyle.entries.toTypedArray()),
            formatPreset = reader.enum("datePreset", ClockDateFormatPreset.DAY_MONTH, ClockDateFormatPreset.entries.toTypedArray()),
        ),
        dateFontFamily = reader.enum("dateFont", ClockFontFamily.SYSTEM, ClockFontFamily.entries.toTypedArray()),
        dateColorHex = reader.text("dateColor", defaults.dateColorHex),
        contentScale = reader.float("scale", 1f),
        contentPaddingDp = reader.float("padding", 14f),
        background = readBackgroundFields(reader),
        action = readActionFields(reader),
    )
}

private fun SearchWidgetConfiguration.writeFields(fields: FieldWriter) {
    fields.add("target", target.name)
    fields.add("customApp", customAppPackage.orEmpty())
    fields.add("hint", hintText)
    fields.add("searchIcon", showSearchIcon)
    fields.add("voiceIcon", showVoiceIcon)
    fields.add("hintColor", hintColorHex)
    fields.add("iconColor", iconColorHex)
    fields.add("textScale", textScale)
    fields.add("padding", contentPaddingDp)
    fields.add("alignStart", alignStart)
    fields.add("tapAction", tapAction.name)
    background.writeFields(fields)
    action.writeFields(fields)
}

private fun readSearchFields(reader: FieldReader): SearchWidgetConfiguration {
    val defaults = SearchWidgetConfiguration()
    return SearchWidgetConfiguration(
        target = reader.enum("target", defaults.target, SearchTarget.entries.toTypedArray()),
        customAppPackage = reader.optionalText("customApp"),
        hintText = reader.text("hint", defaults.hintText),
        showSearchIcon = reader.bool("searchIcon", true),
        showVoiceIcon = reader.bool("voiceIcon", true),
        hintColorHex = reader.text("hintColor", defaults.hintColorHex),
        iconColorHex = reader.text("iconColor", defaults.iconColorHex),
        textScale = reader.float("textScale", 1f),
        contentPaddingDp = reader.float("padding", 18f),
        alignStart = reader.bool("alignStart", true),
        tapAction = reader.enum("tapAction", defaults.tapAction, SearchTapAction.entries.toTypedArray()),
        background = readBackgroundFields(reader),
        action = readActionFields(reader),
    )
}

private fun MusicWidgetConfiguration.writeFields(fields: FieldWriter) {
    fields.add("playerPackage", selectedPlayerPackage.orEmpty())
    fields.add("playerLabel", selectedPlayerLabel.orEmpty())

    fields.add("albumArt", showAlbumArt)
    fields.add("trackTitle", showTrackTitle)
    fields.add("artist", showArtist)
    fields.add("progress", showProgress)
    fields.add("time", showTime)

    fields.add("previous", showPrevious)
    fields.add("playPause", showPlayPause)
    fields.add("next", showNext)

    fields.add("textScale", textScale)
    fields.add("padding", contentPaddingDp)

    fields.add("albumArtSize", albumArtSizeDp)
    fields.add("albumArtRadius", albumArtCornerRadiusDp)
    fields.add("controlScale", controlScale)
    fields.add("progressThickness", progressThicknessDp)

    fields.add("titleColor", titleColorHex)
    fields.add("artistColor", artistColorHex)
    fields.add("timeColor", timeColorHex)
    fields.add("controlColor", controlColorHex)
    fields.add("progressColor", progressColorHex)
    fields.add("progressTrackColor", progressTrackColorHex)

    background.writeFields(fields)
}

private fun readMusicFields(reader: FieldReader): MusicWidgetConfiguration {
    val defaults = MusicWidgetConfiguration()

    return MusicWidgetConfiguration(
        selectedPlayerPackage = reader.optionalText("playerPackage"),
        selectedPlayerLabel = reader.optionalText("playerLabel"),

        showAlbumArt = reader.bool("albumArt", defaults.showAlbumArt),
        showTrackTitle = reader.bool("trackTitle", defaults.showTrackTitle),
        showArtist = reader.bool("artist", defaults.showArtist),
        showProgress = reader.bool("progress", defaults.showProgress),
        showTime = reader.bool("time", defaults.showTime),

        showPrevious = reader.bool("previous", defaults.showPrevious),
        showPlayPause = reader.bool("playPause", defaults.showPlayPause),
        showNext = reader.bool("next", defaults.showNext),

        textScale = reader.float("textScale", defaults.textScale),
        contentPaddingDp = reader.float("padding", defaults.contentPaddingDp),

        albumArtSizeDp = reader.float(
            "albumArtSize",
            defaults.albumArtSizeDp,
        ),
        albumArtCornerRadiusDp = reader.float(
            "albumArtRadius",
            defaults.albumArtCornerRadiusDp,
        ),
        controlScale = reader.float(
            "controlScale",
            defaults.controlScale,
        ),
        progressThicknessDp = reader.float(
            "progressThickness",
            defaults.progressThicknessDp,
        ),

        titleColorHex = reader.text("titleColor", defaults.titleColorHex),
        artistColorHex = reader.text("artistColor", defaults.artistColorHex),
        timeColorHex = reader.text("timeColor", defaults.timeColorHex),
        controlColorHex = reader.text("controlColor", defaults.controlColorHex),
        progressColorHex = reader.text("progressColor", defaults.progressColorHex),
        progressTrackColorHex = reader.text(
            "progressTrackColor",
            defaults.progressTrackColorHex,
        ),

        background = readBackgroundFields(reader),
    )
}

private fun CalendarWidgetConfiguration.writeFields(fields: FieldWriter) {
    fields.add("weekday", showWeekday)
    fields.add("month", showMonth)
}

private fun readCalendarFields(reader: FieldReader): CalendarWidgetConfiguration =
    CalendarWidgetConfiguration(
        showWeekday = reader.bool("weekday", true),
        showMonth = reader.bool("month", true),
    )

class InMemoryTypedWidgetInstanceRepository : TypedWidgetInstanceRepository {
    private val instances = mutableMapOf<Int, TypedWidgetInstance>()

    override suspend fun get(appWidgetId: Int): TypedWidgetInstance? = instances[appWidgetId]

    override suspend fun save(instance: TypedWidgetInstance) {
        instances[instance.appWidgetId] = instance
    }

    override suspend fun delete(appWidgetId: Int) {
        instances.remove(appWidgetId)
    }
}

class DataStoreTypedWidgetInstanceRepository(
    private val preferencesDataStore: DataStore<Preferences>,
) : TypedWidgetInstanceRepository {
    override suspend fun get(appWidgetId: Int): TypedWidgetInstance? {
        val value = preferencesDataStore.data.first()[keyFor(appWidgetId)] ?: return null
        val instance = TypedWidgetInstanceCodec.decode(appWidgetId, value) ?: return null
        // Re-save when the stored form is outdated, including legacy preset product IDs.
        if (TypedWidgetInstanceCodec.encode(instance) != value) save(instance)
        return instance
    }

    override suspend fun save(instance: TypedWidgetInstance) {
        preferencesDataStore.edit { preferences ->
            preferences[keyFor(instance.appWidgetId)] = TypedWidgetInstanceCodec.encode(instance)
        }
    }

    override suspend fun delete(appWidgetId: Int) {
        preferencesDataStore.edit { preferences -> preferences.remove(keyFor(appWidgetId)) }
    }

    private fun keyFor(appWidgetId: Int): Preferences.Key<String> =
        stringPreferencesKey("non_clock_widget_instance_$appWidgetId")
}







