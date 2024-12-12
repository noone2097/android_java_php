package com.example.aleszspetopiaapp.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aleszspetopiaapp.R;
import com.example.aleszspetopiaapp.models.Pet;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.ViewHolder> {
    private static final String TAG = "PetAdapter";
    private static final String BASE_URL = "http://10.0.2.2/petopia/";
    private List<Pet> pets;
    private Context context;
    private OnPetClickListener listener;
    private static final int SENIOR_PET_AGE = 8;
    private static final double DISCOUNT_PERCENTAGE = 0.20;

    public interface OnPetClickListener {
        void onEditClick(Pet pet);
        void onDeleteClick(Pet pet);
    }

    public PetAdapter(Context context, List<Pet> pets, OnPetClickListener listener) {
        this.context = context;
        this.pets = pets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pet pet = pets.get(position);
        
        holder.textViewPetName.setText(pet.getName());
        holder.textViewPetType.setText(pet.getType());
        holder.textViewPetAge.setText(String.format(context.getString(R.string.pet_age_format), pet.getAge()));

        // Get the original price (already in PHP)
        double originalPrice = pet.getPrice();

        if (pet.getAge() >= SENIOR_PET_AGE) {
            // Show original price with strikethrough
            holder.textViewOriginalPrice.setVisibility(View.VISIBLE);
            holder.textViewOriginalPrice.setText(String.format("₱%.2f", originalPrice));
            holder.textViewOriginalPrice.setPaintFlags(
                holder.textViewOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // Calculate and show discounted price
            double discountedPrice = originalPrice * (1 - DISCOUNT_PERCENTAGE);
            holder.textViewPetPrice.setText(String.format("₱%.2f", discountedPrice));
            
            // Show discount text
            holder.textViewDiscount.setVisibility(View.VISIBLE);
        } else {
            // Show only original price
            holder.textViewOriginalPrice.setVisibility(View.GONE);
            holder.textViewPetPrice.setText(String.format("₱%.2f", originalPrice));
            holder.textViewDiscount.setVisibility(View.GONE);
        }

        // Load image using Picasso
        if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            String imageUrl = pet.getImageUrl();
            if (!imageUrl.startsWith("http")) {
                imageUrl = BASE_URL + imageUrl;
            }
            
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_pet_placeholder)
                .error(R.drawable.ic_pet_placeholder)
                .into(holder.imageViewPet);
        } else {
            holder.imageViewPet.setImageResource(R.drawable.ic_pet_placeholder);
        }

        // Set click listeners for edit and delete buttons
        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(pet);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(pet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pets != null ? pets.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPet;
        TextView textViewPetName;
        TextView textViewPetType;
        TextView textViewPetAge;
        TextView textViewPetPrice;
        TextView textViewOriginalPrice;
        TextView textViewDiscount;
        MaterialButton buttonEdit;
        MaterialButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPet = itemView.findViewById(R.id.imageViewPet);
            textViewPetName = itemView.findViewById(R.id.textViewPetName);
            textViewPetType = itemView.findViewById(R.id.textViewPetType);
            textViewPetAge = itemView.findViewById(R.id.textViewPetAge);
            textViewPetPrice = itemView.findViewById(R.id.textViewPetPrice);
            textViewOriginalPrice = itemView.findViewById(R.id.textViewOriginalPrice);
            textViewDiscount = itemView.findViewById(R.id.textViewDiscount);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            textViewDiscount.setText(R.string.discount_text);
        }
    }

    public void updatePets(List<Pet> newPets) {
        this.pets = newPets;
        notifyDataSetChanged();
    }
}
