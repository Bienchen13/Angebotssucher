package de.kathrin.angebote.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.kathrin.angebote.models.Offer;
import de.kathrin.angebote.R;

import static de.kathrin.angebote.utlis.Strings.EURO;
import static de.kathrin.angebote.utlis.Strings.PRICE_FORMAT;

/**
 * Adapter to show offers in a list view
 * (used in the Main Activity)
 */
public class OfferArrayAdapter extends ArrayAdapter<Offer> {

    private static final int LIST_LAYOUT = R.layout.offer_list;

    private final List<Offer> mOfferList;
    private final LayoutInflater mLayoutInflater;

    /**
     * Constructor for the OfferArrayAdapter
     * @param context   the context where the Adapter is called from (used in the super constructor
     *      *           and to initialize the LayoutInflater)
     * @param offerList used in the getView method to get elements from the list
     */
    public OfferArrayAdapter(Context context, List<Offer> offerList) {
        super(context, LIST_LAYOUT, offerList);

        mOfferList = offerList;
        mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * The getView method creates the view hierarchy, extracts the position content, assigns the
     * content to the view elements and returns the view hierarchy.
     * @param position  current position in the list view - done by java -
     * @param convertView   - done by java -
     * @param parent    - done by java -
     * @return  the content filled view hierarchy of one child element of the list view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Create view hierarchy defined in "R.layout.offer_list" (two text views)
        @SuppressLint("ViewHolder")
        View rowView = mLayoutInflater.inflate(LIST_LAYOUT, parent, false);

        // Extract the content at the current position (get current offer)
        final Offer currentOffer = mOfferList.get(position);

        // Get view objects from view hierarchy (the two text views)
        final TextView tvTitle = rowView.findViewById(R.id.offer_item_title);
        final TextView tvPrice = rowView.findViewById(R.id.offer_item_price);

        // Assign the title and price to the text views
        tvTitle.setText(currentOffer.getTitle());

        @SuppressLint("DefaultLocale")
        String price = String.format(PRICE_FORMAT, currentOffer.getPrice()) + EURO;
        tvPrice.setText(price);

        return rowView;
    }
}
