package io.duckduckgosearch.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ErrorFragment extends Fragment {

    Context context;
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
        return view;
    }
}
