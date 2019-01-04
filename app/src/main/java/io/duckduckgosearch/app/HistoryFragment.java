package io.duckduckgosearch.app;

import android.app.Dialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Collections;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

public class HistoryFragment extends BottomSheetDialogFragment {

    static final String HISTORY_DB_NAME = "history_db";

    private HistoryAdapter adapter;
    private HistoryDatabase historyDatabase;
    private ArrayList<HistoryItem> historyList = new ArrayList<>();

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.fragment_history, null);

        historyDatabase = Room.databaseBuilder(getContext(), HistoryDatabase.class, HISTORY_DB_NAME).build();

        CoordinatorLayout root = view.findViewById(R.id.search_history_root);
        final TextView fragmentTitle = view.findViewById(R.id.history_fragment_title);
        final RecyclerView historyListRv = view.findViewById(R.id.history_fragment_list);
        historyListRv.setHasFixedSize(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                historyList = (ArrayList<HistoryItem>) historyDatabase.historyDao().getAllSearchHistory();
                if (historyList.size() == 0) {
                    fragmentTitle.setText(R.string.search_history_empty);
                }
                Collections.reverse(historyList);
                adapter = new HistoryAdapter(getContext(), historyList);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        historyListRv.setAdapter(adapter);
                    }
                });
            }
        }).start();

        ImageView dragIcon = view.findViewById(R.id.history_fragment_drag_icon);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        historyListRv.setItemAnimator(animator);
        historyListRv.setLayoutManager(manager);

        if (PrefManager.isDarkTheme(getContext())) {
            root.setBackground(getResources().getDrawable(R.drawable.history_fragment_bg_dark));
            fragmentTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
            dragIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_round_drag_handle_24px_dark));
        }

        dialog.setContentView(view);

        ((View) view.getParent()).setBackground(null);
    }


}
