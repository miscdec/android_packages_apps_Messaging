package com.android.messaging2.ui.screen.appsetting

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.mms.MmsManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.android.messaging.Factory
import com.android.messaging.R
import com.android.messaging.sms.ApnDatabase
import com.android.messaging.sms.MmsConfig
import com.android.messaging.sms.MmsUtils
import com.android.messaging.ui.UIIntents
import com.android.messaging.util.LogUtil
import com.android.messaging.util.PhoneUtils
import com.android.messaging2.ui.screen.AppScaffold

@Composable
fun PerSubscriptionSettingsScreen(
    modifier: Modifier,
    title: String,
    mSubId: Int,
    onBack: () -> Unit
) {

    AppScaffold(
        modifier = modifier,
        title = title,
        onBack = onBack,
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
        ) {
            val ctx = LocalContext.current
            val subPrefs = Factory.get().getSubscriptionPrefs(mSubId)
            val preference =
                ctx.getSharedPreferences(subPrefs.sharedPreferencesName, Context.MODE_PRIVATE)
            val enabledSmsSetting by remember { mutableStateOf(PhoneUtils.getDefault().isDefaultSmsApp) }
            SettingsGroup(
                title = {
                    Text(stringResource(id = R.string.mms_messaging_category_pref_title))
                }
            ) {
                SettingsSwitch(
                    enabled = enabledSmsSetting,
                    state = rememberPreferenceBooleanSettingState(
                        key = stringResource(id = R.string.auto_retrieve_mms_pref_key),
                        defaultValue = booleanResource(id = R.bool.auto_retrieve_mms_pref_default),
                        preferences = preference
                    ),
                    title = { Text(stringResource(id = R.string.auto_retrieve_mms_pref_title)) },
                    subtitle = { Text(text = stringResource(id = R.string.auto_retrieve_mms_pref_summary)) }
                )
                SettingsSwitch(
                    enabled = enabledSmsSetting,
                    state = rememberPreferenceBooleanSettingState(
                        key = stringResource(id = R.string.auto_retrieve_mms_when_roaming_pref_key),
                        defaultValue = booleanResource(id = R.bool.auto_retrieve_mms_when_roaming_pref_default),
                        preferences = preference
                    ),
                    title = { Text(stringResource(id = R.string.auto_retrieve_mms_when_roaming_pref_title)) },
                    subtitle = { Text(text = stringResource(id = R.string.auto_retrieve_mms_when_roaming_pref_summary)) }
                )

            }
            SettingsGroup(
                title = {
                    Text(stringResource(id = R.string.advanced_category_pref_title))
                }
            ) {
                if (!MmsConfig.get(mSubId).smsDeliveryReportsEnabled) {
                    SettingsSwitch(
                        enabled = enabledSmsSetting,
                        state = rememberPreferenceBooleanSettingState(
                            key = stringResource(id = R.string.delivery_reports_pref_key),
                            defaultValue = booleanResource(id = R.bool.delivery_reports_pref_default),
                            preferences = preference
                        ),
                        title = { Text(stringResource(id = R.string.delivery_reports_pref_title)) },
                        subtitle = { Text(text = stringResource(id = R.string.delivery_reports_pref_summary)) }
                    )
                }

                SettingsMenuLink(
                    enabled = isCellBroadcastAppLinkEnabled(mSubId, ctx),
                    title = { Text(text = stringResource(id = R.string.wireless_alerts_title)) }
                ) {
                    try {
                        ctx.startActivity(UIIntents.get().wirelessAlertsIntent)
                    } catch (e: ActivityNotFoundException) {
                        // Handle so we shouldn't crash if the wireless alerts
                        // implementation is broken.
                        LogUtil.e(
                            LogUtil.BUGLE_TAG,
                            "Failed to launch wireless alerts activity", e
                        )
                        Toast.makeText(
                            ctx,
                            "Failed to launch wireless alerts activity:${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                SettingsMenuLink(
                    enabled = !(!MmsManager.shouldUseLegacyMms() || MmsUtils.useSystemApnTable() && !ApnDatabase.doesDatabaseExist()),
                    title = { Text(text = stringResource(id = R.string.sms_apns_title)) }
                ) {
                    try {
                        ctx.startActivity(
                            UIIntents.get().getApnSettingsIntent(ctx, mSubId)
                        )
                    } catch (e: ActivityNotFoundException) {
                        // Handle so we shouldn't crash if the wireless alerts
                        // implementation is broken.
                        LogUtil.e(
                            LogUtil.BUGLE_TAG,
                            "Failed to launch wireless alerts activity", e
                        )
                        Toast.makeText(
                            ctx,
                            "Failed to launch wireless alerts activity:${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    }
}

fun isCellBroadcastAppLinkEnabled(mSubId: Int, context: Context): Boolean {
    if (!MmsConfig.get(mSubId).showCellBroadcast) {
        return false
    }
    try {
        val pm: PackageManager = context.packageManager
        return (pm.getApplicationEnabledSetting(UIIntents.CMAS_COMPONENT)
            != PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    } catch (ignored: IllegalArgumentException) {
        // CMAS app not installed.
    }
    return false
}
