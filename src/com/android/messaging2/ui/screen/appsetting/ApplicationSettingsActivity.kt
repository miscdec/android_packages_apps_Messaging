package com.android.messaging2.ui.screen.appsetting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.messaging.R
import com.android.messaging.ui.UIIntents
import com.android.messaging2.ui.theme.MessagingTheme

class ApplicationSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val topLevel = intent.getBooleanExtra(
            UIIntents.UI_INTENT_EXTRA_TOP_LEVEL_SETTINGS, false
        )
        if (topLevel) {
            title = getString(R.string.settings_activity_title)
        }

        setContent {
            MessagingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApplicationSettingsScreen(
                        modifier = Modifier,
                        title = title,
                        onBack = {
                            onBackPressed()
                        }
                    )
                }
            }
        }
    }
}

