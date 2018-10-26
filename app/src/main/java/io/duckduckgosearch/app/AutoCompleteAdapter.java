package io.duckduckgosearch.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AutoCompleteAdapter extends ArrayAdapter<String> {

    private Context context;
    private int resId;
    private AutoCompleteTextView searchBar;
    OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClickListener(String searchTerm);
    }

    public AutoCompleteAdapter(@NonNull Context context, int resource,
                               @NonNull String[] objects, AutoCompleteTextView searchBar) {
        super(context, resource, objects);
        this.context = context;
        resId = resource;
        this.searchBar = searchBar;
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
            icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outline_history_24px_white));
            root.setBackgroundColor(context.getResources().getColor(R.color.darkThemeColorPrimary));
        }

        TextView textItem = convertView.findViewById(R.id.auto_complete_item_text);
        textItem.setText(getItem(position));

        return convertView;
    }

}
