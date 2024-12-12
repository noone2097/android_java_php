package com.example.chiespetalparadise;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import org.json.JSONObject;
import java.util.List;

public class FlowerAdapter extends RecyclerView.Adapter<FlowerAdapter.FlowerViewHolder> {
    private List<Flower> flowers;
    private Context context;
    private static final String BASE_URL = "http://10.0.2.2/ChiesPetalParadise/";
    private RequestQueue requestQueue;

    public FlowerAdapter(Context context, List<Flower> flowers) {
        this.context = context;
        this.flowers = flowers;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    @NonNull
    @Override
    public FlowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flower, parent, false);
        return new FlowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlowerViewHolder holder, int position) {
        Flower flower = flowers.get(position);
        holder.flowerName.setText(flower.getName());
        holder.flowerDescription.setText(flower.getDescription());
        holder.flowerPrice.setText(Flower.formatPrice(flower.getPrice()));
        holder.flowerStock.setText("Stock: " + flower.getStock());
        holder.flowerTax.setText(String.format("Tax: %.1f%%", flower.getTaxRate()));

        // Construct the full image URL
        String imageUrl = flower.getImageUrl();
        if (!imageUrl.startsWith("http")) {
            imageUrl = BASE_URL + imageUrl;
        }

        // Load image using Glide
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.flowerImage);

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> showEditDialog(flower));
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(flower));
        holder.itemView.setOnClickListener(v -> showQuantityDialog(flower));
    }

    @Override
    public int getItemCount() {
        return flowers.size();
    }

    private void showEditDialog(Flower flower) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_flower, null);
        
        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        EditText priceInput = dialogView.findViewById(R.id.priceInput);
        EditText stockInput = dialogView.findViewById(R.id.stockInput);
        EditText taxInput = dialogView.findViewById(R.id.taxInput);
        
        // Pre-fill the fields
        nameInput.setText(flower.getName());
        descriptionInput.setText(flower.getDescription());
        priceInput.setText(String.valueOf(flower.getPrice()));
        stockInput.setText(String.valueOf(flower.getStock()));
        taxInput.setText(String.valueOf(flower.getTaxRate()));

        new AlertDialog.Builder(context)
            .setTitle("Edit Flower")
            .setView(dialogView)
            .setPositiveButton("Update", (dialog, which) -> {
                try {
                    String name = nameInput.getText().toString();
                    String description = descriptionInput.getText().toString();
                    double price = Double.parseDouble(priceInput.getText().toString());
                    int stock = Integer.parseInt(stockInput.getText().toString());
                    double taxRate = Double.parseDouble(taxInput.getText().toString());

                    updateFlower(flower.getId(), name, description, price, stock, taxRate);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateFlower(int id, String name, String description, double price, int stock, double taxRate) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id", id);
            jsonBody.put("name", name);
            jsonBody.put("description", description);
            jsonBody.put("price", price);
            jsonBody.put("tax_rate", taxRate);
            jsonBody.put("stock", stock);

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "update_flower.php",
                jsonBody,
                response -> {
                    Toast.makeText(context, "Flower updated successfully", Toast.LENGTH_SHORT).show();
                    // Refresh the list
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).refreshFlowers();
                    }
                },
                error -> Toast.makeText(context, "Error updating flower: " + error.getMessage(), Toast.LENGTH_LONG).show()
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteConfirmation(Flower flower) {
        new AlertDialog.Builder(context)
            .setTitle("Delete Flower")
            .setMessage("Are you sure you want to delete " + flower.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> deleteFlower(flower))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteFlower(Flower flower) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id", flower.getId());

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "delete_flower.php",
                jsonBody,
                response -> {
                    Toast.makeText(context, "Flower deleted successfully", Toast.LENGTH_SHORT).show();
                    // Refresh the list
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).refreshFlowers();
                    }
                },
                error -> Toast.makeText(context, "Error deleting flower: " + error.getMessage(), Toast.LENGTH_LONG).show()
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showQuantityDialog(Flower flower) {
        EditText quantityInput = new EditText(context);
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityInput.setHint("Enter quantity");

        new AlertDialog.Builder(context)
            .setTitle("Calculate Price")
            .setView(quantityInput)
            .setPositiveButton("Calculate", (dialog, which) -> {
                String quantityStr = quantityInput.getText().toString();
                if (!quantityStr.isEmpty()) {
                    int quantity = Integer.parseInt(quantityStr);
                    if (flower.isQuantityAvailable(quantity)) {
                        showPriceBreakdown(flower, quantity);
                    } else {
                        Toast.makeText(context, 
                            "Invalid quantity. Available stock: " + flower.getStock(), 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showPriceBreakdown(Flower flower, int quantity) {
        new AlertDialog.Builder(context)
            .setTitle("Price Breakdown")
            .setMessage(flower.getPriceBreakdown(quantity))
            .setPositiveButton("OK", null)
            .show();
    }

    public void updateFlowers(List<Flower> newFlowers) {
        this.flowers = newFlowers;
        notifyDataSetChanged();
    }

    static class FlowerViewHolder extends RecyclerView.ViewHolder {
        ImageView flowerImage;
        TextView flowerName, flowerDescription, flowerPrice, flowerStock, flowerTax;
        MaterialButton btnEdit, btnDelete;

        public FlowerViewHolder(@NonNull View itemView) {
            super(itemView);
            flowerImage = itemView.findViewById(R.id.flowerImage);
            flowerName = itemView.findViewById(R.id.flowerName);
            flowerDescription = itemView.findViewById(R.id.flowerDescription);
            flowerPrice = itemView.findViewById(R.id.flowerPrice);
            flowerStock = itemView.findViewById(R.id.flowerStock);
            flowerTax = itemView.findViewById(R.id.flowerTax);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
