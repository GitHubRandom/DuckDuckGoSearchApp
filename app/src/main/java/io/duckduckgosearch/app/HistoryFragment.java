package io.duckduckgosearch.app;

import android.app.Dialog;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryFragment extends BottomSheetDialogFragment {

    RecyclerView historyList;
    HistoryAdapter adapter;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.fragment_history, null);
        adapter = new HistoryAdapter(getContext(), HistoryManager.getTermsAsArrayList(getContext()));
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        historyList = view.findViewById(R.id.history_fragment_list);
        historyList.setItemAnimator(animator);
        historyList.setLayoutManager(manager);
        historyList.setAdapter(adapter);
        dialog.setContentView(view);
    }
}
