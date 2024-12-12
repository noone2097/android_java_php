package com.example.chiespetalparadise;

public class Flower {
    private int id;
    private String name;
    private String description;
    private double price;
    private double taxRate;
    private String imageUrl;
    private int stock;
    private String createdAt;
    
    private static final double BULK_DISCOUNT_THRESHOLD = 10; // 10 or more flowers
    private static final double BULK_DISCOUNT_RATE = 0.15; // 15% discount
    private static final double SPECIAL_DISCOUNT_THRESHOLD = 20; // 20 or more flowers
    private static final double SPECIAL_DISCOUNT_RATE = 0.25; // 25% discount

    public Flower(int id, String name, String description, double price, double taxRate, 
                 String imageUrl, int stock, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.taxRate = taxRate;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public double getTaxRate() { return taxRate; }
    public String getImageUrl() { return imageUrl; }
    public int getStock() { return stock; }
    public String getCreatedAt() { return createdAt; }

    // Business Logic Methods
    
    /**
     * Calculate total price including quantity and discounts
     * @param quantity Number of flowers to purchase
     * @return Total price after discounts but before tax
     */
    public double calculateSubtotal(int quantity) {
        if (quantity <= 0) return 0;
        
        double subtotal = price * quantity;
        
        // Apply discounts based on quantity
        if (quantity >= SPECIAL_DISCOUNT_THRESHOLD) {
            subtotal *= (1 - SPECIAL_DISCOUNT_RATE);
        } else if (quantity >= BULK_DISCOUNT_THRESHOLD) {
            subtotal *= (1 - BULK_DISCOUNT_RATE);
        }
        
        return subtotal;
    }

    /**
     * Calculate tax amount
     * @param subtotal Amount to calculate tax on
     * @return Tax amount
     */
    public double calculateTax(double subtotal) {
        return subtotal * (taxRate / 100.0); // Convert percentage to decimal
    }

    /**
     * Calculate final total including tax
     * @param quantity Number of flowers to purchase
     * @return Final total price including discounts and tax
     */
    public double calculateTotal(int quantity) {
        double subtotal = calculateSubtotal(quantity);
        double tax = calculateTax(subtotal);
        return subtotal + tax;
    }

    /**
     * Check if quantity is available in stock
     * @param quantity Quantity to check
     * @return true if quantity is available
     */
    public boolean isQuantityAvailable(int quantity) {
        return quantity > 0 && quantity <= stock;
    }

    /**
     * Get discount percentage based on quantity
     * @param quantity Number of flowers
     * @return Discount percentage (0-100)
     */
    public int getDiscountPercentage(int quantity) {
        if (quantity >= SPECIAL_DISCOUNT_THRESHOLD) {
            return (int)(SPECIAL_DISCOUNT_RATE * 100);
        } else if (quantity >= BULK_DISCOUNT_THRESHOLD) {
            return (int)(BULK_DISCOUNT_RATE * 100);
        }
        return 0;
    }

    /**
     * Format price with currency symbol
     * @param price Price to format
     * @return Formatted price string
     */
    public static String formatPrice(double price) {
        return String.format("₱%.2f", price);
    }

    /**
     * Get a summary of price breakdown
     * @param quantity Number of flowers
     * @return Price breakdown string
     */
    public String getPriceBreakdown(int quantity) {
        if (quantity <= 0) return "Invalid quantity";
        
        double subtotal = calculateSubtotal(quantity);
        double tax = calculateTax(subtotal);
        double total = subtotal + tax;
        int discountPercent = getDiscountPercentage(quantity);
        
        StringBuilder breakdown = new StringBuilder();
        breakdown.append(String.format("Unit Price: %s\n", formatPrice(price)));
        breakdown.append(String.format("Quantity: %d\n", quantity));
        if (discountPercent > 0) {
            breakdown.append(String.format("Discount: %d%%\n", discountPercent));
        }
        breakdown.append(String.format("Subtotal: %s\n", formatPrice(subtotal)));
        breakdown.append(String.format("Tax (%.1f%%): %s\n", taxRate, formatPrice(tax)));
        breakdown.append(String.format("Total: %s", formatPrice(total)));
        
        return breakdown.toString();
    }
}
