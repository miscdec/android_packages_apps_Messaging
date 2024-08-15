package com.android.messaging2.ui.screen.license

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.android.messaging.R
import com.android.messaging2.ui.theme.MessagingTheme
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer


class LicenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MessagingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LicenseScreen(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(id = R.string.menu_license),
                        onBack = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    Dialog(
        onDismissRequest = onBack
    ) {
        LibrariesContainer(
            modifier = modifier
                .fillMaxSize(),
            header = {
                item {
                    TopAppBar(title = {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                    })
                }
            }

        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MessagingTheme {
        LicenseScreen("Android", onBack = {})
    }
}
