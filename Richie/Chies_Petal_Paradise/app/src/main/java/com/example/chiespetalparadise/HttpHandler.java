package com.example.chiespetalparadise;

import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpHandler {
    private static final String BASE_URL = "http://10.0.2.2:80/ChiesPetalParadise/";
    private static final String TAG = "HttpHandler";

    public static String makePostRequest(String endpoint, JSONObject jsonParams) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            Log.d(TAG, "Sending request to: " + url.toString());
            Log.d(TAG, "Request body: " + jsonParams.toString());

            // Write JSON data
            try (OutputStream os = conn.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.write(jsonParams.toString());
                writer.flush();
            }

            // Check response code
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Response code: " + responseCode);

            // Read the response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            Log.d(TAG, "Response: " + response.toString());
            return response.toString();

        } catch (Exception e) {
            Log.e(TAG, "Error in makePostRequest: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
