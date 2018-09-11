package io.duckduckgosearch.app;

import android.content.Context;
import android.os.AsyncTask;

public class SearchTask extends AsyncTask<Void, Void, String> {

    private static final String SEARCH_URL = "https://duckduckgo.com/?q=";
    private String searchTerm;
    private OnTaskFinish onTaskFinish;
    private Context context;

    public interface OnTaskFinish {
        void onTaskFinish(String finalHtmlCode);
    }

    SearchTask(String searchTerm, Context context) {
        this.searchTerm = searchTerm;
        this.context = context;
    }

    public void setOnTaskFinish(OnTaskFinish onTaskFinish) {
        this.onTaskFinish = onTaskFinish;
    }

    @Override
    protected String doInBackground(Void... voids) {
        searchTerm = searchTerm.replace(" ", "+");
        return HttpParser.get(SEARCH_URL + searchTerm);
    }

    @Override
    protected void onPostExecute(String s) {
        onTaskFinish = (OnTaskFinish) context;
        onTaskFinish.onTaskFinish(s);
        super.onPostExecute(s);
    }
}
