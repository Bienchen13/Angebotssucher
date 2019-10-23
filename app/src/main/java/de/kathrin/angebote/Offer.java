package de.kathrin.angebote;

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

    @Override
    public String toString() {
        return  title + ": " + price + "€\n(" + description + ")";
    }
}
