package de.kathrin.angebote;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OfferList {

    private Date availableFrom;
    private Date availableUntil;
    private List<Offer> offerList;
    private final DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    public OfferList() {}

    public OfferList(Date availableFrom, Date availableUntil, List<Offer> offerList) {
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.offerList = offerList;
    }

    public void setAvailableFrom(Date availableFrom) {
        this.availableFrom = availableFrom;
    }

    public void setAvailableUntil(Date availableUntil) {
        this.availableUntil = availableUntil;
    }

    public void setOfferList(List<Offer> offerList) {
        this.offerList = offerList;
    }

    public Date getAvailableFrom() {
        return availableFrom;
    }

    public String getAvailableFromFormatted () {
        return format.format(availableFrom);
    }

    public Long getAvailableFromTime () {
        return availableFrom.getTime();
    }

    public Date getAvailableUntil() {
        return availableUntil;
    }

    public String getAvailableUntilFormatted () {
        return format.format(availableUntil);
    }

    public Long getAvailableUntilTime () {
        return availableUntil.getTime();
    }

    public List<Offer> getOfferList() {
        return offerList;
    }

    @Override
    public String toString() {
        return "Folgende Angebote sind vom " + format.format(availableFrom) +
                " bis zum " + format.format(availableUntil) + " gültig:\n" +
                offerList;
    }
}
