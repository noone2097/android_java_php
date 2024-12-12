package com.example.rent_hub.api;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {
    private static final String BASE_URL = "http://10.0.2.2/rent_hub/api/";
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void get(String endpoint, ApiCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Read response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                final String responseData = response.toString();
                mainHandler.post(() -> callback.onSuccess(responseData));

            } catch (final Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    public static void post(String endpoint, String postData, ApiCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Write request data
                OutputStream os = conn.getOutputStream();
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();
                os.close();

                // Read response
                int responseCode = conn.getResponseCode();
                BufferedReader br;
                if (responseCode >= 200 && responseCode <= 299) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                }
                
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                final String responseData = response.toString();
                if (responseCode >= 200 && responseCode <= 299) {
                    mainHandler.post(() -> callback.onSuccess(responseData));
                } else {
                    mainHandler.post(() -> callback.onError(responseData));
                }

            } catch (final Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    public static void delete(String endpoint, ApiCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                // Read response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                final String responseData = response.toString();
                mainHandler.post(() -> callback.onSuccess(responseData));

            } catch (final Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    public static void put(String endpoint, String putData, ApiCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Write request data
                OutputStream os = conn.getOutputStream();
                os.write(putData.getBytes());
                os.flush();
                os.close();

                // Read response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                final String responseData = response.toString();
                mainHandler.post(() -> callback.onSuccess(responseData));

            } catch (final Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
}
