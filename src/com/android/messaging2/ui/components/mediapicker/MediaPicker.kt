package com.android.messaging2.ui.components.mediapicker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.android.messaging.datamodel.binding.Binding
import com.android.messaging.datamodel.binding.BindingBase
import com.android.messaging.datamodel.binding.ImmutableBindingRef
import com.android.messaging.datamodel.data.DraftMessageData
import com.android.messaging.datamodel.data.DraftMessageData.DraftMessageSubscriptionDataProvider
import com.android.messaging.datamodel.data.MessagePartData
import com.android.messaging.datamodel.data.PendingAttachmentData
import com.android.messaging2.ui.screen.search.SearchBean
import com.android.messaging2.ui.theme.MessagingTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

/**
 * Fragment used to select or capture media to be added to the message
 */
class MediaPicker2 : Fragment(), DraftMessageSubscriptionDataProvider {

    // Use the 'by activityViewModels()' Kotlin property delegate
    // from the fragment-ktx artifact
    private val model: MediaPickerViewModel by activityViewModels()

    var fullScreen: Boolean = false

    /** Provides subscription-related data to access per-subscription configurations.  */
    private lateinit var mSubscriptionDataProvider: DraftMessageSubscriptionDataProvider


    /** Provides access to DraftMessageData associated with the current conversation  */
    private lateinit var mDraftMessageDataModel: ImmutableBindingRef<DraftMessageData>
//    lateinit var listener: MediaPickerListener


//    var conversationThemeColor: Int

    /** The listener interface for events from the media picker  */
    interface MediaPickerListener {
        /** Called when the media picker is opened so the host can accommodate the UI  */
        fun onOpened()

        /**
         * Called when the media picker goes into or leaves full screen mode so the host can
         * accommodate the fullscreen UI
         */
        fun onFullScreenChanged(fullScreen: Boolean)

        /**
         * Called when the user selects one or more items
         * @param items The list of items which were selected
         */
        fun onItemsSelected(items: Collection<MessagePartData>, dismissMediaPicker: Boolean)

        /**
         * Called when the user unselects one item.
         */
        fun onItemUnselected(item: MessagePartData)

        /**
         * Called when the media picker is closed.  Always called immediately after onItemsSelected
         */
        fun onDismissed()

        /**
         * Called when media item selection is confirmed in a multi-select action.
         */
        fun onConfirmItemSelection()

        /**
         * Called when a pending attachment is added.
         * @param pendingItem the pending attachment data being loaded.
         */
        fun onPendingItemAdded(pendingItem: PendingAttachmentData?)

        /**
         * Called when a new media chooser is selected.
         */
        fun onChooserSelected(chooserIndex: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Update the UI
            setContent {
                MessagingTheme {
                    AnimatedVisibility(visible = model.uiState.show) {
                        Box(modifier = Modifier) {
                            Text(text = "Container")
                        }
                    }
                }
            }


        }
    }

    fun setDraftMessageDataModel(draftBinding: BindingBase<DraftMessageData>) {
        mDraftMessageDataModel = Binding.createBindingReference(draftBinding)
    }

    fun getDraftMessageDataModel(): ImmutableBindingRef<DraftMessageData> {
        return mDraftMessageDataModel
    }

    fun setSubscriptionDataProvider(provider: DraftMessageSubscriptionDataProvider) {
        mSubscriptionDataProvider = provider
    }

    override fun getConversationSelfSubId(): Int {
        return mSubscriptionDataProvider.getConversationSelfSubId()
    }

    /**
     * Opens the media picker and optionally shows the chooser for the supplied media type
     * @param startingMediaType The media type of the chooser to open if [.MEDIA_TYPE_DEFAULT]
     * is used, then the default chooser from saved shared prefs is opened
     */
    fun open(startingMediaType: Int, animate: Boolean) {
        lifecycleScope.launch {
            model.newsChannel.send(MediaPickerIntent.Show)
        }
    }

    fun updateActionBar(actionBar: ActionBar) {

    }

    fun setFullscreen(boolean: Boolean) {
        fullScreen = boolean
    }

    fun onBackPressed(): Boolean {
        return false
    }


    fun dismiss(animate: Boolean) {
        lifecycleScope.launch {
            model.newsChannel.send(MediaPickerIntent.Hide)
        }

    }

    fun resetViewHolderState() {

    }

    fun canShowIme(): Boolean {
        return false
    }

    fun isOpen(): Boolean {
        return model.uiState.show
    }

    fun isFullScreen(): Boolean {
        return fullScreen
    }

    companion object {
        /** The tag used when registering and finding this fragment  */
        const val FRAGMENT_TAG = "mediapicker2"

        // Media type constants that the media picker supports
        const val MEDIA_TYPE_DEFAULT = 0x0000
        const val MEDIA_TYPE_NONE = 0x0000
        const val MEDIA_TYPE_IMAGE = 0x0001
        const val MEDIA_TYPE_VIDEO = 0x0002
        const val MEDIA_TYPE_AUDIO = 0x0004
        const val MEDIA_TYPE_VCARD = 0x0008
        const val MEDIA_TYPE_LOCATION = 0x0010
        private const val MEDA_TYPE_INVALID = 0x0020
        const val MEDIA_TYPE_ALL = 0xFFFF
        protected const val CAMERA_PERMISSION_REQUEST_CODE = 1
        protected const val LOCATION_PERMISSION_REQUEST_CODE = 2
        protected const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 3
        protected const val GALLERY_PERMISSION_REQUEST_CODE = 4
        protected const val READ_CONTACT_PERMISSION_REQUEST_CODE = 5
    }
}

class MediaPickerViewModel : ViewModel() {

    //Channel信道，意图发送别ViewModel，
    val newsChannel = Channel<MediaPickerIntent>(Channel.UNLIMITED)

    private var _state = MutableStateFlow(false)

    val state = _state.asStateFlow()

    //状态管理
    var uiState by mutableStateOf(MediaPickerState())

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            newsChannel.consumeAsFlow().collect {
                when (it) {
                    MediaPickerIntent.GetShowState -> getState()
                    MediaPickerIntent.Hide -> show()
                    MediaPickerIntent.Show -> hide()
                }
            }
        }
    }

    private fun hide() {
        _state.value = false
        uiState = uiState.copy(show = _state.value)
    }

    private fun show() {
        _state.value = true
        uiState = uiState.copy(show = _state.value)
    }

    private fun getState() {
        uiState = uiState.copy(show = _state.value)
    }

}

sealed class MediaPickerIntent {
    data object GetShowState : MediaPickerIntent()
    data object Show : MediaPickerIntent()
    data object Hide : MediaPickerIntent()
}

data class MediaPickerState(
    val dataList: List<SearchBean> = emptyList(),
    val show: Boolean = false
)



