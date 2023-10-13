package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.messaging.R;
import com.android.messaging.databinding.CaptchaKeywordsSettingsContainerBinding;
import com.android.messaging.util.CaptchaKeywordsUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class CaptchaKeywordsSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new CaptchaKeywordsSettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public static class CaptchaKeywordsSettingsFragment extends Fragment {

        private CaptchaKeywordsSettingsContainerBinding binding;

        private Context mContext;
        private CaptchaKeywordsUtils mCaptchaKeywordsUtils;

        public CaptchaKeywordsSettingsFragment() {
            // Required empty constructor
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            this.mContext = context;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            binding = CaptchaKeywordsSettingsContainerBinding.inflate(inflater);
            View mView = binding.getRoot();
            this.mCaptchaKeywordsUtils = new CaptchaKeywordsUtils(this.mContext);

            RecyclerView recyclerView = binding.keywordsContainer;
            recyclerView.setLayoutManager(new LinearLayoutManager(this.mContext, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new KeywordsRVAdapter(this.mCaptchaKeywordsUtils.getKeywordsList()));
            binding.addKeywordBtn.setOnClickListener(v -> {
                final EditText editText = new EditText(CaptchaKeywordsSettingsFragment.this.mContext);
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.add_captcha_keyword)
                        .setMessage(R.string.captcha_dialog_tip)
                        .setView(editText)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            CaptchaKeywordsSettingsFragment.this.mCaptchaKeywordsUtils.addKeywords(editText);
                            recyclerView.setAdapter(new KeywordsRVAdapter(this.mCaptchaKeywordsUtils.getKeywordsList()));
                        })
                        .show();
            });
            return mView;
        }

        public TextView createKeywordsView() {
            TextView textView = new TextView(this.mContext);
            TypedValue typedValue = new TypedValue();
            this.mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            textView.setBackgroundResource(typedValue.resourceId);
            textView.setClickable(true);
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setPadding(75, 36, 75, 36);
            textView.setTextColor(Color.BLACK);
            textView.setWidth(1080);
            textView.setMinHeight(156);
            return textView;
        }

        private class KeywordsRVAdapter extends RecyclerView.Adapter<KeywordsRVAdapter.KeywordsVH> {

            private final List<String> mKeywordsList;

            public KeywordsRVAdapter(List<String> keywordsList) {
                this.mKeywordsList = keywordsList;
            }

            @NonNull
            @Override
            public KeywordsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new KeywordsVH(createKeywordsView());
            }

            @Override
            public void onBindViewHolder(@NonNull KeywordsVH holder, int position) {
                String keyword = this.mKeywordsList.get(position);
                holder.textView.setText(keyword);
                holder.textView.setOnLongClickListener(view -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.dialog_delete_title)
                            .setMessage(R.string.dialog_delete_message)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                mCaptchaKeywordsUtils.removeStringFromList(keyword);
                                notifyItemRemoved(position);
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    return true;
                });
            }

            @Override
            public int getItemCount() {
                return mKeywordsList.size();
            }

            private class KeywordsVH extends RecyclerView.ViewHolder {

                private final TextView textView;

                public KeywordsVH(@NonNull View itemView) {
                    super(itemView);
                    this.textView = (TextView) itemView;
                }
            }

        }

    }

}
