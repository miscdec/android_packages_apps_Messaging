package com.android.messaging2.ui.screen.appsetting

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsSwitch
import com.android.messaging.R
import com.android.messaging.ui.UIIntents
import com.android.messaging.util.BuglePrefs
import com.android.messaging2.ui.screen.AppScaffold
import com.android.messaging2.ui.theme.MessagingTheme
import kotlinx.coroutines.launch

class CaptchaKeywordsSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val topLevel = intent.getBooleanExtra(
            UIIntents.UI_INTENT_EXTRA_TOP_LEVEL_SETTINGS, false
        )
        if (topLevel) {
            title = getString(R.string.captcha_keywords_title)
        }

        setContent {
            MessagingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CaptchaKeywordsSettingsScreen(
                        modifier = Modifier,
                        title = title.toString(),
                        onBack = { onBackPressed() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CaptchaKeywordsSettingsScreen(modifier: Modifier, title: String, onBack: () -> Unit) {
    val viewModel: CaptchaKeywordsSettingsViewModel = viewModel()

    val ctx = LocalContext.current
    val uiState = viewModel.uiState
    LaunchedEffect(true) {
        viewModel.captchaChannel.send(
            CaptchaIntent.GetCaptchaKeywordsResult(
                ctx = ctx,
                spName = BuglePrefs.SHARED_PREFERENCES_NAME,
                defaultValue = ctx.getString(R.string.captcha_keywords_default)
                    .split("\n".toRegex())
                    .dropLastWhile { it.isEmpty() }.toHashSet(),
                key = ctx.getString(R.string.captcha_keywords_key)
            )
        )
    }


//    val captchaKeywordsUtils = CaptchaKeywordsUtils(ctx)
//    val subPrefs = captchaKeywordsUtils.mPrefs
//    val preference = ctx.getSharedPreferences(subPrefs.sharedPreferencesName, MODE_PRIVATE)

    val preference =
        ctx.getSharedPreferences(BuglePrefs.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    val scope = rememberCoroutineScope()


    var showAddDialog by remember { mutableStateOf(false) }
    AppScaffold(modifier = modifier, title = title, onBack = onBack, floatingActionButton = {
        ExtendedFloatingActionButton(onClick = {
            showAddDialog = true
        }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.add_captcha_keyword)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = stringResource(id = R.string.add_captcha_keyword))
        }
    }) { paddingValues: PaddingValues ->
        if (showAddDialog) {
            AddKeywordDialog(onDismiss = {
                showAddDialog = false
            }, onConfirmClicked = { keywords ->
                if (keywords.isNotBlank()) {
                    scope.launch {
                        viewModel.captchaChannel.send(
                            CaptchaIntent.AddKeyword(
                                keywords, ctx = ctx,
                                spName = BuglePrefs.SHARED_PREFERENCES_NAME,
                                defaultValue = ctx.getString(R.string.captcha_keywords_default)
                                    .split("\n".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toHashSet(),
                                key = ctx.getString(R.string.captcha_keywords_key)
                            )
                        )
                    }
                }
                showAddDialog = false
            })
        }
        Surface(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
        ) {
            SettingsGroup(
                modifier = Modifier.fillMaxWidth()

            ) {
                SettingsSwitch(state = rememberPreferenceBooleanSettingState(
                    key = stringResource(id = R.string.captcha_detector_key),
                    defaultValue = booleanResource(id = R.bool.captcha_detector_default),
                    preferences = preference
                ),
                    title = { Text(stringResource(id = R.string.captcha_enable_title)) },
                    subtitle = { Text(text = stringResource(id = R.string.captcha_enable_summary)) })

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.captcha_keywords_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.captcha_keywords_header),
                    style = MaterialTheme.typography.labelMedium
                )
                CheckList(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    keywords = uiState.dataList,
                    onRemove = {
                        scope.launch {
                            viewModel.captchaChannel.send(
                                element = CaptchaIntent.DeleteKeyword(
                                    keyword = it,
                                    ctx = ctx,
                                    spName = BuglePrefs.SHARED_PREFERENCES_NAME,
                                    defaultValue = ctx.getString(R.string.captcha_keywords_default)
                                        .split("\n".toRegex())
                                        .dropLastWhile { it.isEmpty() }.toHashSet(),
                                    key = ctx.getString(R.string.captcha_keywords_key)
                                )
                            )
                        }
                    }
                )
            }
        }


    }
}

@Composable
private fun AddKeywordDialog(
    onDismiss: () -> Unit,
    onConfirmClicked: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(title = {
        Text(
            text = stringResource(id = R.string.add_captcha_keyword),
            style = MaterialTheme.typography.titleMedium
        )
    }, text = {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(id = R.string.captcha_dialog_tip))
                Spacer(modifier = Modifier.size(8.dp))
                TextField(value = text, onValueChange = {
                    text = it
                }, isError = text.isEmpty(), label = {
                    if (text.isEmpty()) Text(text = stringResource(id = R.string.captcha_keyword_add_empty))
                }, trailingIcon = {
                    if (text.isEmpty()) Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = stringResource(id = R.string.captcha_keyword_add_empty)
                    )
                }

                )
            }
        }

    }, onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            onClick = { onConfirmClicked(text) },
            enabled = text.isNotEmpty() and text.isNotBlank()
        ) {
            Text(text = stringResource(id = android.R.string.ok))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(id = android.R.string.cancel))
        }
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemLayout(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    keywords: State<KeywordItem>,
    onCheckedChange: (Boolean) -> Unit,
    onRemove: (String) -> Unit = {},
    showCheck: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLangClick: () -> Unit = {}

) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .combinedClickable(
                enabled = true,
                onClick = {
                    if (showCheck) onCheckedChange.invoke(!keywords.value.isSelected)
                    else onClick?.let { it() }
                },
                onLongClick = onLangClick
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = keywords.value.keyword,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )


        if (showCheck) {

            Checkbox(
                modifier = Modifier,
                checked = keywords.value.isSelected,
                interactionSource = interactionSource,
                onCheckedChange = {
                    onCheckedChange.invoke(it)
                }
            )

        } else {
            IconButton(onClick = {
                onRemove(keywords.value.keyword)
            }) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null)
            }
        }

    }
}

@Composable
fun CheckList(
    modifier: Modifier = Modifier,
    keywords: List<String>,
    onRemove: (String) -> Unit
) {
    val showCheck = remember { mutableStateOf(false) }

    var links by remember {
        mutableStateOf(
            keywords.map { KeywordItem(keyword = it, isSelected = false) })
    }
    LazyColumn(modifier = modifier) {
        itemsIndexed(links) { index, item ->
            val mutableKeywordItemState = rememberUpdatedState(newValue = item)
            ItemLayout(
                keywords = mutableKeywordItemState,
                showCheck = showCheck.value,
                onCheckedChange = { state ->
                    val list = links.toMutableList()
                    list[index] = item.copy(isSelected = state)
                    links = list
                },
                onRemove = onRemove
            ) {
                showCheck.value = true
            }
        }
        item {
            Row {
                if (!showCheck.value) {
                    Button(onClick = { showCheck.value = !showCheck.value }) {
                        Text(text = "多选")
                    }
                } else {
                    Button(onClick = {
                        showCheck.value = !showCheck.value
                    }) {
                        Text(text = "单选")
                    }
                    Button(onClick = {
                        val list = links.toMutableList().map {
                            it.copy(isSelected = true)
                        }
                        links = list
                    }) {
                        Text(text = "全选")
                    }
                    Button(onClick = {
                        val list = links.toMutableList().map {
                            it.copy(isSelected = false)
                        }
                        links = list
                    }) {
                        Text(text = "全不选")
                    }
                    Button(onClick = {
                        links
                            .filter { it.isSelected }
                            .forEach {
                                onRemove(it.keyword)
                            }

                    }) {
                        Text(text = "删除")
                    }
                }

            }
        }
    }

}


data class KeywordItem(
    val keyword: String,
    var isSelected: Boolean
)

@Preview
@Composable
fun Preview() {
    Surface {
        CheckList(keywords = (1..20).map { it.toString() }.toList(), onRemove = {})
    }
}

