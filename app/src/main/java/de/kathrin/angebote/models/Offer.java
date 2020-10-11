package de.kathrin.angebote.models;

import androidx.annotation.NonNull;

/**
 * Every offer has a title, price, description and image.
 */

public class Offer {

    private final String title;
    private final Double price;
    private final String description;
    private final String imageUrl;

    public Offer(String title, Double price, String description, String imageUrl) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public Double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return  title + ": " + price + "€\n(" + description + ")";
    }
}
