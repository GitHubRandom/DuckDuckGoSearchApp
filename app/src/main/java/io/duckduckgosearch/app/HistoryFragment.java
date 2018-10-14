package io.duckduckgosearch.app;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryFragment extends BottomSheetDialogFragment {

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.fragment_history, null);

        LinearLayout root = view.findViewById(R.id.search_history_root);
        TextView fragmentTitle = view.findViewById(R.id.history_fragment_title);

        HistoryAdapter adapter;
        if (HistoryManager.getTermsAsArrayList(getContext()) != null) {
            adapter = new HistoryAdapter(getContext(), HistoryManager.getTermsAsArrayList(getContext()));
        } else {
            adapter = new HistoryAdapter(getContext(), new ArrayList<HistoryItem>());
            fragmentTitle.setText(R.string.search_history_empty);
        }

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        RecyclerView historyList = view.findViewById(R.id.history_fragment_list);
        historyList.setItemAnimator(animator);
        historyList.setLayoutManager(manager);
        historyList.setAdapter(adapter);

        if (ThemeChecker.isDarkTheme(getContext())) {
            root.setBackgroundColor(getResources().getColor(R.color.darkThemeColorPrimary));
        }

        dialog.setContentView(view);
    }
}
