package io.duckduckgosearch.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
        if (ThemeChecker.isDarkTheme(context)) {
            holder.icon.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.ic_outline_history_24px_white));
            holder.deleteButton.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.ic_outline_delete_forever_24px_white));
        }
        final String searchTerm = list.get(position).getTerm();
        holder.term.setText(list.get(position).getTerm());
        holder.date.setText("Today");
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(context, SearchActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("search_term", searchTerm);
                searchIntent.putExtras(bundle);
                context.startActivity(searchIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton deleteButton;
        ImageView icon;
        TextView term, date;
        RelativeLayout root;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteButton = itemView.findViewById(R.id.history_item_button);
            term = itemView.findViewById(R.id.history_item_term);
            date = itemView.findViewById(R.id.history_item_date);
            icon = itemView.findViewById(R.id.history_item_icon);
            root = itemView.findViewById(R.id.history_item_root);
        }
    }
}
