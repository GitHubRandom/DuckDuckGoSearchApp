package io.duckduckgosearch.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpParser {

    static String get(String urlAddress) {

        String result;
        try {
            URL url = new URL(urlAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                result = builder.toString();
            } else {
                result = "error: connection error (" + responseCode + ")";
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = "error : malformed url exception";
        } catch (IOException e) {
            e.printStackTrace();
            result = "error : io exception";
        }

        return result;
    }

}
