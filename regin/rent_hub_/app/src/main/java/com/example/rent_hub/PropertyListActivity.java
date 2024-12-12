package com.example.rent_hub;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rent_hub.api.HttpHelper;
import com.example.rent_hub.models.Property;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PropertyListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private List<Property> properties = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_property_list);

        sessionManager = new SessionManager(this);

        recyclerView = findViewById(R.id.propertyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PropertyAdapter(properties, this::onEditProperty, this::onDeleteProperty, this::onToggleStatus);
        recyclerView.setAdapter(adapter);

        FloatingActionButton addButton = findViewById(R.id.addPropertyButton);
        addButton.setOnClickListener(v -> startActivity(new Intent(this, AddPropertyActivity.class)));

        loadProperties();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProperties();
    }

    private void loadProperties() {
        // Get user ID from session
        int userId = sessionManager.getUserId();
        
        if (userId == -1) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        HttpHelper.get("properties.php?owner_id=" + userId, new HttpHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    properties.clear();
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Property property = new Property();
                        property.setId(json.getInt("id"));
                        property.setTitle(json.getString("title"));
                        property.setAddress(json.getString("location")); 
                        property.setPrice(json.getDouble("price"));
                        property.setDescription(json.getString("description"));
                        property.setOwnerId(json.getInt("owner_id"));
                        property.setCreatedAt(json.optString("created_at", ""));
                        property.setImageUrl(json.optString("image_url", ""));
                        property.setStatus(json.optString("status", ""));
                        properties.add(property);
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(PropertyListActivity.this, 
                            "Error parsing properties: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(PropertyListActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void onEditProperty(Property property) {
        Intent intent = new Intent(this, EditPropertyActivity.class);
        intent.putExtra("property_id", property.getId());
        intent.putExtra("title", property.getTitle());
        intent.putExtra("location", property.getAddress());
        intent.putExtra("price", property.getPrice());
        intent.putExtra("description", property.getDescription());
        startActivity(intent);
    }

    private void onDeleteProperty(Property property) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Property")
            .setMessage("Are you sure you want to delete this property?")
            .setPositiveButton("Yes", (dialog, which) -> deleteProperty(property))
            .setNegativeButton("No", null)
            .show();
    }

    private void deleteProperty(Property property) {
        String endpoint = "properties.php?id=" + property.getId();
        HttpHelper.delete(endpoint, new HttpHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(PropertyListActivity.this, 
                    "Property deleted successfully", 
                    Toast.LENGTH_SHORT).show();
                loadProperties();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PropertyListActivity.this, 
                    "Error deleting property: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onToggleStatus(Property property) {
        String newStatus = property.isRented() ? "available" : "rented";
        
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("property_id", property.getId());
            jsonBody.put("status", newStatus);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return;
        }

        HttpHelper.post("update_property_status.php", jsonBody.toString(), new HttpHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    Toast.makeText(PropertyListActivity.this, 
                        "Status updated successfully", Toast.LENGTH_SHORT).show();
                    loadProperties(); // Reload the list
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(PropertyListActivity.this, 
                        "Error updating status: " + error, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_property_list, menu);
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
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
