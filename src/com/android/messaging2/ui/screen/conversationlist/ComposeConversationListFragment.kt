package com.android.messaging2.ui.screen.conversationlist

import android.content.Context
import android.database.Cursor
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.messaging.datamodel.DataModel
import com.android.messaging.datamodel.binding.Binding
import com.android.messaging.datamodel.binding.BindingBase
import com.android.messaging.datamodel.data.ConversationListData
import com.android.messaging.datamodel.data.ConversationListData.ConversationListDataListener
import com.android.messaging.datamodel.data.ConversationListItemData
import com.android.messaging.ui.SnackBarInteraction
import com.android.messaging.ui.UIIntents
import com.android.messaging.ui.conversationlist.ConversationListAdapter
import com.android.messaging.ui.conversationlist.ConversationListItemView
import com.android.messaging.ui.conversationlist.ConversationListSwipeHelper
import com.android.messaging.util.Assert
import com.android.messaging.util.ImeUtil
import com.android.messaging.util.LogUtil
import com.android.messaging2.ui.theme.MessagingTheme
import com.google.common.annotations.VisibleForTesting

//class ComposeConversationListFragment : Fragment() ,
//    ConversationListData.ConversationListDataListener,
//    ConversationListItemView.HostInterface {
//
//    private lateinit var mHost: ConversationListFragmentHost
//
//    private lateinit var mRecyclerView: RecyclerView
//
//    private var mArchiveMode = false
//    private var mBlockedAvailable = false
//    private var mForwardMessageMode = false
//
//    private lateinit var mAdapter: ConversationListAdapter
//
//    @VisibleForTesting
//    val mListBinding = BindingBase.createBinding<ConversationListData>(this)
//
//    private var conversationListItemData  = arrayListOf<ConversationListItemData>()
//
//    /**
//     * {@inheritDoc} from Fragment
//     */
//    override fun onCreate(bundle: Bundle?) {
//        super.onCreate(bundle)
//        mListBinding.getData().init(LoaderManager.getInstance(this), mListBinding)
//        mAdapter = ConversationListAdapter(activity, null, this)
//    }
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        mListBinding.getData().setScrolledToNewestConversation(false)
//
//        return ComposeView(requireContext()).apply {
//            setContent {
//                MessagingTheme {
//                    AndroidView(factory = {
//                        mRecyclerView = RecyclerView(requireContext()).apply {
//                            val manager: LinearLayoutManager =
//                                object : LinearLayoutManager(activity) {
//                                    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
//                                        return RecyclerView.LayoutParams(
//                                            ViewGroup.LayoutParams.MATCH_PARENT,
//                                            ViewGroup.LayoutParams.WRAP_CONTENT
//                                        )
//                                    }
//                                }
//                            setLayoutManager(manager)
//                            setHasFixedSize(true)
//                            setAdapter(mAdapter)
//                            setOnScrollListener(object : RecyclerView.OnScrollListener() {
//                                var mCurrentState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE
//                                override fun onScrolled(
//                                    recyclerView: RecyclerView,
//                                    dx: Int,
//                                    dy: Int
//                                ) {
//                                    if (mCurrentState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
//                                        || mCurrentState == AbsListView.OnScrollListener.SCROLL_STATE_FLING
//                                    ) {
//                                        ImeUtil.get()
//                                            .hideImeKeyboard(requireActivity(), recyclerView)
//                                    }
//                                    mListBinding.getData().setScrolledToNewestConversation(false)
//
//                                }
//
//                                override fun onScrollStateChanged(
//                                    recyclerView: RecyclerView,
//                                    newState: Int
//                                ) {
//                                    mCurrentState = newState
//                                }
//                            })
//                            addOnItemTouchListener(ConversationListSwipeHelper(this))
//                        }
//                        mRecyclerView
//                    })
//                }
//            }
//        }
//    }
//
//    /**
//     * Call this immediately after attaching the fragment
//     */
//    fun setHost(host: ConversationListFragmentHost) {
//        Assert.isNull(mHost)
//        mHost = host
//    }
//
//    @Composable
//    private fun ConversationListItem(itemData: ConversationListItemData) {
//        Column(Modifier) {
//            Text(text = itemData.icon)
//            Text(text = itemData.name)
//            Text(text = itemData.formattedTimestamp)
//            Text(text = itemData.draftSnippetText)
//        }
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (VERBOSE) {
//            LogUtil.v(LogUtil.BUGLE_TAG, "Attaching List")
//        }
//        val arguments = arguments
//        if (arguments != null) {
//            mArchiveMode = arguments.getBoolean(BUNDLE_ARCHIVED_MODE, false)
//            mForwardMessageMode = arguments.getBoolean(BUNDLE_FORWARD_MESSAGE_MODE, false)
//        }
//        mListBinding.bind(DataModel.get().createConversationListData(activity, this, mArchiveMode))
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateUi()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mListBinding.unbind()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mListBinding.getData().setScrolledToNewestConversation(false)
//    }
//
//
//    companion object {
//        private const val BUNDLE_ARCHIVED_MODE = "archived_mode"
//        private const val BUNDLE_FORWARD_MESSAGE_MODE = "forward_message_mode"
//        private const val VERBOSE = false
//
//        // Saved Instance State Data - only for temporal data which is nice to maintain but not
//        // critical for correctness.
//        private const val SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY = "conversationListViewState"
//
//        @JvmStatic
//        fun createArchivedConversationListFragment(): ComposeConversationListFragment {
//            return createConversationListFragment(BUNDLE_ARCHIVED_MODE)
//        }
//
//        @JvmStatic
//        fun createForwardMessageConversationListFragment(): ComposeConversationListFragment {
//            return createConversationListFragment(BUNDLE_FORWARD_MESSAGE_MODE)
//        }
//
//        fun createConversationListFragment(modeKeyName: String?): ComposeConversationListFragment {
//            val fragment = ComposeConversationListFragment()
//            val bundle = Bundle()
//            bundle.putBoolean(modeKeyName, true)
//            fragment.setArguments(bundle)
//            return fragment
//        }
//    }
//
//    override fun onConversationListCursorUpdated(
//        data: ConversationListData,
//        cursor: Cursor
//    ) {
//        mListBinding.ensureBound(data)
//        val oldCursor = mAdapter.swapCursor(cursor)
//    }
//
//    fun updateUi() {
//        mAdapter.notifyDataSetChanged()
//    }
//
//    override fun setBlockedParticipantsAvailable(blockedAvailable: Boolean) {
//        mBlockedAvailable = blockedAvailable
//    }
//
//    override fun onConversationListCursorUpdated(
//        data: ConversationListData?,
//        cursor: Cursor?
//    ) {
//        mListBinding.ensureBound(data)
//        val oldCursor = mAdapter.swapCursor(cursor)
//
//    }
//
//
//
//    override fun onConversationClicked(
//        conversationListItemData: ConversationListItemData?,
//        isLongClick: Boolean,
//        conversationView: ConversationListItemView?
//    ) {
//        val listData = mListBinding.getData()
//        mHost.onConversationClick(
//            listData, conversationListItemData, isLongClick,
//            conversationView
//        )
//    }
//
//    /**
//     * {@inheritDoc} from ConversationListItemView.HostInterface
//     */
//    override fun isConversationSelected(conversationId: String?): Boolean {
//        return mHost.isConversationSelected(conversationId)
//    }
//
//    override fun isSwipeAnimatable(): Boolean {
//        return mHost.isSwipeAnimatable()
//    }
//
//    override fun getSnackBarInteractions(): List<SnackBarInteraction> {
//        val interactions: MutableList<SnackBarInteraction> = ArrayList(1)
////        val fabInteraction: SnackBarInteraction =
////            BasicSnackBarInteraction(mStartNewConversationButton)
////        interactions.add(fabInteraction)
//        return interactions
//    }
//
//    override fun startFullScreenPhotoViewer(
//        initialPhoto: Uri?, initialPhotoBounds: Rect?, photosUri: Uri?
//    ) {
//        UIIntents.get().launchFullScreenPhotoViewer(
//            activity, initialPhoto, initialPhotoBounds, photosUri
//        )
//    }
//
//    override fun startFullScreenVideoViewer(videoUri: Uri?) {
//        UIIntents.get().launchFullScreenVideoViewer(activity, videoUri)
//    }
//
//    override fun isSelectionMode(): Boolean {
//        return mHost != null && mHost.isSelectionMode()
//    }
//}

/**
 * Shows a list of conversations.
 */
class ComposeConversationListFragment : Fragment(), ConversationListDataListener,
    ConversationListItemView.HostInterface {

    private var mShowBlockedMenuItem: MenuItem? = null
    private var mArchiveMode: Boolean = false
    private var mBlockedAvailable: Boolean = false
    private var mForwardMessageMode: Boolean = false

    interface ConversationListFragmentHost {
        fun onConversationClick(
            listData: ConversationListData?,
            conversationListItemData: ConversationListItemData?,
            isLongClick: Boolean,
            conversationView: ConversationListItemView?
        )

        fun onCreateConversationClick()
        fun isConversationSelected(conversationId: String?): Boolean
        fun isSwipeAnimatable(): Boolean
        fun isSelectionMode(): Boolean

        fun hasWindowFocus(): Boolean
    }

    private var mHost: ConversationListFragmentHost? = null


    private lateinit var recyclerView: RecyclerView

    //    private lateinit var mStartNewConversationButton: ExtendedFloatingActionButton
//    private lateinit var mEmptyListMessageView: ListEmptyView
    private lateinit var mAdapter: ConversationListAdapter
    private var mListState: Parcelable? = null

    @VisibleForTesting
    val mListBinding: Binding<ConversationListData> = BindingBase.createBinding(this)

    /**
     * {@inheritDoc} from Fragment
     */
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        mListBinding.getData().init(LoaderManager.getInstance(this), mListBinding)
        mAdapter = ConversationListAdapter(activity, null, this)
    }

    override fun onResume() {
        super.onResume()
        Assert.notNull(mHost)
        setScrolledToNewestConversationIfNeeded()
        updateUi()
    }

    private fun setScrolledToNewestConversationIfNeeded() {
        if ((!mArchiveMode
                && !mForwardMessageMode
                && isScrolledToFirstConversation
                && mHost!!.hasWindowFocus())
        ) {
            mListBinding.getData().setScrolledToNewestConversation(true)
        }
    }

    private val isScrolledToFirstConversation: Boolean
        get() {
            val firstItemPosition: Int = (recyclerView.layoutManager as LinearLayoutManager?)
                ?.findFirstCompletelyVisibleItemPosition()!!
            return firstItemPosition == 0
        }

    /**
     * {@inheritDoc} from Fragment
     */
    override fun onDestroy() {
        super.onDestroy()
        mListBinding.unbind()
        mHost = null
    }

    /**
     * {@inheritDoc} from Fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mListBinding.getData().setScrolledToNewestConversation(false)

        return ComposeView(requireContext()).apply {
            setContent {
                MessagingTheme {
                    AndroidView(factory = {
                        recyclerView = RecyclerView(requireContext()).apply {
                            val manager: LinearLayoutManager =
                                object : LinearLayoutManager(activity) {
                                    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
                                        return RecyclerView.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                    }
                                }
                            setLayoutManager(manager)
                            setHasFixedSize(true)
                            setAdapter(mAdapter)
                            setOnScrollListener(object : RecyclerView.OnScrollListener() {
                                var mCurrentState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                                override fun onScrolled(
                                    recyclerView: RecyclerView,
                                    dx: Int,
                                    dy: Int
                                ) {
                                    if (mCurrentState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
                                        || mCurrentState == AbsListView.OnScrollListener.SCROLL_STATE_FLING
                                    ) {
                                        ImeUtil.get()
                                            .hideImeKeyboard(requireActivity(), recyclerView)
                                    }
                                    mListBinding.getData().setScrolledToNewestConversation(false)

                                }

                                override fun onScrollStateChanged(
                                    recyclerView: RecyclerView,
                                    newState: Int
                                ) {
                                    mCurrentState = newState
                                }
                            })
                            addOnItemTouchListener(ConversationListSwipeHelper(this))
                            if (savedInstanceState != null) {
                                mListState = savedInstanceState.getParcelable(
                                    SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY
                                )
                            }

                            // The root view has a non-null background, which by default is deemed by the framework
                            // to be a "transition group," where all child views are animated together during an
                            // activity transition. However, we want each individual items in the recycler view to
                            // show explode animation themselves, so we explicitly tag the root view to be a non-group.
//                            ViewGroupCompat.setTransitionGroup(recyclerView, false)
//                            setHasOptionsMenu(true)
                        }
                        recyclerView
                    })
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (VERBOSE) {
            LogUtil.v(LogUtil.BUGLE_TAG, "Attaching List")
        }
        val arguments: Bundle? = arguments
        if (arguments != null) {
            mArchiveMode = arguments.getBoolean(BUNDLE_ARCHIVED_MODE, false)
            mForwardMessageMode = arguments.getBoolean(BUNDLE_FORWARD_MESSAGE_MODE, false)
        }
        mListBinding.bind(DataModel.get().createConversationListData(activity, this, mArchiveMode))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mListState != null) {
            outState.putParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY, mListState)
        }
    }

    override fun onPause() {
        super.onPause()
        mListState = recyclerView.layoutManager!!.onSaveInstanceState()
        mListBinding.getData().setScrolledToNewestConversation(false)
    }

    /**
     * Call this immediately after attaching the fragment
     */
    fun setHost(host: ConversationListFragmentHost?) {
        Assert.isNull(mHost)
        mHost = host
    }

    override fun onConversationListCursorUpdated(
        data: ConversationListData,
        cursor: Cursor?
    ) {
        mListBinding.ensureBound(data)
        val oldCursor: Cursor? = mAdapter.swapCursor(cursor)
//        updateEmptyListUi(cursor == null || cursor.count == 0)
        if ((mListState != null) && (cursor != null) && (oldCursor == null)) {
            recyclerView.layoutManager!!.onRestoreInstanceState(mListState)
        }
    }

    override fun setBlockedParticipantsAvailable(blockedAvailable: Boolean) {
        mBlockedAvailable = blockedAvailable
        if (mShowBlockedMenuItem != null) {
            mShowBlockedMenuItem!!.isVisible = blockedAvailable
        }
    }

    fun updateUi() {
        mAdapter.notifyDataSetChanged()
    }


    /**
     * {@inheritDoc} from ConversationListItemView.HostInterface
     */
    override fun onConversationClicked(
        conversationListItemData: ConversationListItemData,
        isLongClick: Boolean, conversationView: ConversationListItemView
    ) {
        val listData: ConversationListData = mListBinding.getData()
        mHost!!.onConversationClick(
            listData, conversationListItemData, isLongClick,
            conversationView
        )
    }

    /**
     * {@inheritDoc} from ConversationListItemView.HostInterface
     */
    override fun isConversationSelected(conversationId: String): Boolean {
        return mHost!!.isConversationSelected(conversationId)
    }

    override fun isSwipeAnimatable(): Boolean {
        return mHost!!.isSwipeAnimatable()
    }


    override fun getSnackBarInteractions(): List<SnackBarInteraction> {
        val interactions: MutableList<SnackBarInteraction> = ArrayList(1)
        return interactions
    }


    override fun startFullScreenPhotoViewer(
        initialPhoto: Uri, initialPhotoBounds: Rect, photosUri: Uri
    ) {
        UIIntents.get().launchFullScreenPhotoViewer(
            activity, initialPhoto, initialPhotoBounds, photosUri
        )
    }

    override fun startFullScreenVideoViewer(videoUri: Uri) {
        UIIntents.get().launchFullScreenVideoViewer(activity, videoUri)
    }

    override fun isSelectionMode(): Boolean {
        return mHost != null && mHost!!.isSelectionMode()
    }

    companion object {
        private val BUNDLE_ARCHIVED_MODE: String = "archived_mode"
        private val BUNDLE_FORWARD_MESSAGE_MODE: String = "forward_message_mode"
        private val VERBOSE: Boolean = false

        // Saved Instance State Data - only for temporal data which is nice to maintain but not
        // critical for correctness.
        private val SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY: String = "conversationListViewState"
        fun createArchivedConversationListFragment(): ComposeConversationListFragment {
            return createConversationListFragment(BUNDLE_ARCHIVED_MODE)
        }

        fun createForwardMessageConversationListFragment(): ComposeConversationListFragment {
            return createConversationListFragment(BUNDLE_FORWARD_MESSAGE_MODE)
        }

        fun createConversationListFragment(modeKeyName: String?): ComposeConversationListFragment {
            return ComposeConversationListFragment().apply {
                this.setArguments(Bundle().apply {
                    putBoolean(
                        modeKeyName,
                        true
                    )
                })
            }
        }
    }
}


