package com.example.aleszspetopiaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aleszspetopiaapp.utils.HttpHelper;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;

    // Password validation pattern
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^" +           // start of string
        "(?=.*[0-9])" + // at least 1 digit
        "(?=\\S+$)" +   // no whitespace
        ".{6,}" +       // at least 6 characters
        "$"             // end of string
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        
        // Get TextInputLayouts for showing errors
        usernameLayout = (TextInputLayout) editTextUsername.getParent().getParent();
        passwordLayout = (TextInputLayout) editTextPassword.getParent().getParent();
        confirmPasswordLayout = (TextInputLayout) editTextConfirmPassword.getParent().getParent();

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

        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set click listeners
        buttonRegister.setOnClickListener(v -> attemptRegister());
        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateUsername(String username) {
        if (username.trim().isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            return false;
        }
        if (username.length() < 3) {
            usernameLayout.setError("Username must be at least 3 characters");
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
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            passwordLayout.setError("Password must be at least 6 characters and contain numbers");
            return false;
        }
        passwordLayout.setError(null);
        return true;
    }

    private boolean validateConfirmPassword(String confirmPassword) {
        String password = editTextPassword.getText().toString();
        if (!confirmPassword.equals(password)) {
            confirmPasswordLayout.setError("Passwords do not match");
            return false;
        }
        confirmPasswordLayout.setError(null);
        return true;
    }

    private void attemptRegister() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate all fields
        boolean isUsernameValid = validateUsername(username);
        boolean isPasswordValid = validatePassword(password);
        boolean isConfirmPasswordValid = validateConfirmPassword(confirmPassword);

        if (!isUsernameValid || !isPasswordValid || !isConfirmPasswordValid) {
            return;
        }

        // Create JSON request body
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);

            // Make API call
            HttpHelper.makeRequest("register.php", "POST", requestBody, new HttpHelper.Callback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONObject response = new JSONObject(result);
                        String message = response.optString("message", "Unknown error");
                        boolean success = response.optBoolean("success", false);
                        
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                // Navigate to login screen
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Check for specific error messages
                                if (message.toLowerCase().contains("username") && 
                                    message.toLowerCase().contains("exist")) {
                                    usernameLayout.setError("Username already exists");
                                } else {
                                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, 
                                "Server error: Please try again later", 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Network error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
