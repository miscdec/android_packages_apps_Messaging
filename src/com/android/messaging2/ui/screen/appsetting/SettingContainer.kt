package com.android.messaging2.ui.screen.appsetting

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.android.messaging.R
import com.android.messaging.datamodel.data.SettingsData.SettingsItem

import com.android.messaging.ui.UIIntents
import com.android.messaging.util.Assert
import com.android.messaging.util.UiUtils.getActivity
import com.android.messaging2.ui.screen.AppScaffold
import com.android.messaging2.ui.screen.license.LicenseActivity

@Composable
fun SettingScreen(
    modifier: Modifier,
    settingsItemList: List<SettingsItem>,
    onBack: () -> Unit = {},
) {
    val ctx = LocalContext.current
    AppScaffold(
        modifier = modifier,
        title = stringResource(id = R.string.settings_activity_title),
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    ctx.startActivity(Intent(ctx, LicenseActivity::class.java))
                }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(id = R.string.menu_license)
                )
            }

        }
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            settingsItemList.map { item ->
                SettingsMenuLink(
//                icon = { Icon(imageVector = Icons.Default.Wifi, contentDescription = "Wifi") },
                    title = { Text(text = item.displayName) },
                    subtitle = {
                        item.displayDetail?.apply {
                            Text(text = this)
                        }
                    },
                    onClick = {
                        when (item.type) {
                            SettingsItem.TYPE_GENERAL_SETTINGS -> UIIntents.get()
                                .launchApplicationSettingsActivity(
                                    getActivity(ctx),
                                    false /* topLevel */
                                )

                            SettingsItem.TYPE_PER_SUBSCRIPTION_SETTINGS -> UIIntents.get()
                                .launchPerSubscriptionSettingsActivity(
                                    getActivity(ctx),
                                    item.subId,
                                    item.activityTitle
                                )

                            else -> Assert.fail("unrecognized setting type!")
                        }
                    },
                )
            }

        }
    }
}

