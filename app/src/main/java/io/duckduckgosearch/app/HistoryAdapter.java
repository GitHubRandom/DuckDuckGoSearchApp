package io.duckduckgosearch.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    Context context;
    ArrayList<HistoryItem> list;

    public HistoryAdapter(Context context, ArrayList<HistoryItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        holder.term.setText(list.get(position).getTerm());
        holder.date.setText("Today");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton deleteButton;
        TextView term, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteButton = itemView.findViewById(R.id.history_item_button);
            term = itemView.findViewById(R.id.history_item_term);
            date = itemView.findViewById(R.id.history_item_date);
        }
    }
}
