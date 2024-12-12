package com.example.rent_hub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rent_hub.api.HttpHelper;
import org.json.JSONObject;

public class EditPropertyActivity extends AppCompatActivity {
    private EditText titleInput;
    private EditText locationInput;
    private EditText priceInput;
    private EditText descriptionInput;
    private Button submitButton;
    private int propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_property);

        // Initialize views
        titleInput = findViewById(R.id.titleInput);
        locationInput = findViewById(R.id.addressInput);
        priceInput = findViewById(R.id.priceInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        submitButton = findViewById(R.id.submitButton);

        // Get property data from intent
        propertyId = getIntent().getIntExtra("property_id", -1);
        String title = getIntent().getStringExtra("title");
        String location = getIntent().getStringExtra("location");
        double price = getIntent().getDoubleExtra("price", 0.0);
        String description = getIntent().getStringExtra("description");

        // Pre-fill the fields
        titleInput.setText(title);
        locationInput.setText(location);
        priceInput.setText(String.valueOf(price));
        descriptionInput.setText(description);

        submitButton.setOnClickListener(v -> updateProperty());
    }

    private void updateProperty() {
        // Validate inputs
        String title = titleInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (title.isEmpty() || location.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            submitButton.setEnabled(false);

            // Create JSON request
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("id", propertyId);
            jsonRequest.put("title", title);
            jsonRequest.put("location", location);
            jsonRequest.put("price", price);
            jsonRequest.put("description", description);

            // Send PUT request
            HttpHelper.put("update_property.php?id=" + propertyId, jsonRequest.toString(), new HttpHelper.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditPropertyActivity.this, 
                            "Property updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        submitButton.setEnabled(true);
                        Toast.makeText(EditPropertyActivity.this, 
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
