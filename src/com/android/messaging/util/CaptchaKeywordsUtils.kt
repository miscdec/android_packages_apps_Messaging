package com.android.messaging.util

import android.content.Context
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import com.android.messaging.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CaptchaKeywordsUtils(private val mContext: Context) {

    private val mKeywords: MutableList<String> = ArrayList()

    var mPrefs: BuglePrefs = BuglePrefs.getApplicationPrefs()
    private val mDefaultKeywordString: String =
        mContext.getString(R.string.captcha_keywords_default)
    private val mKeywordsPrefsKey: String = mContext.getString(R.string.captcha_keywords_key)

    init {
        parseKeywordsList()
    }

    private fun parseKeywordsList() {
        mKeywords.clear()
        var keyWordsData = mPrefs.getString(mKeywordsPrefsKey, mDefaultKeywordString)
        if (TextUtils.isEmpty(keyWordsData)) keyWordsData = mDefaultKeywordString
        val keyWordsListArray =
            keyWordsData.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        mKeywords.addAll(listOf(*keyWordsListArray))
    }

    val keywordsList: List<String> get() = mKeywords

    private fun saveKeywordsList() {
        val keywordListData = TextUtils.join("\n", mKeywords)
        mPrefs.putString(mKeywordsPrefsKey, keywordListData)
    }

    fun addKeywordToList(str: String): Int {
        return if (!mKeywords.contains(str)) {
            mKeywords.add(str)
            0
        } else {
            -1
        }
    }

    fun addKeywordListToList(keywords: List<String>): Boolean {
        return mKeywords.addAll(keywords)
    }

    fun removeStringFromList(str: String) {
        mKeywords.remove(str)
        saveKeywordsList()
    }

    fun addKeywords(editText: EditText) {
        if (!TextUtils.isEmpty(editText.getText())) {
            val keywordArr =
                editText.getText().toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            for (keyword in keywordArr) {
                val addKeywordsResult = addKeywordToList(keyword)
                if (addKeywordsResult == -1) {
                    MaterialAlertDialogBuilder(mContext)
                        .setTitle(R.string.error)
                        .setMessage(R.string.captcha_keyword_duplicate)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                    return
                }
            }
            saveKeywordsList()
            Toast.makeText(
                mContext,
                R.string.captcha_keyword_add_successsful_tip,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            MaterialAlertDialogBuilder(mContext)
                .setTitle(R.string.error)
                .setMessage(R.string.captcha_keyword_add_empty)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    fun addKeywords(keywords: String): Boolean {
        return addKeywordListToList(
            keywords
                .split("\n".toRegex())
                .filter { it.isNotBlank() and it.isNotEmpty() }
                .distinct()
                .filterNot { keywordsList.contains(it) }
                .dropLastWhile { it.isEmpty() }
        )
    }
}
