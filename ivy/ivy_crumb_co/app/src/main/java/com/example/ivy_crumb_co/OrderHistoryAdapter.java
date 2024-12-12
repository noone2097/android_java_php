package com.example.ivy_crumb_co;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
    private List<OrderHistory> orderList;
    private Context context;
    private RequestQueue requestQueue;
    private NumberFormat currencyFormatter;

    public OrderHistoryAdapter(Context context, List<OrderHistory> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.requestQueue = Volley.newRequestQueue(context);
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderHistory order = orderList.get(position);
        holder.orderIdTextView.setText(String.valueOf(order.getOrderId()));
        holder.dateTextView.setText(order.getOrderDate());
        holder.totalTextView.setText(order.getTotal());
        
        // Set status and button visibility
        String status = order.getStatus();
        holder.statusTextView.setText("Status: " + status);
        
        if (status.equals("pending")) {
            holder.statusTextView.setTextColor(Color.parseColor("#FFA500")); // Orange
            holder.payButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.payButton.setOnClickListener(v -> updatePaymentStatus(order, holder));
        } else if (status.equals("paid")) {
            holder.statusTextView.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.payButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> confirmDelete(order));
        }

        // Add order items
        holder.itemsContainer.removeAllViews();
        if (order.getItems() != null) {
            for (OrderHistoryItem item : order.getItems()) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_order_product, holder.itemsContainer, false);
                
                TextView productNameTextView = itemView.findViewById(R.id.productNameTextView);
                TextView quantityTextView = itemView.findViewById(R.id.quantityTextView);
                TextView priceTextView = itemView.findViewById(R.id.priceTextView);

                productNameTextView.setText(item.getItemName());
                quantityTextView.setText("x" + item.getQuantity());
                priceTextView.setText(currencyFormatter.format(item.getPrice()));

                holder.itemsContainer.addView(itemView);
            }
        }
    }

    private void confirmDelete(OrderHistory order) {
        new AlertDialog.Builder(context)
            .setTitle("Delete Order")
            .setMessage("Are you sure you want to delete this order?")
            .setPositiveButton("Delete", (dialog, which) -> deleteOrder(order))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteOrder(OrderHistory order) {
        String url = context.getString(R.string.base_url) + "/delete_order.php";
        
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("order_id", order.getOrderId());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            // Remove the order from the list
                            int position = orderList.indexOf(order);
                            orderList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Order deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error deleting order", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, "Error connecting to server", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void updatePaymentStatus(OrderHistory order, OrderViewHolder holder) {
        String url = context.getString(R.string.base_url) + "/update_payment_status.php";
        
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("order_id", order.getOrderId());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            // Update the order status locally
                            order.setStatus("paid");
                            notifyItemChanged(orderList.indexOf(order));
                            Toast.makeText(context, "Payment status updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error updating payment status", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, "Error connecting to server", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView;
        TextView dateTextView;
        TextView totalTextView;
        TextView statusTextView;
        MaterialButton payButton;
        MaterialButton deleteButton;
        LinearLayout itemsContainer;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            totalTextView = itemView.findViewById(R.id.totalTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            payButton = itemView.findViewById(R.id.payButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            itemsContainer = itemView.findViewById(R.id.itemsContainer);
        }
    }
}
