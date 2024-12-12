package com.example.rent_hub;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rent_hub.models.Property;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Locale;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private List<Property> properties;
    private OnPropertyClickListener editListener;
    private OnPropertyClickListener deleteListener;
    private OnPropertyClickListener toggleStatusListener;
    private static final String BASE_URL = "http://10.0.2.2/rent_hub/api/";

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
    }

    public PropertyAdapter(List<Property> properties, 
                         OnPropertyClickListener editListener,
                         OnPropertyClickListener deleteListener,
                         OnPropertyClickListener toggleStatusListener) {
        this.properties = properties;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.toggleStatusListener = toggleStatusListener;
    }

    @Override
    public PropertyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PropertyViewHolder holder, int position) {
        Property property = properties.get(position);
        holder.titleText.setText(property.getTitle());
        holder.addressText.setText(property.getAddress());
        holder.priceText.setText(String.format(Locale.US, "$%.2f", property.getPrice()));
        
        // Set status text and color
        String status = property.isRented() ? "Rented" : "Available";
        int statusColor = property.isRented() ? Color.RED : Color.GREEN;
        holder.statusText.setText(status);
        holder.statusText.setTextColor(statusColor);

        // Load image using Picasso
        if (property.getImageUrl() != null && !property.getImageUrl().isEmpty()) {
            String imageUrl = BASE_URL + property.getImageUrl();
            Picasso.get()
                   .load(imageUrl)
                   .placeholder(R.drawable.placeholder_image)
                   .error(R.drawable.error_image)
                   .into(holder.propertyImage);
        } else {
            holder.propertyImage.setImageResource(R.drawable.placeholder_image);
        }

        // Set button text based on status
        holder.toggleStatusButton.setText(property.isRented() ? "Mark Available" : "Mark Rented");
        
        holder.editButton.setOnClickListener(v -> editListener.onPropertyClick(property));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onPropertyClick(property));
        holder.toggleStatusButton.setOnClickListener(v -> toggleStatusListener.onPropertyClick(property));
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView addressText;
        TextView priceText;
        TextView statusText;
        ImageView propertyImage;
        Button editButton;
        Button deleteButton;
        Button toggleStatusButton;

        PropertyViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            addressText = itemView.findViewById(R.id.addressText);
            priceText = itemView.findViewById(R.id.priceText);
            statusText = itemView.findViewById(R.id.propertyStatus);
            propertyImage = itemView.findViewById(R.id.propertyImage);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            toggleStatusButton = itemView.findViewById(R.id.toggleStatusButton);
        }
    }
}
