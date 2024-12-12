package com.example.ivy_crumb_co;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;
    private List<OrderHistory> orderList;
    private RequestQueue requestQueue;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        sessionManager = new SessionManager(this);
        recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        orderList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(this, orderList);
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);
        fetchOrders();
    }

    private void fetchOrders() {
        String username = sessionManager.getUsername();
        String url = getString(R.string.base_url) + "/get_orders.php?username=" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray ordersArray = response.getJSONArray("orders");
                            orderList.clear();
                            
                            for (int i = 0; i < ordersArray.length(); i++) {
                                JSONObject orderObj = ordersArray.getJSONObject(i);
                                OrderHistory order = new OrderHistory(
                                    orderObj.getInt("order_id"),
                                    orderObj.getString("order_date"),
                                    orderObj.getString("subtotal"),
                                    orderObj.getString("discount"),
                                    orderObj.getString("total"),
                                    orderObj.getString("status")
                                );

                                // Parse items
                                JSONArray itemsArray = orderObj.getJSONArray("items");
                                List<OrderHistoryItem> items = new ArrayList<>();
                                for (int j = 0; j < itemsArray.length(); j++) {
                                    JSONObject itemObj = itemsArray.getJSONObject(j);
                                    OrderHistoryItem item = new OrderHistoryItem(
                                        itemObj.getString("item_name"),
                                        itemObj.getInt("quantity"),
                                        itemObj.getDouble("price"),
                                        itemObj.getDouble("subtotal")
                                    );
                                    items.add(item);
                                }
                                order.setItems(items);
                                orderList.add(order);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error fetching orders", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }
}
