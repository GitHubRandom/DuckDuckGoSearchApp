package io.duckduckgosearch.app;

import android.app.Activity;
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
import java.util.Collections;
import java.util.Date;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<HistoryItem> list;
    private HistoryDatabase historyDatabase;
    private OnLastTermDeleted onLastTermDeleted;

    interface OnLastTermDeleted {
        void onLastTermDeleted();
    }

    HistoryAdapter(Context context, ArrayList<HistoryItem> list) {
        this.context = context;
        if (list != null) {
            this.list = list;
        }
        historyDatabase = Room.databaseBuilder(context, HistoryDatabase.class, HistoryFragment.HISTORY_DB_NAME).build();
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        onLastTermDeleted = (OnLastTermDeleted) context;
        if (PrefManager.isDarkTheme(context)) {
            holder.icon.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.ic_outline_history_24px_white));
            holder.deleteButton.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.ic_outline_delete_forever_24px_white));
        }
        final String searchTerm = list.get(position).getSearchTerm();
        final int termPosition = position;
        holder.term.setText(list.get(position).getSearchTerm());
        holder.date.setText(calculatePastTime(list.get(position).getSearchDate()));
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
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (getItemCount() == 1) {
                            onLastTermDeleted.onLastTermDeleted();
                        }
                        historyDatabase.historyDao().delete(list.get(termPosition));
                        list = (ArrayList<HistoryItem>) historyDatabase.historyDao().getAllSearchHistory();
                        Collections.reverse(list);
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemRemoved(termPosition);
                                notifyItemRangeChanged(termPosition, getItemCount());
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton deleteButton;
        ImageView icon;
        TextView term, date;
        RelativeLayout root;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteButton = itemView.findViewById(R.id.history_item_button);
            term = itemView.findViewById(R.id.history_item_term);
            date = itemView.findViewById(R.id.history_item_date);
            icon = itemView.findViewById(R.id.history_item_icon);
            root = itemView.findViewById(R.id.history_item_root);
        }
    }

    private String calculatePastTime(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(Calendar.getInstance().getTime());
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int day = currentCalendar.get(Calendar.DAY_OF_YEAR);

        Calendar calendarToCompare = Calendar.getInstance();
        calendarToCompare.setTime(date);
        int yearToCompare = calendarToCompare.get(Calendar.YEAR);
        int monthToCompare = calendarToCompare.get(Calendar.MONTH);
        int weekToCompare = calendarToCompare.get(Calendar.WEEK_OF_YEAR);
        int dayToCompare = calendarToCompare.get(Calendar.DAY_OF_YEAR);

        if (dayToCompare == day) {
            return context.getResources().getString(R.string.today);
        } else if (weekToCompare == week) {
            int diff = day - dayToCompare;
            if (diff == 1) {
                return context.getResources().getString(R.string.yesterday);
            } else {
                return context.getResources().getString(R.string.past_days, diff);
            }
        } else if (monthToCompare == month) {
            int diff = week - weekToCompare;
            if (diff == 1) {
                return context.getResources().getString(R.string.last_week);
            } else {
                return context.getResources().getString(R.string.past_weeks, diff);
            }
        } else if (yearToCompare == year) {
            int diff = month - monthToCompare;
            if (diff == 1) {
                return context.getResources().getString(R.string.last_month);
            } else {
                return context.getResources().getString(R.string.past_months, diff);
            }
        } else {
            int diff = year - yearToCompare;
            if (diff == 1) {
                return context.getResources().getString(R.string.last_year);
            } else {
                return context.getResources().getString(R.string.past_years, diff);
            }
        }
    }
}
