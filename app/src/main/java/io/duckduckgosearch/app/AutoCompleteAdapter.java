package io.duckduckgosearch.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    private DuckAutoCompleteTextView searchBar;
    private OnItemClickListener clickListener;
    private String[] filteredList;
    private String[] list;
    private ArrayList<String> suggestions;
    private int historyCount;

    public interface OnItemClickListener {
        void onItemClickListener(String searchTerm);
    }

    AutoCompleteAdapter(@NonNull Context context, int resource,
                        @NonNull String[] objects, DuckAutoCompleteTextView searchBar) {
        super(context, resource, objects);
        this.context = context;
        resId = resource;
        list = objects;
        if (list.length >= 5) {
            filteredList = Arrays.copyOf(list, 5, String[].class);
        } else {
            filteredList = Arrays.copyOf(list, list.length, String[].class);
        }
        this.searchBar = searchBar;
        historyCount = filteredList.length;
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
                        if (list.length < 5) {
                            synchronized (this) {
                                results.values = Arrays.copyOf(list, list.length, String[].class);
                                results.count = list.length;
                            }
                        } else {
                            synchronized (this) {
                                results.values = Arrays.copyOf(list, 5, String[].class);
                                results.count = 5;
                            }
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
            protected void publishResults(CharSequence charSequence, final FilterResults filterResults) {
                OnlineACParser parser = new OnlineACParser((String[])filterResults.values);
                parser.setOnParseListener(new OnlineACParser.OnParsed() {
                    @Override
                    public void onParsed(ArrayList<String> list) {
                        suggestions = list;
                        if (filterResults.values == null) {
                            if (suggestions != null && suggestions.size() != 0) {
                                if (suggestions.size() < 5) {
                                    filteredList = Arrays.copyOf(suggestions.toArray(), suggestions.size(), String[].class);
                                } else {
                                    filteredList = Arrays.copyOf(suggestions.toArray(), 5, String[].class);
                                }
                            }
                            filteredList = new String[]{};
                            notifyDataSetInvalidated();
                        } else {
                            if (suggestions != null && suggestions.size() != 0) {
                                ArrayList<String> finalList = new ArrayList<>(Arrays.asList((String[]) filterResults.values));
                                if (filterResults.count < 5) {
                                    for (int i = 0; i < 5 - filterResults.count; i++) {
                                        finalList.add(suggestions.get(i));
                                    }
                                }
                                filteredList = Arrays.copyOf(finalList.toArray(), finalList.size(), String[].class);
                            } else {
                                filteredList = (String[]) filterResults.values;
                            }
                            if (filteredList.length > 0) {
                                notifyDataSetChanged();
                            } else {
                                notifyDataSetInvalidated();
                            }
                        }
                    }
                });
                parser.execute(charSequence.toString());
                historyCount = filterResults.count;
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
        if (position >= historyCount) {
            icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_search_24px));
        } else {
            icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outline_history_24px));
        }
        if (PrefManager.isDarkTheme(context)) {
            appendButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outline_top_left_arrow_white));
            if (position >= historyCount) {
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_search_24px_white));
            } else {
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outline_history_24px_white));
            }
            root.setBackgroundColor(context.getResources().getColor(R.color.darkThemeColorPrimary));
        }

        TextView textItem = convertView.findViewById(R.id.auto_complete_item_text);
        textItem.setText(getItem(position));

        return convertView;
    }
}
