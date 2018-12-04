package io.duckduckgosearch.app;

import android.app.Dialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

public class HistoryFragment extends BottomSheetDialogFragment {

    public static final String HISTORY_DB_NAME = "history_db";

    private HistoryAdapter adapter;
    private HistoryDatabase historyDatabase;
    private ArrayList<HistoryItem> historyList = new ArrayList<>();

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.fragment_history, null);

        historyDatabase = Room.databaseBuilder(getContext(), HistoryDatabase.class, HISTORY_DB_NAME).build();

        LinearLayout root = view.findViewById(R.id.search_history_root);
        TextView fragmentTitle = view.findViewById(R.id.history_fragment_title);
        final RecyclerView historyListRv = view.findViewById(R.id.history_fragment_list);
        historyListRv.setHasFixedSize(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                historyList = (ArrayList<HistoryItem>) historyDatabase.historyDao().getAllSearchHistory();
                adapter = new HistoryAdapter(getContext(), historyList);
                historyListRv.setAdapter(adapter);
            }
        }).start();

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        historyListRv.setItemAnimator(animator);
        historyListRv.setLayoutManager(manager);

        if (PrefManager.isDarkTheme(getContext())) {
            root.setBackground(getResources().getDrawable(R.drawable.history_fragment_bg_dark));
            fragmentTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        dialog.setContentView(view);

        ((View) view.getParent()).setBackground(null);
    }


}
