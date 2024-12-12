package com.example.aleszspetopiaapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.aleszspetopiaapp.adapters.PetAdapter;
import com.example.aleszspetopiaapp.models.Pet;
import com.example.aleszspetopiaapp.utils.HttpHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PetAdapter.OnPetClickListener {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private PetAdapter petAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddPet;
    private Toolbar toolbar;
    private TextView textViewEmpty;
    private static final String PREFS_NAME = "PetopiaPrefs";
    private int userId = -1;
    private static final int ADD_PET_REQUEST = 1;
    private static final int EDIT_PET_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewPets);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabAddPet = findViewById(R.id.fabAddPet);
        toolbar = findViewById(R.id.toolbar);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        // Set up toolbar
        setSupportActionBar(toolbar);

        // Check if user is logged in
        if (!checkSession()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        petAdapter = new PetAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(petAdapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadPets);
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );

        // Set up FAB
        fabAddPet.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPetActivity.class);
            startActivityForResult(intent, ADD_PET_REQUEST);
        });

        // Load pets
        loadPets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear shared preferences
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Return to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadPets() {
        swipeRefreshLayout.setRefreshing(true);
        
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpHelper.makeRequest("get_pets.php", "POST", requestBody, new HttpHelper.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    
                    if (success) {
                        JSONArray petsArray = jsonResponse.getJSONArray("pets");
                        List<Pet> pets = new ArrayList<>();
                        
                        for (int i = 0; i < petsArray.length(); i++) {
                            JSONObject petObject = petsArray.getJSONObject(i);
                            Pet pet = new Pet(
                                petObject.getInt("id"),
                                petObject.getInt("user_id"),
                                petObject.getString("name"),
                                petObject.getString("type"),
                                petObject.getInt("age"),
                                petObject.getDouble("price"),
                                petObject.getString("description"),
                                petObject.getString("image_url"),
                                petObject.getString("created_at")
                            );
                            pets.add(pet);
                        }
                        
                        runOnUiThread(() -> {
                            petAdapter.updatePets(pets);
                            swipeRefreshLayout.setRefreshing(false);
                            textViewEmpty.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    } else {
                        String message = jsonResponse.getString("message");
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, R.string.error_loading_pets, 
                            Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        petAdapter = new PetAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(petAdapter);
    }

    private void deletePet(Pet pet) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting pet...");
        progressDialog.show();

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("pet_id", pet.getId());
            requestBody.put("user_id", userId);

            HttpHelper.makeRequest("delete_pet.php", "POST", requestBody, new HttpHelper.Callback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        try {
                            JSONObject response = new JSONObject(result);
                            if (response.getBoolean("success")) {
                                Toast.makeText(MainActivity.this, "Pet deleted successfully", Toast.LENGTH_SHORT).show();
                                loadPets();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to delete pet: " + response.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error processing server response", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Error connecting to server", Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onEditClick(Pet pet) {
        Intent intent = new Intent(this, EditPetActivity.class);
        intent.putExtra("pet", pet);
        startActivityForResult(intent, EDIT_PET_REQUEST);
    }

    @Override
    public void onDeleteClick(Pet pet) {
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("Delete Pet")
            .setMessage("Are you sure you want to delete " + pet.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> deletePet(pet))
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_PET_REQUEST || requestCode == EDIT_PET_REQUEST) 
            && resultCode == RESULT_OK) {
            // Refresh the pet list
            loadPets();
        }
    }

    private boolean checkSession() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        userId = prefs.getInt("user_id", -1);
        String username = prefs.getString("username", "");

        Log.d(TAG, "checkSession - isLoggedIn: " + isLoggedIn + 
            ", userId: " + userId + ", username: " + username);

        return isLoggedIn && userId != -1;
    }

    private void redirectToLogin() {
        Log.d(TAG, "Redirecting to login activity");
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().commit(); // Clear any invalid session data
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkSession()) {
            Log.d(TAG, "No valid session in onResume, redirecting to login");
            redirectToLogin();
        } else {
            loadPets();
        }
    }
}