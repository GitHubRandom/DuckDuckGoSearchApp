package io.duckduckgosearch.app;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class HistoryManager {

    private static final String FILENAME = "history.txt";

    public static void addTerm(String term, Date date, Context context) {
        ArrayList<HistoryItem> storedHistory = getTermsAsArrayList(context);
        Type baseType = new TypeToken<ArrayList<HistoryItem>>() {}.getType();
        Gson gson = new Gson();
        if (storedHistory == null) {
            storedHistory = new ArrayList<>();
        }
        try {
            storedHistory.add(new HistoryItem(term, date));
            FileOutputStream outputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(gson.toJson(storedHistory, baseType).getBytes(Charset.forName("UTF-8")));
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<HistoryItem> getTermsAsArrayList(Context context) {
        ArrayList<HistoryItem> result;
        Type baseType = new TypeToken<ArrayList<HistoryItem>>() {}.getType();
        Gson gson = new Gson();
        try {
            FileInputStream stream = context.openFileInput(FILENAME);
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = gson.fromJson(builder.toString(), baseType);
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] getTermsAsStringArray(Context context) {
        ArrayList<HistoryItem> historyItems = getTermsAsArrayList(context);
        if (historyItems != null && historyItems.size() != 0) {
            ArrayList<String> historyTerms = new ArrayList<>();
            for (int i = 0; i < historyItems.size(); i++) {
                historyTerms.add(historyItems.get(i).getTerm());
            }
            return Arrays.copyOf(historyTerms.toArray(), historyItems.size(), String[].class);
        }
        return null;
    }

    public static void deleteTerm(int position, Context context) {
        ArrayList<HistoryItem> storedHistory = getTermsAsArrayList(context);
        Type baseType = new TypeToken<ArrayList<HistoryItem>>(){}.getType();
        Gson gson = new Gson();
        if (storedHistory == null) {
            storedHistory = new ArrayList<>();
        }
        try {
            storedHistory.remove(position);
            FileOutputStream outputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(gson.toJson(storedHistory, baseType).getBytes(Charset.forName("UTF-8")));
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

}
