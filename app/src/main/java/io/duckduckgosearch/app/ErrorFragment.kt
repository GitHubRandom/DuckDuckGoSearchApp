package io.duckduckgosearch.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ErrorFragment extends Fragment {

    private Context context;
    private OnReloadButtonClick reloadButtonClick;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    public interface OnReloadButtonClick {
        void onReloadButtonClick();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_error, container, false);
        Button reload = view.findViewById(R.id.reload_button);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadButtonClick = (OnReloadButtonClick) context;
                reloadButtonClick.onReloadButtonClick();
            }
        });

        if (PrefManager.isDarkTheme(getContext())) {
            ((TextView) view.findViewById(R.id.error_text)).setTextColor(getResources().getColor(android.R.color.white));
            reload.setTextColor(getResources().getColor(R.color.darkThemeColorAccent));
            reload.setBackground(getResources().getDrawable(R.drawable.retry_button_bg_white));
        }

        return view;
    }
}
