package com.android.messaging2.ui.screen.conversationlist

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import com.android.messaging.R
import com.android.messaging.datamodel.data.ConversationListData
import com.android.messaging.datamodel.data.ConversationListItemData
import com.android.messaging.ui.UIIntents
import com.android.messaging.ui.conversationlist.ConversationListItemView
import com.android.messaging.util.Trace
import com.android.messaging2.ui.theme.MessagingTheme

class ConversationListActivity : AppCompatActivity(),
    ComposeConversationListFragment.ConversationListFragmentHost {

    var isSElectionMode = false

    private val mSelectedConversations: ArrayMap<String, SelectedConversation>? = null

    protected var mConversationListFragment: ComposeConversationListFragment? = null

    override fun onAttachFragment(fragment: Fragment) {
        Trace.beginSection("AbstractConversationListActivity.onAttachFragment")
        // Fragment could be debug dialog
        if (fragment is ComposeConversationListFragment) {
            mConversationListFragment = fragment
            mConversationListFragment!!.setHost(this)
        }
        Trace.endSection()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentManager = this.supportFragmentManager
        setContent {
            MessagingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isSElectionMode by remember { mutableStateOf(isSElectionMode) }
                    ConversationListScreen(
                        modifier = Modifier,
                        title = stringResource(id = R.string.app_name),
                        fragmentManager = fragmentManager,
                        action = {
                            val ctx = LocalContext.current
                            if (!isSElectionMode) {
                                IconButton(onClick = {

                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = stringResource(id = R.string.action_start_search)
                                    )
                                }
                                IconButton(onClick = {

                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(id = R.string.action_start_search)
                                    )
                                }
                                var expanded by remember { mutableStateOf(false) }

                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.action_settings)) },
                                        onClick = { UIIntents.get().launchSettingsActivity(ctx) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.action_menu_show_archived)) },
                                        onClick = {
                                            UIIntents.get().launchArchivedConversationsActivity(ctx)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.blocked_contacts_title)) },
                                        onClick = {
                                            UIIntents.get().launchBlockedParticipantsActivity(ctx)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.action_debug_options)) },
                                        onClick = {
                                            UIIntents.get().launchDebugMmsConfigActivity(ctx)
                                        }
                                    )

                                }
                            } else {
                                IconButton(onClick = {
                                    exitMultiSelectState()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = stringResource(id = R.string.action_start_search)
                                    )
                                }
                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = stringResource(id = R.string.action_start_search)
                                    )
                                }
                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(id = R.string.action_start_search)
                                    )
                                }
                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = stringResource(id = R.string.action_start_search)
                                    )
                                }
                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = stringResource(id = R.string.action_start_search)
                                    )
                                }
                            }

                        },
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    protected fun startMultiSelectActionMode() {
        isSElectionMode = true
        mConversationListFragment!!.updateUi()
    }

    protected fun exitMultiSelectState() {
        isSElectionMode = false
        mConversationListFragment!!.updateUi()
    }

    override fun onConversationClick(
        listData: ConversationListData?,
        conversationListItemData: ConversationListItemData?,
        isLongClick: Boolean,
        conversationView: ConversationListItemView?
    ) {
        if (isLongClick && !isInConversationListSelectMode()) {
            isSElectionMode = true
        }

        if (isInConversationListSelectMode()) {

            listData!!.blockedParticipants
            val id = conversationListItemData!!.conversationId
            if (mSelectedConversations?.containsKey(id) == true) {
                mSelectedConversations.remove(id)
            } else {
                mSelectedConversations?.put(id, SelectedConversation(conversationListItemData))
            }
            mConversationListFragment!!.updateUi()
//            mConversationListFragment.updateUi()
        } else {
            val conversationId = conversationListItemData!!.conversationId
            val sceneTransitionAnimationOptions: Bundle? = null
            val hasCustomTransitions = false
            UIIntents.get().launchConversationActivity(
                this, conversationId, null,
                sceneTransitionAnimationOptions,
                hasCustomTransitions
            )

        }

    }

    private fun isInConversationListSelectMode(): Boolean {
        return isSElectionMode
    }

    override fun onCreateConversationClick() {
        UIIntents.get().launchCreateNewConversationActivity(this, null)
    }

    override fun isConversationSelected(conversationId: String?): Boolean {
        return isInConversationListSelectMode() && mSelectedConversations?.containsKey(
            conversationId
        ) == true
    }

    override fun isSwipeAnimatable(): Boolean {
        return !isInConversationListSelectMode()
    }

    override fun isSelectionMode(): Boolean {
        return isSElectionMode
    }

}


internal class SelectedConversation(data: ConversationListItemData) {
    val conversationId: String
    val timestamp: Long
    val icon: String
    val otherParticipantNormalizedDestination: String
    val participantLookupKey: CharSequence
    val isGroup: Boolean
    val isArchived: Boolean

    init {
        conversationId = data.conversationId
        timestamp = data.timestamp
        icon = data.icon
        otherParticipantNormalizedDestination = data.otherParticipantNormalizedDestination
        participantLookupKey = data.participantLookupKey
        isGroup = data.isGroup
        isArchived = data.isArchived
    }
}


