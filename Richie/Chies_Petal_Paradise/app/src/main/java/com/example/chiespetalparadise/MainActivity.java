package com.example.chiespetalparadise;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView flowersRecyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private FlowerAdapter flowerAdapter;
    private List<Flower> flowerList;
    private ImageView selectedImageView;
    private String selectedImageBase64;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        if (selectedImageView != null) {
                            selectedImageView.setImageBitmap(bitmap);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                selectedImageBase64 = bitmapToBase64(bitmap);
                            }
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        // Setup permission launcher
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
                }
            }
        );

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        flowersRecyclerView = findViewById(R.id.flowersRecyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        FloatingActionButton fabAddFlower = findViewById(R.id.fabAddFlower);

        // Initialize RecyclerView and adapter
        flowerList = new ArrayList<>();
        flowerAdapter = new FlowerAdapter(this, flowerList);
        flowersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        flowersRecyclerView.setAdapter(flowerAdapter);

        // Setup SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadFlowers);

        // Setup FAB
        fabAddFlower.setOnClickListener(v -> showAddFlowerDialog());

        // Load flowers
        loadFlowers();
    }

    private void showAddFlowerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_flower, null);
        
        selectedImageView = dialogView.findViewById(R.id.imageFlower);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        EditText editName = dialogView.findViewById(R.id.editFlowerName);
        EditText editDescription = dialogView.findViewById(R.id.editFlowerDescription);
        EditText editPrice = dialogView.findViewById(R.id.editFlowerPrice);
        EditText editStock = dialogView.findViewById(R.id.editFlowerStock);

        btnSelectImage.setOnClickListener(v -> checkAndRequestPermissions());

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Add New Flower")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = editName.getText().toString().trim();
                String description = editDescription.getText().toString().trim();
                String priceStr = editPrice.getText().toString().trim();
                String stockStr = editStock.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    editName.setError("Name is required");
                    return;
                }
                if (TextUtils.isEmpty(priceStr)) {
                    editPrice.setError("Price is required");
                    return;
                }
                if (TextUtils.isEmpty(stockStr)) {
                    editStock.setError("Stock is required");
                    return;
                }
                if (selectedImageBase64 == null) {
                    Toast.makeText(MainActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double price = Double.parseDouble(priceStr);
                    int stock = Integer.parseInt(stockStr);
                    addFlower(name, description, price, selectedImageBase64, stock);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Invalid price or stock value", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.setOnDismissListener(dialogInterface -> {
            selectedImageView = null;
            selectedImageBase64 = null;
        });

        dialog.show();
    }

    private void checkAndRequestPermissions() {
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

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
    }

    private void addFlower(String name, String description, double price, String imageBase64, int stock) {
        new Thread(() -> {
            try {
                JSONObject flowerData = new JSONObject();
                flowerData.put("name", name);
                flowerData.put("description", description);
                flowerData.put("price", price);
                flowerData.put("image_data", imageBase64);
                flowerData.put("stock", stock);

                String response = HttpHandler.makePostRequest("add_flower.php", flowerData);
                
                if (response != null) {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("status")) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Flower added successfully", Toast.LENGTH_SHORT).show();
                            loadFlowers();
                        });
                    } else {
                        showError("Error: " + jsonResponse.getString("message"));
                    }
                } else {
                    showError("Error adding flower");
                }
            } catch (Exception e) {
                showError("Error: " + e.getMessage());
            }
        }).start();
    }

    private void loadFlowers() {
        new Thread(() -> {
            try {
                String response = HttpHandler.makePostRequest("get_flowers.php", new JSONObject());
                
                if (response != null) {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("status")) {
                        JSONArray flowersArray = jsonResponse.getJSONArray("flowers");
                        List<Flower> newFlowers = new ArrayList<>();
                        
                        for (int i = 0; i < flowersArray.length(); i++) {
                            JSONObject jsonFlower = flowersArray.getJSONObject(i);
                            Flower flower = new Flower(
                                jsonFlower.getInt("id"),
                                jsonFlower.getString("name"),
                                jsonFlower.getString("description"),
                                jsonFlower.getDouble("price"),
                                jsonFlower.optDouble("tax_rate", 12.00), // Default to 12% if not found
                                jsonFlower.getString("image_url"),
                                jsonFlower.getInt("stock"),
                                jsonFlower.getString("created_at")
                            );
                            newFlowers.add(flower);
                        }

                        runOnUiThread(() -> {
                            flowerList.clear();
                            flowerList.addAll(newFlowers);
                            flowerAdapter.notifyDataSetChanged();
                            swipeRefresh.setRefreshing(false);
                        });
                    } else {
                        showError("Error: " + jsonResponse.getString("message"));
                    }
                } else {
                    showError("Error loading flowers");
                }
            } catch (Exception e) {
                showError("Error: " + e.getMessage());
            }
        }).start();
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
        });
    }

    public void refreshFlowers() {
        loadFlowers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Clear user data
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Go to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}