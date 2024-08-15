package com.android.messaging2.ui.screen.appsetting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.messaging.datamodel.data.ParticipantData
import com.android.messaging.ui.UIIntents
import com.android.messaging2.ui.theme.MessagingTheme


class PerSubscriptionSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra(
            UIIntents.UI_INTENT_EXTRA_PER_SUBSCRIPTION_SETTING_TITLE
        )
        val mSubId = if (intent != null) intent.getIntExtra(
            UIIntents.UI_INTENT_EXTRA_SUB_ID,
            ParticipantData.DEFAULT_SELF_SUB_ID
        ) else ParticipantData.DEFAULT_SELF_SUB_ID


        setContent {
            MessagingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PerSubscriptionSettingsScreen(
                        modifier = Modifier,
                        title = title ?: "",
                        mSubId = mSubId,
                        onBack = {
                            onBackPressed()
                        }
                    )
                }
            }
        }
    }
}

