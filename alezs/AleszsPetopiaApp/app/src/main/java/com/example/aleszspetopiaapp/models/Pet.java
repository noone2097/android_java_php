package com.example.aleszspetopiaapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Pet implements Parcelable {
    private int id;
    private int userId;
    private String name;
    private String type;
    private int age;
    private double price;
    private String description;
    private String imageUrl; // URL of the pet image
    private String createdAt;

    public Pet() {
    }

    public Pet(int id, int userId, String name, String type, int age, double price, String description, String imageUrl, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.age = age;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    protected Pet(Parcel in) {
        id = in.readInt();
        userId = in.readInt();
        name = in.readString();
        type = in.readString();
        age = in.readInt();
        price = in.readDouble();
        description = in.readString();
        imageUrl = in.readString();
        createdAt = in.readString();
    }

    public static final Creator<Pet> CREATOR = new Creator<Pet>() {
        @Override
        public Pet createFromParcel(Parcel in) {
            return new Pet(in);
        }

        @Override
        public Pet[] newArray(int size) {
            return new Pet[size];
        }
    };

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(userId);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeInt(age);
        dest.writeDouble(price);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(createdAt);
    }
}
