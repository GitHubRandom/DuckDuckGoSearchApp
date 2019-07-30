package io.duckduckgosearch.app;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class OnlineACParser extends AsyncTask<String, Void, ArrayList<String>> {

    private static final String ACUrl = "https://duckduckgo.com/ac/?q=";
    private OnParsed onParsed;
    public interface OnParsed {
        void onParsed(ArrayList<String> list);
    }
    private String[] filterResults;

    public OnlineACParser(String[] filterResults) {
        this.filterResults = filterResults;
    }

    void setOnParseListener(OnParsed onParsed) {
        this.onParsed = onParsed;
    }

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        return getJSONAC(strings[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
        onParsed.onParsed(strings);
    }

    ArrayList<String> getJSONAC(String term) {
        ArrayList<String> finalResponse = new ArrayList<>();
        if (term == null || term.equals("")) {
            return null;
        }
        String JSONString;
        try {
            JSONString = getJSON(ACUrl + URLEncoder.encode(term.trim(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        JSONArray suggestions;
        try {
            suggestions = new JSONArray(JSONString);
            for (int i = 0; i < suggestions.length(); i++) {
                boolean isInHistory = false;
                if (filterResults != null) {
                    for (String filterResult : filterResults) {
                        if (filterResult.equals(suggestions.getJSONObject(i).getString("phrase"))) {
                            isInHistory = true;
                        }
                    }
                }
                if (!isInHistory) {
                    finalResponse.add(suggestions.getJSONObject(i).getString("phrase"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return finalResponse;
    }

    private String getJSON(final String baseUrl) {
        String response = null;
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection urlConnection;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            response = streamToStr(inputStream);
        } catch (MalformedURLException e) {
            Log.e("Error", "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e("Error", "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e("Error", "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e("Error", "Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    private String streamToStr(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }

}
