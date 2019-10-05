package de.kathrin.angebote;

import java.io.Serializable;

public class Market implements Serializable {

    private String marketID;
    private String name;
    private String street;
    private String city;
    private String plz;
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

    public void setMarketID(String marketID) {
        this.marketID = marketID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    @Override
    public String toString() {
        return name + "\n"
                + street + ", " + plz + " " + city + "\n";
    }
}
