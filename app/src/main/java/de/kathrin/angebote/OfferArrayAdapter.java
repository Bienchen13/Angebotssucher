package de.kathrin.angebote;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class OfferArrayAdapter extends ArrayAdapter {

    private List<Offer> mOfferList;
    private LayoutInflater mLayoutInflater;

    public OfferArrayAdapter(Context context, List<Offer> offerList) {
        super(context, R.layout.offer_list, offerList);

        mOfferList = offerList;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View rowView;

        if (convertView == null) {
            // Create view hierarchy
             rowView = mLayoutInflater.inflate(R.layout.offer_list, parent, false);
        } else {
            rowView = convertView;
        }

        // Get current object
        Offer currentOffer = mOfferList.get(position);

        // Get view objects from view hierarchy
        TextView tvTitle = rowView.findViewById(R.id.item_title);
        TextView tvPrice = rowView.findViewById(R.id.item_price);
        //TextView tvDescription = rowView.findViewById(R.id.item_description);

        // Fill view object with contents from current object
        //tvTitle.setText(currentOffer.getTitle().replace("\n", " "));
        tvTitle.setText(currentOffer.getTitle());
        String price = String.format("%02.2f", currentOffer.getPrice()) + "€";
        tvPrice.setText(price);
        //tvDescription.setText(currentOffer.getDescription());

        return rowView;
    }
}
