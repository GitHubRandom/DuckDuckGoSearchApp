package io.duckduckgosearch.app;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchTask extends AsyncTask<Void, Void, String> {

    private static final String SEARCH_URL = "https://duckduckgo.com/?q=";
    private static final String SEARCH_URL_END = "&ia=web";
    private String searchTerm;
    private Context context;

    public interface OnTaskFinish {
        void onTaskFinish(String finalHtmlCode);
    }

    SearchTask(String searchTerm, Context context) {
        this.searchTerm = searchTerm;
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... voids) {
        searchTerm = searchTerm.replace(" ", "+");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SEARCH_URL + searchTerm + SEARCH_URL_END)
                .header("User-Agent", "Mozilla/5.0 (Android 4.4; Mobile; rv:41.0) Gecko/41.0 Firefox/41.0")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.body() == null) {
                return "";
            }
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        OnTaskFinish onTaskFinish = (OnTaskFinish) context;
        onTaskFinish.onTaskFinish(s);
        super.onPostExecute(s);
    }
}
