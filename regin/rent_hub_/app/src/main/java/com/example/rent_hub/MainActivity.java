package com.example.rent_hub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.rent_hub.api.HttpHelper;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set up click listeners for cards
        CardView managePropertiesCard = findViewById(R.id.managePropertiesCard);
        CardView addPropertyCard = findViewById(R.id.addPropertyCard);
        CardView logoutCard = findViewById(R.id.logoutCard);

        managePropertiesCard.setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, PropertyListActivity.class)));

        addPropertyCard.setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, AddPropertyActivity.class)));

        logoutCard.setOnClickListener(v -> logout());
    }

    private void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}