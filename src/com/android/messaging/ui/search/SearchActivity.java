package com.android.messaging.ui.search;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.provider.Telephony;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversation.ConversationActivity;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* loaded from: classes2.dex */
public class SearchActivity extends ListActivity {

    public static final String CONVERSATION_ID = "conversation_id";
    private static final String TAG = "SearchActivity";
    private AsyncSearchHandler mAsyncSearchHandler;
    private ContentResolver mContentResolver;
    private String mConversationId;
    private TextView mEmptyView;
    private AsyncQueryHandler mQueryHandler;
    private String mSearchStringParameter;
    private Uri mUri;
    private final ContentObserver mChangeObserver = new ContentObserver(new Handler()) { // from class: com.android.messaging.ui.SearchActivity.4
        @Override // android.database.ContentObserver
        public void onChange(boolean selfUpdate) {
            if (!selfUpdate && SearchActivity.this.mUri != null && SearchActivity.this.mQueryHandler != null) {
                new Handler().postDelayed(new Runnable() { // from class: com.android.messaging.ui.SearchActivity.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                    }
                }, 1500L);
            }
        }
    };

    public static byte[] getBytesUsingUtf8(String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    @Override // android.app.Activity
    public void onStop() {
        super.onStop();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public long getThreadId(long sourceId, long which) {
        Uri.Builder b = Uri.parse("content://mms-sms/messageIdToThread").buildUpon();
        String s = b.appendQueryParameter("row_id", String.valueOf(sourceId)).appendQueryParameter("table_to_use", String.valueOf(which)).build().toString();
        Cursor c = getContentResolver().query(Uri.parse(s), null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getLong(c.getColumnIndex("thread_id"));
                }
                return -1L;
            } finally {
                c.close();
            }
        }
        return -1L;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mSearchStringParameter = getIntent().getStringExtra("query");
        if (this.mSearchStringParameter == null) {
            this.mSearchStringParameter = getIntent().getStringExtra("intent_extra_data_key");
        }
        this.mConversationId = getIntent().getStringExtra("conversation_id");
        final String searchString = (this.mSearchStringParameter != null) ? this.mSearchStringParameter.trim() : this.mSearchStringParameter;
        Log.d("jordan", "searchString = " + searchString);
        Log.d("intent", getIntent().toString());
        final Uri u = getIntent().getData();
        if (u != null && u.getQueryParameter("source_id") != null) {
            // from class: com.android.messaging.ui.SearchActivity.1
// java.lang.Runnable
            Thread t = new Thread(() -> {
                try {
                    long sourceId = Long.parseLong(u.getQueryParameter("source_id"));
                    long whichTable = Long.parseLong(u.getQueryParameter("which_table"));
                    long threadId = SearchActivity.this.getThreadId(sourceId, whichTable);
                    PrintStream printStream2 = System.out;
                    printStream2.println("sourceId = " + sourceId + ", whichTable = " + whichTable + ", threadId = " + threadId);
                    Intent onClickIntent = new Intent(SearchActivity.this, ConversationActivity.class);
                    onClickIntent.putExtra("highlight", searchString);
                    onClickIntent.putExtra("select_id", sourceId);
                    onClickIntent.putExtra("thread_id", threadId);
                    SearchActivity.this.startActivity(onClickIntent);
                    SearchActivity.this.finish();
                } catch (NumberFormatException ignored) {
                }
            }, "Search thread");
            t.start();
            return;
        }
        setContentView(R.layout.search_activity);
        this.mContentResolver = getContentResolver();
        registerConversationListChangeObserver();
        ListView listView = getListView();
        listView.setItemsCanFocus(true);
        listView.setFocusable(true);
        listView.setClickable(true);
        this.mEmptyView = findViewById(R.id.empty_view);
        setTitle("");
        this.mAsyncSearchHandler = new AnonymousClass2(searchString, listView);
        this.mQueryHandler = new AnonymousClass3(this.mContentResolver, searchString, listView);
        this.mUri = Telephony.MmsSms.SEARCH_URI.buildUpon().appendQueryParameter("pattern", searchString).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ArrayList<SearchViewBean> changeCursorToList(Cursor cursor, String searchString) {
        ArrayList<SearchViewBean> arrayList = new ArrayList<>();
        if (cursor != null) {
            DatabaseWrapper dbWrapper = DataModel.get().getDatabase();
            while (cursor.moveToNext()) {
                SearchViewBean searchViewBean = new SearchViewBean();
                long conversationId = cursor.getLong(cursor.getColumnIndex("conversation_id"));
                long threadId = BugleDatabaseOperations.getThreadId(dbWrapper, String.valueOf(conversationId));
                String textStr = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PartColumns.TEXT));
                String contentType = cursor.getString(cursor.getColumnIndex("content_type"));
                int messageId = cursor.getInt(cursor.getColumnIndex("message_id"));
                searchViewBean.conversationName = BugleDatabaseOperations.getExistingConversationName(dbWrapper, threadId);
                searchViewBean.threadId = threadId;
                searchViewBean.conversationId = String.valueOf(conversationId);
                searchViewBean.messageId = messageId;
                searchViewBean.snipText = textStr;

                if (searchViewBean.snipText.contains(searchString)) {
                    arrayList.add(searchViewBean);
                }
            }
            cursor.close();
        }
        return arrayList;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
            return true;
        }
        return false;
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        PrintStream printStream = System.out;
        printStream.println("jordan,enter onStart(), the mUri = " + this.mUri.toString());
        this.mAsyncSearchHandler.startSearch(this.mConversationId, this.mSearchStringParameter.trim());
    }

    @Override // android.app.ListActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver != null) {
            contentResolver.unregisterContentObserver(this.mChangeObserver);
        }
    }

    private void registerConversationListChangeObserver() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver != null) {
            contentResolver.registerContentObserver(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, true, this.mChangeObserver);
        }
    }

    /* loaded from: classes2.dex */
    public static class TextViewSnippet extends AppCompatTextView {
        private static final String sEllipsis = "…";
        private static final int sTypefaceHighlight = 1;
        private String mFullText;
        private Pattern mPattern;
        private String mTargetString;

        public TextViewSnippet(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public TextViewSnippet(Context context) {
            super(context);
        }

        public TextViewSnippet(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override // android.widget.TextView, android.view.View
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            String snippetString;
            TextViewSnippet textViewSnippet = this;
            if (textViewSnippet.mFullText == null) {
                textViewSnippet.mFullText = "";
            }
            if (textViewSnippet.mTargetString == null) {
                return;
            }
            String fullTextLower = textViewSnippet.mFullText.toLowerCase();
            String targetStringLower = textViewSnippet.mTargetString.toLowerCase();
            int startPos = 0;
            int searchStringLength = targetStringLower.length();
            int bodyLength = fullTextLower.length();
            Matcher m = textViewSnippet.mPattern.matcher(textViewSnippet.mFullText);
            int i = 0;
            if (m.find(0)) {
                startPos = m.start();
            }
            TextPaint tp = getPaint();
            float searchStringWidth = tp.measureText(textViewSnippet.mTargetString);
            float ellipsisWidth = tp.measureText(sEllipsis);
            float textFieldWidth = getWidth() - (2.0f * ellipsisWidth);
            if (searchStringWidth > textFieldWidth && bodyLength >= searchStringLength) {
                snippetString = textViewSnippet.mFullText.substring(startPos, startPos + searchStringLength);
            } else {
                int offset = -1;
                int start = -1;
                String snippetString2 = null;
                int end = -1;
                while (true) {
                    offset++;
                    String fullTextLower2 = fullTextLower;
                    int newstart = Math.max(i, startPos - offset);
                    int newend = Math.min(bodyLength, startPos + searchStringLength + offset);
                    if (newstart == start && newend == end) {
                        break;
                    }
                    start = newstart;
                    end = newend;
                    String candidate = textViewSnippet.mFullText.substring(start, end);
                    if (tp.measureText(candidate) > textFieldWidth) {
                        break;
                    }
                    String targetStringLower2 = targetStringLower;
                    Object[] objArr = new Object[3];
                    objArr[0] = start == 0 ? "" : sEllipsis;
                    objArr[1] = candidate;
                    objArr[2] = end == bodyLength ? "" : sEllipsis;
                    snippetString2 = String.format("%s%s%s", objArr);
                    textViewSnippet = this;
                    fullTextLower = fullTextLower2;
                    targetStringLower = targetStringLower2;
                    i = 0;
                }
                snippetString = snippetString2;
            }
            if (snippetString == null) {
                snippetString = "";
            }
            SpannableString spannable = new SpannableString(snippetString);
            int start2 = 0;
            Matcher m2 = textViewSnippet.mPattern.matcher(snippetString);
            if (!textViewSnippet.mTargetString.equals("")) {
                while (m2.find(start2)) {
                    spannable.setSpan(new StyleSpan(sTypefaceHighlight), m2.start(), m2.end(), 0);
                    start2 = m2.end();
                    targetStringLower = targetStringLower;
                }
            }
            textViewSnippet.setText(spannable);
            super.onLayout(changed, left, top, right, bottom);
        }

        public void setText(String fullText, String target) {
            String patternString = Pattern.quote(target);
            this.mPattern = Pattern.compile(patternString, 2);
            this.mFullText = fullText == null ? "" : fullText;
            this.mTargetString = target;
            requestLayout();
        }
    }

    /* loaded from: classes2.dex */
    public static abstract class AsyncSearchHandler extends Handler {
        private static final int EVENT_ARG_QUERY = 1;

        public abstract void onQueryComplete(Cursor cursor);

        public void startSearch(final String conversationId, final String key) {
            // from class: com.android.messaging.ui.SearchActivity.AsyncSearchHandler.1
// java.lang.Runnable
            new Thread(() -> {
                Cursor cursor = AsyncSearchHandler.this.startQuery(conversationId, key);
                Message message = new Message();
                message.what = 1;
                message.obj = cursor;
                AsyncSearchHandler.this.sendMessage(message);
            }).start();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public Cursor startQuery(String conversationId, String searchString) {
            DatabaseWrapper dbWrapper = DataModel.get().getDatabase();
            dbWrapper.beginTransaction();
            searchString.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_").replace("'", "''");
            Cursor cursor = BugleDatabaseOperations.queryMessage(dbWrapper, conversationId, "");
            if (cursor != null) {
                dbWrapper.setTransactionSuccessful();
            }
            dbWrapper.endTransaction();
            return cursor;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                onQueryComplete((Cursor) msg.obj);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.messaging.ui.SearchActivity$2  reason: invalid class name */
    /* loaded from: classes2.dex */
    public class AnonymousClass2 extends AsyncSearchHandler {
        final /* synthetic */ ListView val$listView;
        final /* synthetic */ String val$searchString;

        AnonymousClass2(String str, ListView listView) {
            this.val$searchString = str;
            this.val$listView = listView;
        }

        @Override // com.android.messaging.ui.SearchActivity.AsyncSearchHandler
        public void onQueryComplete(Cursor c) {
            if (c == null) {
                SearchActivity searchActivity = SearchActivity.this;
                searchActivity.setTitle(searchActivity.getResources().getQuantityString(R.plurals.search_results_title, 0, 0, this.val$searchString));
                System.out.println("enter SearchActivity, Query result cursor is null, return");
                return;
            }
            PrintStream printStream = System.out;
            printStream.println("jordan,in SearchActivity cursor size = " + c.getCount());
            int cursorCount = c.getCount();
            new AnonymousClass1(c).execute();
            this.val$listView.setFocusable(true);
            this.val$listView.setFocusableInTouchMode(true);
            this.val$listView.requestFocus();
            if (cursorCount <= 0) {
                SearchActivity.this.mEmptyView.setVisibility(View.VISIBLE);
                SearchActivity.this.mEmptyView.setText(R.string.search_empty);
                return;
            }
            SearchRecentSuggestions recent = null;
            if (0 != 0) {
                recent.saveRecentQuery(this.val$searchString, SearchActivity.this.getString(R.string.search_history, Integer.valueOf(cursorCount), this.val$searchString));
            }
        }

        /* renamed from: com.android.messaging.ui.SearchActivity$2$1  reason: invalid class name */
        /* loaded from: classes2.dex */
        class AnonymousClass1 extends AsyncTask<Void, Void, ArrayList<SearchViewBean>> {
            final /* synthetic */ Cursor val$c;

            AnonymousClass1(Cursor cursor) {
                this.val$c = cursor;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public ArrayList<SearchViewBean> doInBackground(Void... arg0) {
                ArrayList<SearchViewBean> searchList = SearchActivity.this.changeCursorToList(this.val$c, AnonymousClass2.this.val$searchString);
                return searchList;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(ArrayList<SearchViewBean> searchList) {
                SearchActivity.this.setTitle(SearchActivity.this.getResources().getQuantityString(R.plurals.search_results_title, searchList.size(), searchList.size(), AnonymousClass2.this.val$searchString));
                BaseAdapter adapter = new C00051(searchList);
                SearchActivity.this.setListAdapter(adapter);
            }

            /* JADX INFO: Access modifiers changed from: package-private */
            /* renamed from: com.android.messaging.ui.SearchActivity$2$1$1  reason: invalid class name and collision with other inner class name */
            /* loaded from: classes2.dex */
            public class C00051 extends BaseAdapter {
                final /* synthetic */ ArrayList val$searchList;

                C00051(ArrayList arrayList) {
                    this.val$searchList = arrayList;
                }

                @Override // android.widget.Adapter
                public int getCount() {
                    return this.val$searchList.size();
                }

                @Override // android.widget.Adapter
                public Object getItem(int position) {
                    return null;
                }

                @Override // android.widget.Adapter
                public long getItemId(int position) {
                    return 0L;
                }

                @Override // android.widget.Adapter
                public View getView(int position, View convertView, ViewGroup parent) {
                    final SearchViewBean bean = (SearchViewBean) this.val$searchList.get(position);
                    LayoutInflater inflater = LayoutInflater.from(SearchActivity.this);
                    View view = inflater.inflate(R.layout.search_item, parent, false);
                    TextView title = (TextView) view.findViewById(R.id.title);
                    TextViewSnippet snippet = (TextViewSnippet) view.findViewById(R.id.subtitle);
                    snippet.setText(bean.snipText, AnonymousClass2.this.val$searchString);
                    title.setText(bean.conversationName);
                    // from class: com.android.messaging.ui.SearchActivity.2.1.1.1
// android.view.View.OnClickListener
                    view.setOnClickListener(v -> new Thread(() -> {
                        DatabaseWrapper dbWrapper = DataModel.get().getDatabase();
                        dbWrapper.beginTransaction();
                        Cursor cursor = BugleDatabaseOperations.queryMessage(dbWrapper, bean.conversationId, "");
                        dbWrapper.endTransaction();
                        int index = 0;
                        if (cursor != null) {
                            while (cursor.moveToNext() && bean.messageId != cursor.getInt(cursor.getColumnIndex("message_id"))) {
                                index++;
                            }
                            cursor.close();
                        }
                        UIIntents.get().launchConversationActivityWithSearch(SearchActivity.this, bean.conversationId, index);
                    }).start());
                    return view;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.messaging.ui.SearchActivity$3  reason: invalid class name */
    /* loaded from: classes2.dex */
    public class AnonymousClass3 extends AsyncQueryHandler {
        final /* synthetic */ ListView val$listView;
        final /* synthetic */ String val$searchString;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass3(ContentResolver x0, String str, ListView listView) {
            super(x0);
            this.val$searchString = str;
            this.val$listView = listView;
        }

        /* JADX WARN: Type inference failed for: r15v0, types: [com.android.messaging.ui.SearchActivity$3$1] */
        @Override // android.content.AsyncQueryHandler
        protected void onQueryComplete(int token, Object cookie, final Cursor c) {
            if (c == null) {
                SearchActivity searchActivity = SearchActivity.this;
                searchActivity.setTitle(searchActivity.getResources().getQuantityString(R.plurals.search_results_title, 0, 0, this.val$searchString));
                System.out.println("enter SearchActivity, Query result cursor is null, return");
                return;
            }
            PrintStream printStream = System.out;
            printStream.println("jordan,in SearchActivity cursor size = " + c.getCount());
            final int address = c.getColumnIndex("address");
            final int threadIdPos = c.getColumnIndex("thread_id");
            final int bodyPos = c.getColumnIndex("body");
            int cursorCount = c.getCount();
            SearchActivity searchActivity2 = SearchActivity.this;
            searchActivity2.setTitle(searchActivity2.getResources().getQuantityString(R.plurals.search_results_title, cursorCount, cursorCount, this.val$searchString));
            new AsyncTask<Void, Void, HashMap<Long, String[]>>() { // from class: com.android.messaging.ui.SearchActivity.3.1
                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public HashMap<Long, String[]> doInBackground(Void... arg0) {
                    HashMap<Long, String[]> convIdAndNames = new HashMap<>();
                    if (c != null) {
                        while (c.moveToNext()) {
                            long threadId = c.getLong(threadIdPos);
                            DatabaseWrapper dbWrapper = DataModel.get().getDatabase();
                            String convId = BugleDatabaseOperations.getExistingConversation(dbWrapper, threadId, false);
                            String convName = BugleDatabaseOperations.getExistingConversationName(dbWrapper, threadId);
                            convIdAndNames.put(Long.valueOf(threadId), new String[]{convId, convName});
                        }
                    }
                    return convIdAndNames;
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public void onPostExecute(final HashMap<Long, String[]> result) {
                    SearchActivity.this.setListAdapter(new CursorAdapter(SearchActivity.this, c, false) { // from class: com.android.messaging.ui.SearchActivity.3.1.1
                        @Override // android.widget.CursorAdapter
                        public void bindView(View view, final Context context, Cursor cursor) {
                            TextView title = (TextView) view.findViewById(R.id.title);
                            TextViewSnippet snippet = (TextViewSnippet) view.findViewById(R.id.subtitle);
                            long threadId = cursor.getLong(threadIdPos);
                            String[] convIdAndName = (String[]) result.get(Long.valueOf(threadId));
                            final String convId = convIdAndName[0];
                            String convName = convIdAndName[1];
                            String addressStr = cursor.getString(address);
                            String bodyStr = cursor.getString(bodyPos);
                            title.setText(convName.isEmpty() ? addressStr : convName);
                            snippet.setText(bodyStr, AnonymousClass3.this.val$searchString);
                            view.setOnClickListener(new View.OnClickListener() { // from class: com.android.messaging.ui.SearchActivity.3.1.1.1
                                @Override // android.view.View.OnClickListener
                                public void onClick(View v) {
                                    UIIntents.get().launchConversationActivity(context, convId, null, null, false);
                                }
                            });
                        }

                        @Override // android.widget.CursorAdapter
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            LayoutInflater inflater = LayoutInflater.from(context);
                            View v = inflater.inflate(R.layout.search_item, parent, false);
                            return v;
                        }
                    });
                }
            }.execute();
            this.val$listView.setFocusable(true);
            this.val$listView.setFocusableInTouchMode(true);
            this.val$listView.requestFocus();
            if (cursorCount <= 0) {
                SearchActivity.this.mEmptyView.setVisibility(View.VISIBLE);
                SearchActivity.this.mEmptyView.setText(R.string.search_empty);
                return;
            }
            SearchRecentSuggestions recent = null;
            if (0 != 0) {
                recent.saveRecentQuery(this.val$searchString, SearchActivity.this.getString(R.string.search_history, Integer.valueOf(cursorCount), this.val$searchString));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes2.dex */
    public class SearchViewBean {
        String conversationId;
        String conversationName;
        int messageId;
        String snipText;
        long threadId;

        SearchViewBean() {
        }
    }
}


