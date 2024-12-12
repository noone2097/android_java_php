package com.example.ivy_crumb_co;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, emailEditText;
    private Button registerButton;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                // Validate input
                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                new RegisterUserTask().execute(username, password, email);
            } else {
                Toast.makeText(RegisterActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class RegisterUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://10.0.2.2/ivy_app/register.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000); // 5 seconds timeout
                conn.setReadTimeout(5000); // 5 seconds timeout

                String postData = "username=" + username + "&password=" + password + "&email=" + email;

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = postData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                return response.toString();

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
                return "error:" + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result != null) {
                    if (result.startsWith("error:")) {
                        Toast.makeText(RegisterActivity.this,
                                "Connection error: " + result.substring(6),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(result);
                    String message = jsonResponse.getString("message");
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                    // If registration is successful, finish the activity
                    if (jsonResponse.has("status") &&
                            jsonResponse.getString("status").equals("success")) {
                        finish();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Network error: No response from server",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                Toast.makeText(RegisterActivity.this,
                        "Error processing response: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}