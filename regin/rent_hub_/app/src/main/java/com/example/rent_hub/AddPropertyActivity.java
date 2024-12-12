package com.example.rent_hub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.rent_hub.api.HttpHelper;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.text.NumberFormat;
import java.util.Locale;

public class AddPropertyActivity extends AppCompatActivity {
    private static final String TAG = "AddPropertyActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;

    private EditText titleInput;
    private EditText addressInput;
    private EditText priceInput;
    private EditText descriptionInput;
    private Spinner statusSpinner;
    private TextView monthlyCalculationText;
    private TextView yearlyCalculationText;
    private ImageView propertyImageView;
    private Button selectImageButton;
    private Button submitButton;
    private Uri selectedImageUri;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                propertyImageView.setImageURI(selectedImageUri);
            }
        }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to access images", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_add_property);

        // Initialize views
        titleInput = findViewById(R.id.titleInput);
        addressInput = findViewById(R.id.addressInput);
        priceInput = findViewById(R.id.priceInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        statusSpinner = findViewById(R.id.statusSpinner);
        monthlyCalculationText = findViewById(R.id.monthlyCalculationText);
        yearlyCalculationText = findViewById(R.id.yearlyCalculationText);
        propertyImageView = findViewById(R.id.propertyImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        submitButton = findViewById(R.id.submitButton);

        // Set up spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.property_status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        selectImageButton.setOnClickListener(v -> checkPermissionAndPickImage());
        submitButton.setOnClickListener(v -> submitProperty());

        // Add text change listener for price calculations
        priceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateRent();
            }
        });
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openImagePicker();
            }
        } else {
            // Below Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private String getBase64FromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        // Compress the bitmap
        int maxWidth = 800;
        int maxHeight = 800;
        float scale = Math.min(((float)maxWidth / bitmap.getWidth()), ((float)maxHeight / bitmap.getHeight()));

        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        outputStream.close();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void calculateRent() {
        try {
            String priceText = priceInput.getText().toString().trim();
            if (priceText.isEmpty()) {
                monthlyCalculationText.setText("Monthly Income: $0.00");
                yearlyCalculationText.setText("Yearly Income: $0.00");
                return;
            }

            double price = Double.parseDouble(priceText);
            double monthlyIncome = price;
            double yearlyIncome = price * 12;

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            monthlyCalculationText.setText("Monthly Income: " + currencyFormat.format(monthlyIncome));
            yearlyCalculationText.setText("Yearly Income: " + currencyFormat.format(yearlyIncome));
        } catch (NumberFormatException e) {
            monthlyCalculationText.setText("Monthly Income: Invalid input");
            yearlyCalculationText.setText("Yearly Income: Invalid input");
        }
    }

    private void submitProperty() {
        // Get user ID from session
        int userId = sessionManager.getUserId();
        Log.d(TAG, "User ID from session: " + userId);

        if (userId == -1) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Validate inputs
        String title = titleInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        Log.d(TAG, "Form values - Title: " + title +
                   ", Address: " + address +
                   ", Price: " + priceStr +
                   ", Description length: " + description.length());

        if (title.isEmpty() || address.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                Toast.makeText(this, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            submitButton.setEnabled(false);
            selectImageButton.setEnabled(false);

            // Get base64 image
            String base64Image = getBase64FromUri(selectedImageUri);
            Log.d(TAG, "Image converted to base64, length: " + base64Image.length());

            // Create JSON request
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("title", title);
            jsonRequest.put("description", description);
            jsonRequest.put("price", price);
            jsonRequest.put("location", address);
            jsonRequest.put("image_url", base64Image);
            jsonRequest.put("owner_id", userId);

            Log.d(TAG, "JSON request created: " + jsonRequest.toString());

            // Send request
            HttpHelper.post("add_property.php", jsonRequest.toString(), new HttpHelper.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "API Response: " + response);
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");
                            Log.d(TAG, "Success: " + success + ", Message: " + message);
                            
                            if (success) {
                                Toast.makeText(AddPropertyActivity.this, 
                                    "Property added successfully", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                submitButton.setEnabled(true);
                                selectImageButton.setEnabled(true);
                                Toast.makeText(AddPropertyActivity.this, 
                                    "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response", e);
                            submitButton.setEnabled(true);
                            selectImageButton.setEnabled(true);
                            Toast.makeText(AddPropertyActivity.this, 
                                "Error parsing response: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "API Error: " + error);
                    runOnUiThread(() -> {
                        submitButton.setEnabled(true);
                        selectImageButton.setEnabled(true);
                        Toast.makeText(AddPropertyActivity.this, 
                            "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error creating property", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            submitButton.setEnabled(true);
            selectImageButton.setEnabled(true);
        }
    }
}
