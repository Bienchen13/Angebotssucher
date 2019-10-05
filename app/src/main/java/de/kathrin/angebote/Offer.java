package de.kathrin.angebote;

public class Offer {

    private final String title;
    private final Double price;
    private final String description;

    public Offer(String title, Double price, String description) {
        this.title = title;
        this.price = price;
        this.description = description;
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

    @Override
    public String toString() {
        return  title + ": " + price + "€\n(" + description + ")";
    }
}
