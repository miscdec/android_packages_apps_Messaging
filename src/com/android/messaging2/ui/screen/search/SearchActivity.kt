package com.android.messaging2.ui.screen.search

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.messaging.datamodel.BugleDatabaseOperations
import com.android.messaging.datamodel.DataModel
import com.android.messaging.datamodel.DatabaseHelper
import com.android.messaging.ui.UIIntents
import com.android.messaging2.ui.screen.AppScaffold
import com.android.messaging2.ui.theme.MessagingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mSearchStringParameter =
            intent.getStringExtra("query") ?: intent.getStringExtra("intent_extra_data_key")
                ?.trim { it <= ' ' }.toString()
        val mConversationId = intent.getStringExtra("conversation_id")
        Log.d("newSearch", "query = $mSearchStringParameter,conversationId = $mConversationId")
        setContent {
            MessagingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SearchScreen(
                        modifier = Modifier.fillMaxSize(),
                        title = "Search Result: $mSearchStringParameter",
                        searchKey = mSearchStringParameter,
                        conversationId = mConversationId,
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
fun SearchScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    searchKey: String,
    conversationId: String?,
) {
    val vm: SearchViewModel = viewModel()
    AppScaffold(
        title = title,
        onBack = onBack
    ) { paddingValues ->
        //请求数据
        val viewState = vm.uiState
        LaunchedEffect(key1 = true, block = {
            vm.newsChannel.send(
                SearchIntent.GetSearchResult(
                    conversationId = conversationId,
                    key = searchKey
                )
            )
        })
        val ctx = LocalContext.current
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {

            itemsIndexed(viewState.dataList) { index, item ->
                Card(
                    onClick = {
                        UIIntents.get().launchConversationActivityWithSearch(
                            ctx.applicationContext,
                            item.conversationId,
                            index
                        )
                    }
                ) {
                    Column {
                        Text(text = item.conversationName.toString())
                        Text(text = item.snipText.toString())
                    }
                }
            }

        }
    }
}

sealed class SearchIntent {
    //传递消息
    class GetSearchResult(val conversationId: String?, val key: String) : SearchIntent()
}

data class SearchState(
    val dataList: List<SearchBean> = emptyList()
)

class SearchViewModel : ViewModel() {

    //Channel信道，意图发送别ViewModel，
    val newsChannel = Channel<SearchIntent>(Channel.UNLIMITED)

    //状态管理
    var uiState by mutableStateOf(SearchState())

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            newsChannel.consumeAsFlow().collect {
                when (it) {
                    is SearchIntent.GetSearchResult -> search(
                        conversationId = it.conversationId,
                        searchKey = it.key
                    )
                }
            }
        }
    }


    private suspend fun startQuery(conversationId: String?, searchString: String): Cursor? {
        val dbWrapper = DataModel.get().getDatabase()
        dbWrapper.beginTransaction()
        searchString.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
            .replace("'", "''")
        val cursor = BugleDatabaseOperations.queryMessage(dbWrapper, conversationId, searchString)
        if (cursor != null) {
            dbWrapper.setTransactionSuccessful()
        }
        dbWrapper.endTransaction()
        return cursor
    }

    fun search(conversationId: String?, searchKey: String) {
        val arrayList = mutableListOf<SearchBean>()
        viewModelScope.launch(Dispatchers.IO) {
            val cursor = startQuery(conversationId, searchKey)
            cursor?.run {
                if (cursor.count >= 0) {
                    val dbWrapper = DataModel.get().getDatabase()
                    while (cursor.moveToNext()) {
                        val conversationId =
                            cursor.getLong(cursor.getColumnIndex("conversation_id"))
                        val threadId =
                            BugleDatabaseOperations.getThreadId(
                                dbWrapper,
                                conversationId.toString()
                            )
                        val textStr =
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.PartColumns.TEXT))
                        cursor.getString(cursor.getColumnIndex("content_type"))
                        val messageId = cursor.getInt(cursor.getColumnIndex("message_id"))
                        val searchViewBean = SearchBean(
                            conversationId = conversationId.toString(),
                            threadId = threadId,
                            conversationName = BugleDatabaseOperations.getExistingConversationName(
                                dbWrapper,
                                threadId
                            ),
                            messageId = messageId,
                            snipText = textStr
                        )
                        if (searchViewBean.snipText?.contains(searchKey) == true) {
                            arrayList.add(searchViewBean)
                        }
                    }
                    cursor.close()
                }
            }
            uiState = uiState.copy(dataList = arrayList)
        }
    }

}


data class SearchBean(
    var conversationId: String,
    var conversationName: String? = null,
    var messageId: Int = 0,
    var snipText: String? = null,
    var threadId: Long = 0
)


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    MessagingTheme {
        SearchScreen(
            "Android",
            onBack = {},
            searchKey = "mSearchStringParameter",
            conversationId = "",
        )
    }
}
