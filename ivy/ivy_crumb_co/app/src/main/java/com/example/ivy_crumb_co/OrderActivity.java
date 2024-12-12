package com.example.ivy_crumb_co;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ivy_crumb_co.OrderItemAdapter;
import com.example.ivy_crumb_co.Order;
import com.example.ivy_crumb_co.OrderItem;
import com.example.ivy_crumb_co.Product;
import com.example.ivy_crumb_co.SessionManager;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity {
    private RecyclerView orderItemsRecyclerView;
    private TextView subtotalText;
    private TextView totalDiscountText;
    private TextView totalText;
    private MaterialButton checkoutButton;
    private NumberFormat currencyFormatter;
    private Order currentOrder;
    private OrderItemAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Your Cart");

        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView);
        subtotalText = findViewById(R.id.subtotalText);
        totalDiscountText = findViewById(R.id.totalDiscountText);
        totalText = findViewById(R.id.totalText);
        checkoutButton = findViewById(R.id.checkoutButton);
        Button orderHistoryButton = findViewById(R.id.orderHistoryButton);

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));

        currentOrder = (Order) getIntent().getSerializableExtra("order");
        if (currentOrder == null) {
            currentOrder = new Order();
            Toast.makeText(this, "New order created", Toast.LENGTH_SHORT).show();
        }

        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter(currentOrder);
        orderItemsRecyclerView.setAdapter(adapter);

        adapter.setItemUpdateListener(new OrderItemAdapter.ItemUpdateListener() {
            @Override
            public void onItemUpdated() {
                updateOrderSummary();
            }

            @Override
            public void onOrderUpdated(Order order) {
                updateOrderSummary();
                if (order.getOrderItems().isEmpty()) {
                    adapter.updateOrder(order); // Clear RecyclerView properly
                }
            }
        });

        checkoutButton.setOnClickListener(v -> submitOrder());

        sessionManager = new SessionManager(this);
        if (sessionManager == null) {
            Toast.makeText(this, "Session Manager initialization failed", Toast.LENGTH_SHORT).show();
        }

        orderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrderActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        updateOrderSummary();
    }

    private void updateOrderSummary() {
        subtotalText.setText(String.format("Subtotal: %s", currencyFormatter.format(currentOrder.getSubtotal())));

        if (currentOrder.getDiscount() > 0) {
            totalDiscountText.setVisibility(View.VISIBLE);
            totalDiscountText.setText(String.format("Discount: -%s", currencyFormatter.format(currentOrder.getDiscount())));
        } else {
            totalDiscountText.setVisibility(View.GONE);
        }

        totalText.setText(String.format("Total: %s", currencyFormatter.format(currentOrder.getTotal())));
    }

    private void submitOrder() {
        if (currentOrder.getOrderItems().isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create JSON object for order
            JSONObject orderJson = new JSONObject();
            orderJson.put("username", sessionManager.getUsername());
            orderJson.put("subtotal", currentOrder.getSubtotal());
            orderJson.put("discount", currentOrder.getDiscount());
            orderJson.put("total", currentOrder.getTotal());

            JSONArray itemsArray = new JSONArray();
            for (OrderItem item : currentOrder.getOrderItems()) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("productName", item.getProduct().getName());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getProduct().getPrice());
                itemJson.put("subtotal", item.getSubtotal());
                itemJson.put("discount", item.getDiscount());
                itemJson.put("total", item.getTotal());
                itemsArray.put(itemJson);
            }
            orderJson.put("items", itemsArray);

            // Make network request
            String url = getString(R.string.base_url) + "/submit_order.php";
            
            // Create custom request with raw JSON string
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                orderJson,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(OrderActivity.this, "Order submitted successfully", Toast.LENGTH_SHORT).show();
                            // Navigate to OrderHistory
                            Intent intent = new Intent(OrderActivity.this, OrderHistoryActivity.class);
                            startActivity(intent);
                            finish();
                            // Clear the order
                            currentOrder = new Order(sessionManager.getUsername());
                            adapter.updateOrder(currentOrder);
                            updateOrderSummary();
                            // Return to products
                            returnResult();
                        } else {
                            Toast.makeText(OrderActivity.this, "Failed to submit order: " + response.getString("message"), 
                                Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(OrderActivity.this, "Error processing response: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    String message = error.getMessage();
                    if (error.networkResponse != null) {
                        message += " Status: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(OrderActivity.this, "Network error: " + message, 
                        Toast.LENGTH_LONG).show();
                }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            // Add request to queue
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating order data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        returnResult();
        super.onBackPressed(); // Ensure proper back navigation
    }

    private void returnResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("order", currentOrder);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            returnResult();
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addProductToOrder(Product newProduct, int quantity) {
        currentOrder.addItem(newProduct, quantity);
        adapter.updateOrder(currentOrder);
        adapter.notifyItemInserted(currentOrder.getOrderItems().size() - 1); // Notify item inserted
    }
}