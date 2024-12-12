package com.example.aleszspetopiaapp.utils;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpHelper {
    private static final String BASE_URL = "http://10.0.2.2/petopia/"; // For Android Emulator
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void makeRequest(String endpoint, String method, JSONObject requestBody, Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoInput(true);

                if (requestBody != null) {
                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }

                int responseCode = connection.getResponseCode();
                StringBuilder response = new StringBuilder();
                
                // Read the response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                            responseCode >= 400 
                                ? connection.getErrorStream() 
                                : connection.getInputStream(), 
                            StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                final String responseData = response.toString();
                
                // Try to parse as JSON to validate
                try {
                    new JSONObject(responseData);
                } catch (Exception e) {
                    // If not valid JSON, wrap in a JSON object
                    JSONObject wrappedResponse = new JSONObject();
                    wrappedResponse.put("message", responseData);
                    wrappedResponse.put("success", responseCode == 200);
                    mainHandler.post(() -> callback.onSuccess(wrappedResponse.toString()));
                    return;
                }

                // If we got here, it's valid JSON
                if (responseCode >= 200 && responseCode < 300) {
                    mainHandler.post(() -> callback.onSuccess(responseData));
                } else {
                    mainHandler.post(() -> callback.onError(responseData));
                }

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
