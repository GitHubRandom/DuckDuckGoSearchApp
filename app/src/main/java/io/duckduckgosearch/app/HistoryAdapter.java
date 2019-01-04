package io.duckduckgosearch.app;

import android.annotation.SuppressLint;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    @SuppressLint("SimpleDateFormat")
    private String calculatePastTime(Date date) {
        DateFormat format = new SimpleDateFormat("EEE. MMM d, yyyy");
        DateFormat formatDay = new SimpleDateFormat("EEEE");
        DateFormat formatNoYear = new SimpleDateFormat("EEE. MMM d");

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(Calendar.getInstance().getTime());
        int day = currentCalendar.get(Calendar.DAY_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);

        Calendar calendarToCompare = Calendar.getInstance();
        calendarToCompare.setTime(date);
        int dayToCompare = calendarToCompare.get(Calendar.DAY_OF_YEAR);
        int yearToCompare = calendarToCompare.get(Calendar.YEAR);

        long diff = currentCalendar.getTimeInMillis() - calendarToCompare.getTimeInMillis();
        long seconds, minutes, hours, days;

        seconds = diff / 1000;
        if (seconds >= 60) {
            minutes = seconds / 60;
            if (minutes >= 60) {
                hours = minutes / 60;
                if (hours >= 24) {
                    days = hours / 24;
                    if (days < 2 && day - dayToCompare < 2 && day - dayToCompare > 0) {
                        return context.getResources().getString(R.string.time_calculation_yesterday);
                    } else if (days < 7 && day - dayToCompare < 7 && day - dayToCompare > 0) {
                        return formatDay.format(date);
                    } else {
                        if (yearToCompare == year) {
                            return formatNoYear.format(date);
                        } else {
                            return format.format(date);
                        }
                    }
                } else {
                    if (hours == 1) {
                        return context.getResources().getString(R.string.time_calculation_hour, 1);
                    } else {
                        return context.getResources().getString(R.string.time_calculation_hours, hours);
                    }
                }
            } else {
                return context.getResources().getString(R.string.time_calculation_minutes, minutes);
            }
        } else {
            return context.getResources().getString(R.string.time_calculation_few_moments);
        }

    }
}
