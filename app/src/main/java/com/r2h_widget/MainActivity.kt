package com.r2h_widget

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.ui.AppAction
import com.r2h_widget.ui.AppUiState
import com.r2h_widget.ui.AppViewModelFactory
import com.r2h_widget.ui.MainViewModel
import com.r2h_widget.ui.R2HWidgetApp
import com.r2h_widget.ui.WidgetDetailsViewModel
import com.r2h_widget.customization.CustomizationViewModel
import com.r2h_widget.ui.theme.R2HWidgetTheme
import com.r2h_widget.widget.pinning.AndroidWidgetPinningController
import com.r2h_widget.widget.clock.DigitalClockWidgetReceiver
import com.r2h_widget.widget.clock.EXTRA_EDIT_APP_WIDGET_ID
import com.r2h_widget.widget.clock.EXTRA_EDIT_PRODUCT_ID

class MainActivity : ComponentActivity() {
    private val dependencies by lazy { AppDependencies(applicationContext) }
    private val viewModelFactory by lazy { AppViewModelFactory(dependencies) }
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }
    private val detailsViewModel: WidgetDetailsViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[WidgetDetailsViewModel::class.java]
    }
    private val customizationViewModel: CustomizationViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[CustomizationViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val customizationState by customizationViewModel.uiState.collectAsStateWithLifecycle()
            R2HWidgetTheme(accentHex = customizationState.selections.accentHex) {
                R2HWidgetApp(
                    viewModel = mainViewModel,
                    detailsViewModel = detailsViewModel,
                    customizationViewModel = customizationViewModel,
                    pinningController = AndroidWidgetPinningController(this@MainActivity),
                )
            }
        }
        handleWidgetEditIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetEditIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        customizationViewModel.onAction(
            com.r2h_widget.customization.CustomizationAction.RefreshIconPackInstallation,
        )
    }

    private fun handleWidgetEditIntent(intent: Intent?) {
        val appWidgetId = intent?.getIntExtra(
            EXTRA_EDIT_APP_WIDGET_ID,
            android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: return
        if (appWidgetId == android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID) return
        val productId = intent.getStringExtra(EXTRA_EDIT_PRODUCT_ID)
            ?: DigitalClockWidgetReceiver.DEFAULT_PRODUCT_ID
        // Legacy pinned widgets still carry preset IDs such as clock-03; they edit
        // the canonical Digital Clock product with their configuration preserved.
        mainViewModel.onAction(
            AppAction.OpenWidgetDetails(com.r2h_widget.catalog.WidgetProductIds.canonicalize(productId), appWidgetId),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun R2HWidgetPreview() {
    R2HWidgetTheme {
        com.r2h_widget.ui.R2HWidgetAppContent(
            uiState = AppUiState(),
            onAction = { _: AppAction -> },
        )
    }
}
