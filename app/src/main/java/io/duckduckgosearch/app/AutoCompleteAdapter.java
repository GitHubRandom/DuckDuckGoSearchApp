package io.duckduckgosearch.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AutoCompleteAdapter extends ArrayAdapter<String> {

    private Context context;
    private int resId;
    String data[];

    public AutoCompleteAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        this.context = context;
        resId = resource;
        data = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            convertView = inflater.inflate(resId, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.auto_complete_item_icon);
        if (PrefManager.isDarkTheme(context)) {
            icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outline_history_24px_white));
        }

        TextView textItem = convertView.findViewById(R.id.auto_complete_item_text);
        textItem.setText(getItem(position));

        return convertView;
    }


}
