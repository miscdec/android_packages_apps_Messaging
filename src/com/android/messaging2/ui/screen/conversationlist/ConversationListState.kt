package com.android.messaging2.ui.screen.conversationlist


//sealed class MessageViewState {
//    object Loading : MessageViewState()
//    data class Success(val messages: List<SmsMessageModel>) : MessageViewState()
//    data class Error(val error: String) : MessageViewState()
//}
//
//sealed class MessageIntent {
//    object LoadMessages : MessageIntent()
//}
//
//
//class MessageViewModel() : ViewModel() {
//
//
//    private val _state = MutableStateFlow<MessageViewState>(MessageViewState.Loading)
//    val state: StateFlow<MessageViewState> = _state
//
//    private val _intent = Channel<MessageIntent>(Channel.UNLIMITED)
//    val intent = _intent.receiveAsFlow()
//
//    init {
//        handleIntent()
//
//    }
//
//    fun handleIntent() {
//        viewModelScope.launch {
//            intent.collect { messageIntent ->
//                when (messageIntent) {
//                    is MessageIntent.LoadMessages -> loadMessages()
//                }
//            }
//        }
//    }
//
//
//
////    private suspend fun loadMessages() {
////        try {
////            val messages = database.messageDao().getAllMessages()
////            _state.value = MessageViewState.Success(messages)
////        } catch (e: Exception) {
////            _state.value = MessageViewState.Error("Error loading messages")
////        }
////    }
//
//    fun processIntent(intent: MessageIntent) {
//        viewModelScope.launch {
//            _intent.send(intent)
//        }
//    }
//}
