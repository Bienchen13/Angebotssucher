package de.kathrin.angebote.models;

import androidx.annotation.NonNull;
import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static de.kathrin.angebote.utlis.Strings.DATE_FORMAT_OFFER_LIST;

/**
 * Every OfferList has a period of validity and (of course) a list with all offers.
 */
public class OfferList extends ArrayList<Offer> {

    private Date availableFrom;
    private Date availableUntil;

    @SuppressLint("SimpleDateFormat")
    private final DateFormat format = new SimpleDateFormat(DATE_FORMAT_OFFER_LIST);

    public OfferList() {}

    // SETTER

    public void setAvailableFrom(Date availableFrom) {
        this.availableFrom = availableFrom;
    }

    public void setAvailableUntil(Date availableUntil) {
        this.availableUntil = availableUntil;
    }

    // GETTER
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

    public String getOffersInString () {
        StringBuilder concat = new StringBuilder();
        for (Offer o: this) {
            concat.append(o.getTitle())
                    .append(": ")
                    .append(o.getPrice().toString())
                    .append("€ ")
                    .append(o.getDescription())
                    .append(" ")
                    .append(o.getImageUrl())
                    .append("\n");
        }
        return concat.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return "Folgende Angebote sind vom " + format.format(availableFrom) +
                " bis zum " + format.format(availableUntil) + " gültig:" +
                getOffersInString();
    }
}
