package com.example.chiespetalparadise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonParams = new JSONObject();
                            jsonParams.put("email", email);
                            jsonParams.put("password", password);

                            Log.d(TAG, "Sending login request with email: " + email);
                            Log.d(TAG, "Request JSON: " + jsonParams.toString());

                            String response = HttpHandler.makePostRequest("login.php", jsonParams);
                            Log.d(TAG, "Received response: " + response);
                            
                            if (response != null) {
                                JSONObject jsonResponse = new JSONObject(response);
                                final boolean status = jsonResponse.getBoolean("status");
                                final String message = jsonResponse.getString("message");

                                Log.d(TAG, "Login status: " + status + ", message: " + message);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (status) {
                                            // Save user data
                                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            try {
                                                editor.putInt("userId", jsonResponse.getInt("user_id"));
                                                editor.putString("userName", jsonResponse.getString("name"));
                                                editor.apply();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            // Navigate to main activity
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, "Response is null");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "Server error: No response", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            final String errorMessage = e.getMessage();
                            Log.e(TAG, "Login error: " + errorMessage);
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
