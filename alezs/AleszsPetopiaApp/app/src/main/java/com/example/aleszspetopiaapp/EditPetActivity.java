package com.example.aleszspetopiaapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aleszspetopiaapp.models.Pet;
import com.example.aleszspetopiaapp.utils.HttpHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditPetActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputEditText editTextPetName, editTextPetType, editTextPetAge, 
                             editTextPetPrice, editTextPetDescription;
    private ImageView imageViewPet;
    private Button buttonChooseImage, buttonUpdatePet;
    private Uri selectedImageUri;
    private Pet pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);

        // Initialize views
        editTextPetName = findViewById(R.id.editTextPetName);
        editTextPetType = findViewById(R.id.editTextPetType);
        editTextPetAge = findViewById(R.id.editTextPetAge);
        editTextPetPrice = findViewById(R.id.editTextPetPrice);
        editTextPetDescription = findViewById(R.id.editTextPetDescription);
        imageViewPet = findViewById(R.id.imageViewPet);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonUpdatePet = findViewById(R.id.buttonUpdatePet);

        // Get pet data from intent
        pet = getIntent().getParcelableExtra("pet");
        if (pet != null) {
            populateFields();
        }

        // Set up button click listeners
        buttonChooseImage.setOnClickListener(v -> openImageChooser());
        buttonUpdatePet.setOnClickListener(v -> updatePet());
    }

    private void populateFields() {
        editTextPetName.setText(pet.getName());
        editTextPetType.setText(pet.getType());
        editTextPetAge.setText(String.valueOf(pet.getAge()));
        editTextPetPrice.setText(String.valueOf(pet.getPrice()));
        editTextPetDescription.setText(pet.getDescription());

        // Load pet image if available
        if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            String imageUrl = pet.getImageUrl();
            if (!imageUrl.startsWith("http")) {
                imageUrl = "http://10.0.2.2/petopia/" + imageUrl;
            }
            Picasso.get()
                   .load(imageUrl)
                   .placeholder(R.drawable.ic_pet_placeholder)
                   .error(R.drawable.ic_pet_placeholder)
                   .into(imageViewPet);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageViewPet.setImageURI(selectedImageUri);
        }
    }

    private void updatePet() {
        String name = editTextPetName.getText().toString().trim();
        String type = editTextPetType.getText().toString().trim();
        String ageStr = editTextPetAge.getText().toString().trim();
        String priceStr = editTextPetPrice.getText().toString().trim();
        String description = editTextPetDescription.getText().toString().trim();

        // Validation
        if (name.isEmpty() || type.isEmpty() || ageStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            double price = Double.parseDouble(priceStr);

            // Create JSON object for the request
            JSONObject requestBody = new JSONObject();
            requestBody.put("id", pet.getId());
            requestBody.put("name", name);
            requestBody.put("type", type);
            requestBody.put("age", age);
            requestBody.put("price", price);
            requestBody.put("description", description);

            // Make API call to update pet
            HttpHelper.makeRequest("update_pet.php", "POST", requestBody, 
                new HttpHelper.Callback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");
                            
                            runOnUiThread(() -> {
                                Toast.makeText(EditPetActivity.this, message, 
                                    Toast.LENGTH_SHORT).show();
                                if (success) {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(EditPetActivity.this, 
                                    "Error updating pet", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(EditPetActivity.this, 
                                "Network error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });

        } catch (Exception e) {
            Toast.makeText(this, "Please enter valid numbers for age and price", 
                Toast.LENGTH_SHORT).show();
        }
    }
}
