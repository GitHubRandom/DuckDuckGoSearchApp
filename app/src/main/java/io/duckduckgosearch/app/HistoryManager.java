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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HistoryManager {

    private static final String FILENAME = "history.txt";

    public static void addTerm(String term, Context context) {
        ArrayList<String> storedHistory = getTermsAsArrayList(context);
        Type baseType = new TypeToken<ArrayList<String>>() {}.getType();
        Gson gson = new Gson();
        if (storedHistory != null) {
            try {
                storedHistory.add(term);
                FileOutputStream outputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                outputStream.write(gson.toJson(storedHistory, baseType).getBytes(Charset.forName("UTF-8")));
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getTermsAsArrayList(Context context) {
        ArrayList<String> result;
        Type baseType = new TypeToken<ArrayList<String>>() {}.getType();
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
