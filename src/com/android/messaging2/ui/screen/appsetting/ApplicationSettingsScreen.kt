package com.android.messaging2.ui.screen.appsetting

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState
import com.alorma.compose.settings.ui.SettingsSwitch
import com.android.messaging.R
import com.android.messaging.util.BuglePrefs
import com.android.messaging.util.PhoneUtils
import com.android.messaging2.ui.screen.AppScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationSettingsScreen(
    modifier: Modifier,
    title: CharSequence,
//    navController: NavController
    onBack: () -> Unit = {},
) {

    AppScaffold(
        title = title.toString(),
        onBack = onBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            val ctx = LocalContext.current
            val preference =
                ctx.getSharedPreferences(BuglePrefs.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            SettingsSwitch(
                state = rememberPreferenceBooleanSettingState(
                    key = stringResource(id = R.string.send_sound_pref_key),
                    defaultValue = booleanResource(id = R.bool.send_sound_pref_default),
                    preferences = preference
                ),
                title = { Text(stringResource(id = R.string.send_sound_pref_title)) },
            )
            SettingsSwitch(
                state = rememberPreferenceBooleanSettingState(
                    key = stringResource(id = R.string.show_emoticons_pref_key),
                    defaultValue = booleanResource(id = R.bool.show_emoticons_pref_default),
                    preferences = preference
                ),
                title = { Text(stringResource(id = R.string.show_emoticons_pref_title)) },
                subtitle = { Text(text = stringResource(id = R.string.show_emoticons_pref_summary)) }
            )
            SettingsSwitch(
                state = rememberPreferenceBooleanSettingState(
                    key = stringResource(id = R.string.swipe_right_deletes_conversation_key),
                    defaultValue = booleanResource(id = R.bool.swipe_right_deletes_conversation_default),
                    preferences = preference
                ),
                title = { Text(stringResource(id = R.string.swipe_to_delete_conversation_pref_title)) },
                subtitle = { Text(text = stringResource(id = R.string.swipe_to_delete_conversation_pref_summary)) }
            )
            SettingsSwitch(
                state = rememberPreferenceBooleanSettingState(
                    key = stringResource(id = R.string.send_sound_pref_key),
                    defaultValue = booleanResource(id = R.bool.send_sound_pref_default),
                    preferences = preference
                ),
                title = { Text(stringResource(id = R.string.send_sound_pref_title)) },
            )
        }
    }

}
