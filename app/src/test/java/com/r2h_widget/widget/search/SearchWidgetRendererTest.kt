package com.r2h_widget.widget.search

import android.app.SearchManager
import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import com.r2h_widget.R
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SearchWidgetRendererTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun renderer_showsHintAndBothIconsByDefault() {
        val inflated = inflate(SearchWidgetConfiguration(hintText = "Look it up"))

        assertEquals("Look it up", inflated.findViewById<TextView>(R.id.search_hint).text.toString())
        assertEquals(View.VISIBLE, inflated.findViewById<View>(R.id.search_icon).visibility)
        assertEquals(View.VISIBLE, inflated.findViewById<View>(R.id.search_voice_icon).visibility)
    }

    @Test
    fun renderer_hidesIconsWhenDisabled() {
        val inflated = inflate(SearchWidgetConfiguration(showSearchIcon = false, showVoiceIcon = false))

        assertEquals(View.GONE, inflated.findViewById<View>(R.id.search_icon).visibility)
        assertEquals(View.GONE, inflated.findViewById<View>(R.id.search_voice_icon).visibility)
    }

    @Test
    fun blankHint_fallsBackToTheWordSearch() {
        val inflated = inflate(SearchWidgetConfiguration(hintText = "  "))

        assertEquals("Search", inflated.findViewById<TextView>(R.id.search_hint).text.toString())
    }

    @Test
    fun webTarget_resolvesASearchIntent() {
        val intent = SearchWidgetRenderer.searchIntentFor(context, SearchWidgetConfiguration(target = SearchTarget.WEB))

        assertNotNull(intent)
    }

    @Test
    fun assistantTarget_resolvesTheVoiceCommandIntent() {
        val intent = SearchWidgetRenderer.searchIntentFor(context, SearchWidgetConfiguration(target = SearchTarget.ASSISTANT))

        assertEquals(android.content.Intent.ACTION_VOICE_COMMAND, intent?.action)
    }

    @Test
    fun customAppWithoutPackage_fallsBackToTheAssistant() {
        val intent = SearchWidgetRenderer.searchIntentFor(
            context,
            SearchWidgetConfiguration(target = SearchTarget.CUSTOM_APP, customAppPackage = null),
        )

        assertEquals(android.content.Intent.ACTION_VOICE_COMMAND, intent?.action)
    }

    @Test
    fun customApp_resolvesItsLaunchIntentWhenInstalled() {
        val installedPackage = context.packageName
        val intent = SearchWidgetRenderer.searchIntentFor(
            context,
            SearchWidgetConfiguration(target = SearchTarget.CUSTOM_APP, customAppPackage = installedPackage),
        )

        assertNotNull(intent)
        assertEquals(installedPackage, intent?.`package`)
    }

    private fun inflate(configuration: SearchWidgetConfiguration): View {
        val views = SearchWidgetRenderer.render(context, configuration)
        val root = FrameLayout(context)
        return views.apply(context, root)
    }
}
