package com.example.aleszspetopiaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aleszspetopiaapp.utils.HttpHelper;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Get TextInputLayouts for showing errors
        usernameLayout = (TextInputLayout) editTextUsername.getParent().getParent();
        passwordLayout = (TextInputLayout) editTextPassword.getParent().getParent();

        // Add text change listeners for real-time validation
        editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonLogin.setOnClickListener(v -> attemptLogin());
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Check if user is already logged in
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("PetopiaPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        int userId = prefs.getInt("user_id", -1);
        String username = prefs.getString("username", "");
        
        Log.d(TAG, "checkLoginStatus - isLoggedIn: " + isLoggedIn + ", userId: " + userId + ", username: " + username);
        
        if (isLoggedIn && userId != -1) {
            // User is already logged in, go to MainActivity
            Log.d(TAG, "User is logged in, redirecting to MainActivity");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean validateUsername(String username) {
        if (username.trim().isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            return false;
        }
        usernameLayout.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (password.trim().isEmpty()) {
            passwordLayout.setError("Password cannot be empty");
            return false;
        }
        passwordLayout.setError(null);
        return true;
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        passwordLayout.setError(null);
    }

    private void attemptLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Clear any previous errors
        clearErrors();

        // Validate input
        boolean isUsernameValid = validateUsername(username);
        boolean isPasswordValid = validatePassword(password);

        if (!isUsernameValid || !isPasswordValid) {
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);

            Log.d(TAG, "Attempting login with username: " + username);
            buttonLogin.setEnabled(false);
            buttonLogin.setText("Logging in...");

            HttpHelper.makeRequest("login.php", "POST", requestBody, new HttpHelper.Callback() {
                @Override
                public void onSuccess(String result) {
                    Log.d(TAG, "Login response: " + result);
                    try {
                        JSONObject response = new JSONObject(result);
                        boolean success = response.optBoolean("success", false);
                        String message = response.optString("message", "Unknown error occurred");

                        runOnUiThread(() -> {
                            buttonLogin.setEnabled(true);
                            buttonLogin.setText("Login");

                            if (success) {
                                // Store user session data
                                SharedPreferences prefs = getSharedPreferences("PetopiaPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                
                                // Log the raw response
                                Log.d(TAG, "Raw server response: " + result);
                                
                                int userId = response.optInt("user_id", -1);
                                String responseUsername = response.optString("username", "");
                                
                                Log.d(TAG, "Extracted from response - userId: " + userId + 
                                    ", username: " + responseUsername);
                                
                                if (userId == -1) {
                                    Log.e(TAG, "Invalid user_id received from server");
                                    Toast.makeText(LoginActivity.this, 
                                        "Server error: Invalid user ID", 
                                        Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Clear any existing data first
                                editor.clear().commit();
                                
                                Log.d(TAG, "Cleared existing session data");
                                
                                // Save new session data
                                editor.putInt("user_id", userId);
                                editor.putString("username", responseUsername);
                                editor.putBoolean("isLoggedIn", true);
                                
                                // Commit changes and verify
                                boolean committed = editor.commit();
                                
                                Log.d(TAG, "Attempted to save - commit result: " + committed);
                                
                                if (!committed) {
                                    Log.e(TAG, "Failed to commit SharedPreferences changes");
                                    Toast.makeText(LoginActivity.this, 
                                        "Error saving session data", 
                                        Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Verify the data was saved
                                SharedPreferences verifyPrefs = getSharedPreferences("PetopiaPrefs", MODE_PRIVATE);
                                boolean isLoggedIn = verifyPrefs.getBoolean("isLoggedIn", false);
                                int savedUserId = verifyPrefs.getInt("user_id", -1);
                                String savedUsername = verifyPrefs.getString("username", "");
                                
                                Log.d(TAG, "Verification check:" +
                                    "\nExpected userId: " + userId + ", got: " + savedUserId +
                                    "\nExpected username: '" + responseUsername + "', got: '" + savedUsername + "'" +
                                    "\nExpected isLoggedIn: true, got: " + isLoggedIn);

                                boolean userIdMatch = savedUserId == userId;
                                boolean usernameMatch = responseUsername.equals(savedUsername);
                                
                                Log.d(TAG, "Match results:" +
                                    "\nuserId match: " + userIdMatch +
                                    "\nusername match: " + usernameMatch +
                                    "\nisLoggedIn: " + isLoggedIn);

                                if (userIdMatch && isLoggedIn && usernameMatch) {
                                    // Session data verified, start MainActivity
                                    Log.d(TAG, "Session verified successfully, starting MainActivity");
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.e(TAG, "Session verification failed! Data mismatch:" +
                                        "\nuserId match: " + userIdMatch +
                                        "\nusername match: " + usernameMatch +
                                        "\nisLoggedIn: " + isLoggedIn);
                                    Toast.makeText(LoginActivity.this, 
                                        "Error saving session data", 
                                        Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Log.d(TAG, "Login failed: " + message);
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing login response: " + e.getMessage());
                        runOnUiThread(() -> {
                            buttonLogin.setEnabled(true);
                            buttonLogin.setText("Login");
                            Toast.makeText(LoginActivity.this,
                                "Error parsing response: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Network error: " + error);
                    runOnUiThread(() -> {
                        buttonLogin.setEnabled(true);
                        buttonLogin.setText("Login");
                        Toast.makeText(LoginActivity.this,
                            "Network error: Please check your connection",
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating request: " + e.getMessage());
            buttonLogin.setEnabled(true);
            buttonLogin.setText("Login");
            Toast.makeText(this, "Error creating request: Please try again",
                Toast.LENGTH_SHORT).show();
        }
    }
}
