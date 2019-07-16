package io.duckduckgosearch.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is a custom adapter for search suggestions list
 */

public class AutoCompleteAdapter extends ArrayAdapter<String> {

    private Context context;
    private int resId;
    private AutoCompleteTextView searchBar;
    private OnItemClickListener clickListener;
    private String[] filteredList;
    private String[] list;

    public interface OnItemClickListener {
        void onItemClickListener(String searchTerm);
    }

    AutoCompleteAdapter(@NonNull Context context, int resource,
                        @NonNull String[] objects, AutoCompleteTextView searchBar) {
        super(context, resource, objects);
        this.context = context;
        resId = resource;
        list = objects;
        filteredList = Arrays.copyOf(list, 5, String[].class);
        this.searchBar = searchBar;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();

                if (list == null || list.length == 0) {
                    synchronized (this) {
                        results.values = null;
                        results.count = 0;
                    }
                } else {
                    if (charSequence == null || charSequence.length() == 0) {
                        synchronized (this) {
                            results.values = Arrays.copyOf(list, 5, String[].class);
                            results.count = 5;
                        }
                    } else {
                        ArrayList<String> matchingTerms = new ArrayList<>();
                        for (String item : list) {
                            if (item.toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                                matchingTerms.add(item);
                            }
                        }
                        if (matchingTerms.size() < 5) {
                            synchronized (this) {
                                results.values = Arrays.copyOf(matchingTerms.toArray(), matchingTerms.size(), String[].class);
                                results.count = matchingTerms.size();
                            }
                        } else {
                            synchronized (this) {
                                results.values = Arrays.copyOf(matchingTerms.toArray(), 5, String[].class);
                                results.count = 5;
                            }
                        }
                    }
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults == null) {
                    filteredList = new String[]{};
                    notifyDataSetInvalidated();
                } else {
                    filteredList = (String[]) filterResults.values;
                    if (filterResults.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
                Log.d("Filter", Arrays.toString(filteredList));
            }
        };
    }

    @Override
    public String getItem(int position) {
        return filteredList[position];
    }

    @Override
    public int getCount() {
        if (filteredList != null) {
            return filteredList.length;
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            convertView = inflater.inflate(resId, parent, false);
        }

        final int pos = position;

        ImageButton appendButton = convertView.findViewById(R.id.auto_complete_item_button);
        appendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
                searchBar.append(getItem(pos) + " ");
            }
        });

        RelativeLayout root = convertView.findViewById(R.id.auto_complete_item_root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clickListener = (OnItemClickListener) context;
                    clickListener.onItemClickListener(getItem(pos));
                    searchBar.setText(getItem(pos));
                } catch (ClassCastException e) {
                    Log.e("AutoCompleteAdapter", "You must implement the clickListenerInterface");
                }
            }
        });
        ImageView icon = convertView.findViewById(R.id.auto_complete_item_icon);
        if (PrefManager.isDarkTheme(context)) {
            appendButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outline_top_left_arrow_white));
            icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outline_history_24px_white));
            root.setBackgroundColor(context.getResources().getColor(R.color.darkThemeColorPrimary));
        }

        TextView textItem = convertView.findViewById(R.id.auto_complete_item_text);
        textItem.setText(getItem(position));

        return convertView;
    }
}
