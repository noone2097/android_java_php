package com.example.ivy_crumb_co;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> products;
    private Order currentOrder;
    private Menu menu;
    private MenuItem cartMenuItem;
    private static final String TAG = "ProductsActivity";
    private ActivityResultLauncher<Intent> orderLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize RecyclerView
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Initialize order launcher
        orderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        currentOrder = (Order) result.getData().getSerializableExtra("order");
                        // Update the adapter with the new order
                        productAdapter.setOrder(currentOrder);
                        updateCartBadge();
                    }
                }
        );

        try {
            // Populate products list
            products = new ArrayList<>();
            products.add(new Product("Sourdough Bread", 200.00, R.drawable.bread_sourdough));
            products.add(new Product("Chocolate Cake", 400.00, R.drawable.cake_chocolate));
            products.add(new Product("Baguette", 250.00, R.drawable.bread_baguette));
            products.add(new Product("Carrot Cake", 400.00, R.drawable.cake_carrot));
            products.add(new Product("Croissant", 150.00, R.drawable.bread_croissant));
            products.add(new Product("Cheesecake", 350.00, R.drawable.cake_cheese));

            // Initialize order
            String username = getIntent().getStringExtra("username");
            if (username == null) {
                Log.e(TAG, "No username provided");
                finish();
                return;
            }
            currentOrder = new Order(username);

            // Set up adapter
            productAdapter = new ProductAdapter(products, currentOrder);
            productAdapter.setOnItemClickListener(() -> {
                Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show();
                updateCartBadge();
            });
            productsRecyclerView.setAdapter(productAdapter);

        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Failed to load products. Please try again.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
        }
    }

    private void updateCartBadge() {
        MenuItem cartItem = menu.findItem(R.id.action_cart);
        if (cartItem != null) {
            // Get the current order from session
            int itemCount = currentOrder != null ? currentOrder.getOrderItems().size() : 0;

            // Get the action view
            View actionView = cartItem.getActionView();
            TextView badgeTextView = actionView.findViewById(R.id.cart_badge);

            if (itemCount > 0) {
                badgeTextView.setVisibility(View.VISIBLE);
                badgeTextView.setText(String.valueOf(itemCount));
            } else {
                badgeTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_products, menu);
        
        // Set up cart menu item with custom layout
        MenuItem cartItem = menu.findItem(R.id.action_cart);
        FrameLayout actionView = (FrameLayout) getLayoutInflater().inflate(R.layout.menu_cart_layout, null);
        cartItem.setActionView(actionView);
        
        actionView.setOnClickListener(v -> {
            Intent intent = new Intent(ProductsActivity.this, OrderActivity.class);
            intent.putExtra("order", currentOrder);
            orderLauncher.launch(intent);
        });

        updateCartBadge();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_view_cart) {
            Intent orderIntent = new Intent(this, OrderActivity.class);
            orderIntent.putExtra("order", currentOrder);
            orderLauncher.launch(orderIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                .setNegativeButton("No", null)
                .show();
    }
}
