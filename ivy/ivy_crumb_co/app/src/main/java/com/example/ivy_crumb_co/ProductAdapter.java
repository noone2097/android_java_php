package com.example.ivy_crumb_co;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private NumberFormat currencyFormatter;
    private Order currentOrder;
    private OnItemClickListener itemClickListener;

    public ProductAdapter(List<Product> products, Order currentOrder) {
        this.products = products;
        this.currentOrder = currentOrder;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productName.setText(product.getName());
        holder.productPrice.setText(currencyFormatter.format(product.getPrice()));
        holder.productImage.setImageResource(product.getImageResourceId());
        updateProductInfo(holder, product);

        holder.decreaseButton.setOnClickListener(v -> {
            if (product.getQuantity() > 0) {
                product.setQuantity(product.getQuantity() - 1);
                updateProductInfo(holder, product);
            }
        });

        holder.increaseButton.setOnClickListener(v -> {
            product.setQuantity(product.getQuantity() + 1);
            updateProductInfo(holder, product);
        });

        holder.addToCartButton.setOnClickListener(v -> {
            if (product.getQuantity() <= 0) {
                Toast.makeText(v.getContext(), "Please add quantity first", Toast.LENGTH_SHORT).show();
                return;
            }
            currentOrder.addItem(product, product.getQuantity());
            if (itemClickListener != null) {
                itemClickListener.onItemAdded();
            }
            // Reset quantity after adding to cart
            product.setQuantity(0);
            holder.quantityText.setText("0");
            holder.calculationsLayout.setVisibility(View.GONE);
        });
    }

    private void updateProductInfo(ProductViewHolder holder, Product product) {
        holder.quantityText.setText(String.valueOf(product.getQuantity()));

        if (product.getQuantity() > 0) {
            holder.calculationsLayout.setVisibility(View.VISIBLE);

            // Update subtotal
            holder.subtotalText.setText("Subtotal: " + currencyFormatter.format(product.getSubtotal()));

            // Update discount if applicable
            if (product.getDiscount() > 0) {
                holder.discountText.setText(product.getDiscountMessage() + "\n-" + 
                    currencyFormatter.format(product.getDiscount()));
                holder.discountText.setVisibility(View.VISIBLE);
            } else {
                holder.discountText.setText(product.getDiscountMessage());
                holder.discountText.setVisibility(View.VISIBLE);
            }

            // Update total
            holder.totalText.setText("Total: " + currencyFormatter.format(product.getTotal()));
        } else {
            holder.calculationsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setOrder(Order order) {
        this.currentOrder = order;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        Button decreaseButton;
        Button increaseButton;
        TextView quantityText;
        LinearLayout calculationsLayout;
        TextView subtotalText;
        TextView discountText;
        TextView totalText;
        MaterialButton addToCartButton;

        ProductViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            quantityText = itemView.findViewById(R.id.quantityText);
            calculationsLayout = itemView.findViewById(R.id.calculationsLayout);
            subtotalText = itemView.findViewById(R.id.subtotalText);
            discountText = itemView.findViewById(R.id.discountText);
            totalText = itemView.findViewById(R.id.totalText);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }

    public interface OnItemClickListener {
        void onItemAdded();
    }
}