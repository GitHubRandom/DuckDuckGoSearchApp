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

public class HistoryManager {

    private static final String FILENAME = "history.txt";

    public static void addTerm(String term, String date, Context context) {
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

}
