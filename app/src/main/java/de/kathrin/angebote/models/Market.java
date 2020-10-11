package de.kathrin.angebote.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 *  Every market is an own object with
 *  marketID (from the website), name, street,
 *  city, plz and an _id for the database.
 */
public class Market implements Serializable {

    private final String marketID;
    private final String name;
    private final String street;
    private final String city;
    private final String plz;
    private long _id;

    public Market(String marketID, String name, String street, String city, String plz) {
        this.marketID = marketID;
        this.name = name;
        this.street = street;
        this.city = city;
        this.plz = plz;
    }

    public Market(String marketID, String name, String street, String city, String plz, long _id) {
        this.marketID = marketID;
        this.name = name;
        this.street = street;
        this.city = city;
        this.plz = plz;
        this._id = _id;
    }

    public String getMarketID() {
        return marketID;
    }

    public String getName() {
        return name;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPlz() {
        return plz;
    }

    public long get_id() {
        return _id;
    }

    @NonNull
    @Override
    public String toString() {
        return name + "\n"
                + street + ", " + plz + " " + city + "\n";
    }
}
