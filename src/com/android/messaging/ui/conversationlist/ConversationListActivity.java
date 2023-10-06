/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging.ui.conversationlist;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.DebugUtils;
import com.android.messaging.util.Trace;

public class ConversationListActivity extends AbstractConversationListActivity {
    private SearchView.SearchAutoComplete mSearchEditText;
    private MenuItem mSearchItem;
    private SearchView mSearchView;
    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() { // from class: com.android.messaging.ui.conversationlist.ConversationListActivity.8
        @Override // androidx.appcompat.widget.SearchView.OnQueryTextListener
        public boolean onQueryTextSubmit(String query) {
            UIIntents.get().launchSearchActivity(ConversationListActivity.this, new Intent().putExtra("query", query));
            ConversationListActivity.this.mSearchItem.collapseActionView();
            return true;
        }

        @Override // androidx.appcompat.widget.SearchView.OnQueryTextListener
        public boolean onQueryTextChange(String newText) {
            System.out.println("onQueryTextChange()");
            if (newText != null && newText.length() > 512) {
                ConversationListActivity.this.mSearchView.setQuery(newText.substring(0, 511), false);
                ConversationListActivity conversationListActivity = ConversationListActivity.this;
                Toast.makeText(conversationListActivity, conversationListActivity.getString(R.string.search_max_length), Toast.LENGTH_LONG).show();
            }
            ConversationListActivity.this.setCloseBtnGone(true);
            return true;
        }
    };

    public ConversationListActivity(SearchView.SearchAutoComplete mSearchEditText) {
        this.mSearchEditText = mSearchEditText;
    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Trace.beginSection("ConversationListActivity.onCreate");
        setTheme(R.style.BugleTheme_ConversationListActivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list_activity);
        Trace.endSection();
        invalidateActionBar();
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.action_bar_background_color)));
        actionBar.show();
        super.updateActionBar(actionBar);
    }

    public ConversationListActivity() {
    }

    @Override
    public void onResume() {
        super.onResume();
        // Invalidate the menu as items that are based on settings may have changed
        // while not in the app (e.g. Talkback enabled/disable affects new conversation
        // button)
        supportInvalidateOptionsMenu();
        if (getSearchEditText() != null && getSearchEditText().getVisibility() == View.VISIBLE) {
            getSearchEditText().setFocusable(true);
            getSearchEditText().setFocusableInTouchMode(true);
            getSearchEditText().requestFocus();
        }
    }

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (super.onCreateOptionsMenu(menu)) {
            return true;
        }
        getMenuInflater().inflate(R.menu.conversation_list_fragment_menu, menu);
        final MenuItem item = menu.findItem(R.id.action_debug_options);
        if (item != null) {
            final boolean enableDebugItems = DebugUtils.isDebugEnabled();
            item.setVisible(enableDebugItems).setEnabled(enableDebugItems);
        }
        initSearchView(menu);
        return true;
    }

    public void initSearchView(Menu menu) {
        this.mSearchItem = menu.findItem(R.id.action_start_search);
        this.mSearchView = (SearchView) MenuItemCompat.getActionView(this.mSearchItem);
        this.mSearchView.setOnQueryTextListener(this.mQueryTextListener);
        this.mSearchView.setQueryHint(getString(R.string.search_hint));
        this.mSearchView.setIconifiedByDefault(false);
        this.mSearchView.setIconified(false);
        this.mSearchView.clearFocus();
        this.mSearchView.getResources().getIdentifier("android:id/search_src_text", null, null);
        setCloseBtnGone(true);
//        ImageView imageView = (ImageView) this.mSearchView.findViewById(R.id.search_button);
        this.mSearchView.setSubmitButtonEnabled(false);
        MenuItemCompat.setOnActionExpandListener(this.mSearchItem, new SearchViewExpandListener());
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            this.mSearchView.setSearchableInfo(info);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_start_new_conversation) {
            onActionBarStartNewConversation();
            return true;
        } else if (itemId == R.id.action_settings) {
            onActionBarSettings();
            return true;
        } else if (itemId == R.id.action_debug_options) {
            onActionBarDebug();
            return true;
        } else if (itemId == R.id.action_show_archived) {
            onActionBarArchived();
            return true;
        } else if (itemId == R.id.action_show_blocked_contacts) {
            onActionBarBlockedParticipants();
            return true;
        } else if (itemId == R.id.action_start_search) {
            onSearchRequested();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private SearchView.SearchAutoComplete getSearchEditText() {
        return this.mSearchEditText;
    }

    private void clearSearchText() {
        System.out.println("enter clearSearchText()");
        if (getSearchEditText() != null) {
            if (!TextUtils.isEmpty(getSearchEditText().getText())) {
                getSearchEditText().setText("");
            }
        }
    }

    public void setCloseBtnGone(boolean bool) {
        int closeBtnId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView mCloseButton = null;
        SearchView searchView = this.mSearchView;
        if (searchView != null) {
            mCloseButton = (ImageView) searchView.findViewById(closeBtnId);
        }
        if (mCloseButton != null) {
            mCloseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_small_light));
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean onSearchRequested() {
        MenuItem menuItem = this.mSearchItem;
        if (menuItem != null) {
            menuItem.expandActionView();
            this.mSearchView.setFocusable(true);
            this.mSearchView.setFocusableInTouchMode(true);
            this.mSearchView.requestFocus();
            clearSearchText();
        }
        return true;
    }

    public void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, 2);
    }

    public void hideSoftInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    class SearchViewExpandListener implements MenuItemCompat.OnActionExpandListener {

        @Override // androidx.core.view.MenuItemCompat.OnActionExpandListener
        public boolean onMenuItemActionCollapse(MenuItem item) {
            ConversationListActivity conversationListActivity = ConversationListActivity.this;
            conversationListActivity.hideSoftInput(conversationListActivity.mSearchView);
            return true;
        }

        @Override // androidx.core.view.MenuItemCompat.OnActionExpandListener
        public boolean onMenuItemActionExpand(MenuItem item) {
            if (!ConversationListActivity.this.isSelectionMode()) {
                ConversationListActivity.this.openKeyboard();
                return true;
            }
            return true;
        }
    }

    @Override
    public void onActionBarHome() {
        exitMultiSelectState();
    }

    public void onActionBarStartNewConversation() {
        UIIntents.get().launchCreateNewConversationActivity(this, null);
    }

    public void onActionBarSettings() {
        UIIntents.get().launchSettingsActivity(this);
    }

    public void onActionBarBlockedParticipants() {
        UIIntents.get().launchBlockedParticipantsActivity(this);
    }

    public void onActionBarArchived() {
        UIIntents.get().launchArchivedConversationsActivity(this);
    }


    @Override
    public boolean isSwipeAnimatable() {
        return !isInConversationListSelectMode();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final ConversationListFragment conversationListFragment =
                (ConversationListFragment) getSupportFragmentManager().findFragmentById(
                        R.id.conversation_list_fragment);
        // When the screen is turned on, the last used activity gets resumed, but it gets
        // window focus only after the lock screen is unlocked.
        if (hasFocus && conversationListFragment != null) {
            conversationListFragment.setScrolledToNewestConversationIfNeeded();
        }
    }
}
