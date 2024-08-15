package com.android.messaging2.ui.screen.conversationlist

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentManager
import com.android.messaging.R
import com.android.messaging.databinding.ConversationListFragmentComposeBinding
import com.android.messaging.ui.UIIntents
import com.android.messaging2.ui.screen.AppScaffold

@Composable
fun ConversationListScreen(
    modifier: Modifier,
    title: String,
    fragmentManager: FragmentManager,
    onBack: () -> Unit,
    action: @Composable (RowScope.() -> Unit),
) {
    val ctx = LocalContext.current
    AppScaffold(
        modifier = modifier,
        title = title,
        onBack = onBack,
        navigationIcon = {},
        actions = action,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.start_new_conversation)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.start_new_conversation)
                    )
                },
                onClick = { UIIntents.get().launchCreateNewConversationActivity(ctx, null) }
            )
        }
    ) {
        ConversationListContainer(
            modifier = modifier
                .padding(paddingValues = it)
                .fillMaxSize(),
            fragmentManager = fragmentManager,
        )

    }

}

@Composable
fun ConversationListContainer(
    modifier: Modifier = Modifier,
    fragmentManager: FragmentManager,
) {
    AndroidViewBinding(
        modifier = modifier.fillMaxSize(),
        factory = ConversationListFragmentComposeBinding::inflate
    ) {
        fragmentManager
            .beginTransaction()
            .add(R.id.conversation_list_fragment_container_view, ComposeConversationListFragment())
            .commit()
    }

}


