package com.android.messaging2.ui.screen.appsetting

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class CaptchaKeywordsSettingsViewModel() : ViewModel() {

    private val _keywords = MutableStateFlow<MutableList<String>>(mutableListOf())

    //Channel信道，意图发送别ViewModel，
    val captchaChannel = Channel<CaptchaIntent>(Channel.UNLIMITED)

    //状态管理
    var uiState by mutableStateOf(CaptchaState())

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            captchaChannel.consumeAsFlow().collect { captchaIntent ->
                when (captchaIntent) {
                    is CaptchaIntent.GetCaptchaKeywordsResult -> queryKeywords(
                        ctx = captchaIntent.ctx,
                        spName = captchaIntent.spName,
                        defaultValue = captchaIntent.defaultValue,
                        key = captchaIntent.key
                    )

                    is CaptchaIntent.AddKeyword -> addKeyword(
                        keyword = captchaIntent.keyword,
                        ctx = captchaIntent.ctx,
                        spName = captchaIntent.spName,
                        defaultValue = captchaIntent.defaultValue,
                        key = captchaIntent.key
                    )

                    is CaptchaIntent.DeleteKeyword -> deleteKeyword(
                        keyword = captchaIntent.keyword,
                        ctx = captchaIntent.ctx,
                        spName = captchaIntent.spName,
                        defaultValue = captchaIntent.defaultValue,
                        key = captchaIntent.key
                    )
                }
            }
        }
    }

    private fun queryKeywords(
        ctx: Context,
        spName: String,
        defaultValue: HashSet<String>,
        key: String? = null,
    ) {
        viewModelScope.launch {
            val set by SharedPreferencesDelegate(
                context = ctx,
                spName = spName,
                defaultValue = defaultValue,
                key = key
            )
            _keywords.value = set.toMutableList()
            uiState = uiState.copy(dataList = _keywords.value)
        }

    }


    fun deleteKeyword(
        keyword: String,
        ctx: Context,
        spName: String,
        defaultValue: HashSet<String>,
        key: String? = null,
    ) {
        viewModelScope.launch {
            val set by SharedPreferencesDelegate(
                context = ctx,
                spName = spName,
                defaultValue = defaultValue,
                key = key
            )
            set.remove(keyword)
            _keywords.value = set.toMutableList()
            uiState = uiState.copy(dataList = _keywords.value)
        }
    }

    fun addKeyword(
        keyword: String,
        ctx: Context,
        spName: String,
        defaultValue: HashSet<String>,
        key: String? = null,
    ) {
        viewModelScope.launch {
            val set by SharedPreferencesDelegate(
                context = ctx,
                spName = spName,
                defaultValue = defaultValue,
                key = key
            )
            set.add(keyword)
            _keywords.value = set.toMutableList()
            uiState = uiState.copy(dataList = _keywords.value)
        }
    }
}

data class CaptchaState(
    val dataList: List<String> = emptyList()
)

sealed class CaptchaIntent {
    class GetCaptchaKeywordsResult(
        val ctx: Context,
        val spName: String,
        val defaultValue: HashSet<String>,
        val key: String? = null,
    ) : CaptchaIntent()

    class DeleteKeyword(
        val keyword: String,
        val ctx: Context,
        val spName: String,
        val defaultValue: HashSet<String>,
        val key: String? = null,
    ) : CaptchaIntent()

    class AddKeyword(
        val keyword: String,
        val ctx: Context,
        val spName: String,
        val defaultValue: HashSet<String>,
        val key: String? = null,
    ) : CaptchaIntent()
}


/**
 * SharedPreferences委托代理
 * @param context Context
 * @param spName SP存入的XML名字
 * @param defaultValue 默认值
 * @param key 存取数据时对应的key
 */
class SharedPreferencesDelegate<T>(
    private val context: Context,
    private val spName: String,
    private val defaultValue: T,
    private val key: String? = null,
) : ReadWriteProperty<Any?, T> {

    private val sp: SharedPreferences by lazy(LazyThreadSafetyMode.NONE) {
        context.getSharedPreferences(spName, Context.MODE_PRIVATE)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val finalKey = key ?: property.name
        return when (defaultValue) {
            is Int -> sp.getInt(finalKey, defaultValue)
            is Long -> sp.getLong(finalKey, defaultValue)
            is Float -> sp.getFloat(finalKey, defaultValue)
            is Boolean -> sp.getBoolean(finalKey, defaultValue)
            is String -> sp.getString(finalKey, defaultValue)
            is Set<*> -> sp.getStringSet(finalKey, defaultValue as? Set<String>)
            else -> throw IllegalStateException("Unsupported type")
        } as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val finalKey = key ?: property.name
        with(sp.edit()) {
            when (value) {
                is Int -> putInt(finalKey, value)
                is Long -> putLong(finalKey, value)
                is Float -> putFloat(finalKey, value)
                is Boolean -> putBoolean(finalKey, value)
                is String -> putString(finalKey, value)
                is Set<*> -> putStringSet(finalKey, value.map { it.toString() }.toHashSet())
//                is List<*> -> putStringSet(finalKey,value.toString()
//                    .split("\n".toRegex())
//                    .dropLastWhile { it.isEmpty() }
//                    .toHashSet()
//                )
                else -> throw IllegalStateException("Unsupported type")
            }
            apply()
        }
    }
}

