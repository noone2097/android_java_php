package com.example.aleszspetopiaapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.aleszspetopiaapp.utils.HttpHelper;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AddPetActivity extends AppCompatActivity {
    private static final String TAG = "AddPetActivity";
    private static final int MAX_IMAGE_DIMENSION = 1024;
    
    private EditText editTextName;
    private EditText editTextType;
    private EditText editTextAge;
    private EditText editTextPrice;
    private EditText editTextDescription;
    private ImageView imageViewPet;
    private Button buttonSelectImage;
    private Button buttonAddPet;
    private Uri selectedImageUri;
    
    private TextInputLayout nameLayout;
    private TextInputLayout typeLayout;
    private TextInputLayout ageLayout;
    private TextInputLayout priceLayout;
    private TextInputLayout descriptionLayout;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                imageViewPet.setImageURI(selectedImageUri);
            }
        }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission required to select images", Toast.LENGTH_LONG).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        // Initialize views
        editTextName = findViewById(R.id.editTextPetName);
        editTextType = findViewById(R.id.editTextPetType);
        editTextAge = findViewById(R.id.editTextPetAge);
        editTextPrice = findViewById(R.id.editTextPetPrice);
        editTextDescription = findViewById(R.id.editTextPetDescription);
        imageViewPet = findViewById(R.id.imageViewPet);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonAddPet = findViewById(R.id.buttonAddPet);

        // Get TextInputLayouts
        nameLayout = findViewById(R.id.textInputLayoutPetName);
        typeLayout = findViewById(R.id.textInputLayoutPetType);
        ageLayout = findViewById(R.id.textInputLayoutPetAge);
        priceLayout = findViewById(R.id.textInputLayoutPetPrice);
        descriptionLayout = findViewById(R.id.textInputLayoutPetDescription);

        buttonSelectImage.setOnClickListener(v -> checkPermissionAndPickImage());
        buttonAddPet.setOnClickListener(v -> validateAndAddPet());
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openImagePicker();
            }
        } else {
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

    private String getBase64FromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI");
                return null;
            }

            // Resize image if too large
            bitmap = resizeBitmapIfNeeded(bitmap);

            // Convert to base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
            
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error getting base64 from URI: " + e.getMessage());
            return null;
        }
    }

    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return bitmap;
        }

        float ratio = Math.min(
            (float) MAX_IMAGE_DIMENSION / width,
            (float) MAX_IMAGE_DIMENSION / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void validateAndAddPet() {
        String name = editTextName.getText().toString().trim();
        String type = editTextType.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Clear previous errors
        nameLayout.setError(null);
        typeLayout.setError(null);
        ageLayout.setError(null);
        priceLayout.setError(null);
        descriptionLayout.setError(null);

        // Validate fields
        boolean isValid = true;

        if (name.isEmpty()) {
            nameLayout.setError("Pet name is required");
            isValid = false;
        }

        if (type.isEmpty()) {
            typeLayout.setError("Pet type is required");
            isValid = false;
        }

        if (ageStr.isEmpty()) {
            ageLayout.setError("Age is required");
            isValid = false;
        }

        if (priceStr.isEmpty()) {
            priceLayout.setError("Price is required");
            isValid = false;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Get user_id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PetopiaPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        
        Log.d(TAG, "Retrieved user_id: " + userId);
        Log.d(TAG, "IsLoggedIn: " + isLoggedIn);

        if (userId == -1 || !isLoggedIn) {
            Toast.makeText(this, "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Convert image to base64
        String base64Image = getBase64FromUri(selectedImageUri);
        if (base64Image == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("user_id", userId);
            requestBody.put("name", name);
            requestBody.put("type", type);
            requestBody.put("age", Integer.parseInt(ageStr));
            requestBody.put("price", Double.parseDouble(priceStr));
            requestBody.put("description", description);
            requestBody.put("image", base64Image);

            buttonAddPet.setEnabled(false);
            buttonAddPet.setText("Adding Pet...");

            HttpHelper.makeRequest("add_pet.php", "POST", requestBody, new HttpHelper.Callback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONObject response = new JSONObject(result);
                        boolean success = response.optBoolean("success", false);
                        String message = response.optString("message", "Unknown error");

                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(AddPetActivity.this, "Pet added successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Return to previous screen
                            } else {
                                Toast.makeText(AddPetActivity.this, message, Toast.LENGTH_LONG).show();
                                buttonAddPet.setEnabled(true);
                                buttonAddPet.setText("Add Pet");
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(AddPetActivity.this, "Error adding pet", Toast.LENGTH_SHORT).show();
                            buttonAddPet.setEnabled(true);
                            buttonAddPet.setText("Add Pet");
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Network error: " + error);
                    runOnUiThread(() -> {
                        Toast.makeText(AddPetActivity.this, "Network error: " + error, Toast.LENGTH_LONG).show();
                        buttonAddPet.setEnabled(true);
                        buttonAddPet.setText("Add Pet");
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating request: " + e.getMessage());
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            buttonAddPet.setEnabled(true);
            buttonAddPet.setText("Add Pet");
        }
    }
}
