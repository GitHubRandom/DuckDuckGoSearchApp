package io.duckduckgosearch.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryFragment extends BottomSheetDialogFragment {

    static final String HISTORY_DB_NAME = "history_db";

    private Context context;
    private HistoryAdapter adapter;
    private HistoryDatabase historyDatabase;
    private ArrayList<HistoryItem> historyList = new ArrayList<>();
    private TextView fragmentTitle;
    private RecyclerView historyListRv;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.fragment_history, null);

        historyDatabase = Room.databaseBuilder(context, HistoryDatabase.class, HISTORY_DB_NAME).build();

        CoordinatorLayout root = view.findViewById(R.id.search_history_root);
        fragmentTitle = view.findViewById(R.id.history_fragment_title);
        historyListRv = view.findViewById(R.id.history_fragment_list);
        historyListRv.setHasFixedSize(true);

        if (PrefManager.isHistoryEnabled(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    historyList = (ArrayList<HistoryItem>) historyDatabase.historyDao().getAllSearchHistory();
                    if (historyList.size() == 0) {
                        fragmentTitle.setText(R.string.search_history_empty);
                    }
                    Collections.reverse(historyList);
                    adapter = new HistoryAdapter(getContext(), historyList);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            historyListRv.setAdapter(adapter);
                        }
                    });
                }
            }).start();
        } else {
            fragmentTitle.setText(R.string.search_history_disabled);
        }


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
