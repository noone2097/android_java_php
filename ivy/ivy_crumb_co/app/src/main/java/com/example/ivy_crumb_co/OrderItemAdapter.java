package com.example.ivy_crumb_co;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderViewHolder> {
    private Order order;
    private NumberFormat currencyFormatter;
    private ItemUpdateListener itemUpdateListener;

    public interface ItemUpdateListener {
        void onItemUpdated();
        void onOrderUpdated(Order order);
    }

    public OrderItemAdapter(Order order) {
        this.order = order;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fil", "PH"));
    }

    public void setItemUpdateListener(ItemUpdateListener itemUpdateListener) {
        this.itemUpdateListener = itemUpdateListener;
    }

    public void updateOrder(Order newOrder) {
        this.order = newOrder;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem item = order.getOrderItems().get(position);
        Product product = item.getProduct();

        holder.productNameTextView.setText(product.getName());
        holder.productImageView.setImageResource(product.getImageResourceId());
        updateItemDisplay(holder, item);

        holder.decreaseButton.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                updateItemDisplay(holder, item);
                if (itemUpdateListener != null) {
                    itemUpdateListener.onItemUpdated();
                }
            } else {
                order.removeItem(item);
                notifyDataSetChanged();
                if (itemUpdateListener != null) {
                    itemUpdateListener.onOrderUpdated(order);
                }
            }
        });

        holder.increaseButton.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            updateItemDisplay(holder, item);
            if (itemUpdateListener != null) {
                itemUpdateListener.onItemUpdated();
            }
        });

        holder.removeButton.setOnClickListener(v -> {
            order.removeItem(item);
            notifyDataSetChanged();
            if (itemUpdateListener != null) {
                itemUpdateListener.onOrderUpdated(order);
            }
        });
    }

    private void updateItemDisplay(@NonNull OrderViewHolder holder, OrderItem item) {
        Product product = item.getProduct();
        holder.quantityTextView.setText(String.format("Quantity: %d", item.getQuantity()));
        holder.priceTextView.setText(String.format("Price: %s", currencyFormatter.format(product.getPrice())));
        holder.subtotalTextView.setText(String.format("Subtotal: %s", currencyFormatter.format(item.getSubtotal())));
        
        // Show discount if applicable
        double discount = item.getDiscount();
        if (discount > 0) {
            holder.discountTextView.setVisibility(View.VISIBLE);
            holder.discountTextView.setText(String.format("Discount: -%s", currencyFormatter.format(discount)));
            holder.totalTextView.setVisibility(View.VISIBLE);
            holder.totalTextView.setText(String.format("Total: %s", currencyFormatter.format(item.getTotal())));
        } else {
            holder.discountTextView.setVisibility(View.GONE);
            holder.totalTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return order.getOrderItems().size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView quantityTextView;
        TextView priceTextView;
        TextView subtotalTextView;
        TextView discountTextView;
        TextView totalTextView;
        ImageButton removeButton;
        ImageButton decreaseButton;
        ImageButton increaseButton;
        ImageView productImageView;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            subtotalTextView = itemView.findViewById(R.id.subtotalTextView);
            discountTextView = itemView.findViewById(R.id.discountTextView);
            totalTextView = itemView.findViewById(R.id.totalTextView);
            removeButton = itemView.findViewById(R.id.removeButton);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            productImageView = itemView.findViewById(R.id.productImageView);
        }
    }
}
