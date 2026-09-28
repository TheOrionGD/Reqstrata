package com.theoriongd.reqstrata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.navigation.AppNavigation
import com.theoriongd.reqstrata.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            val dynamicColors by viewModel.useDynamicColors.collectAsState()
            val colorPalette by viewModel.selectedColorPalette.collectAsState()

            MyApplicationTheme(
                darkTheme = isDark,
                dynamicColor = dynamicColors,
                colorPalette = colorPalette
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
