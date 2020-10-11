package de.kathrin.angebote.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.R.drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import de.kathrin.angebote.MainActivity;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.R;
import de.kathrin.angebote.database.MarketDataSource;

import static de.kathrin.angebote.utlis.LayoutUtilsSelectMarket.MARKET_ITEM;
import static de.kathrin.angebote.utlis.LayoutUtilsSelectMarket.MARKET_ITEM_STAR;
import static de.kathrin.angebote.utlis.Strings.EXTRA_MARKET;
import static de.kathrin.angebote.utlis.Strings.FAV_TAG;
import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;
import static de.kathrin.angebote.utlis.Strings.UNFAV_TAG;

/**
 * Adapter to extract the Market data and show it in a list view
 * (used in the Select Market Activity)
 */

public class MarketArrayAdapter extends ArrayAdapter<Market> {

    private static final String LOG_TAG = PROJECT_NAME + MarketArrayAdapter.class.getSimpleName();
    private static final int LIST_LAYOUT = R.layout.market_list;
    private static final int FAV_STAR = drawable.btn_star_big_on;
    private static final int UNFAV_STAR = drawable.btn_star_big_off;

    private final AppCompatActivity mParent;
    private final List<Market> mMarketList;
    private final LayoutInflater mLayoutInflater;
    private final MarketDataSource mDataSource;

    /**
     * Constructor for the MarketArrayAdapter
     * @param context   the context where the Adapter is called from (used in the super constructor
     *                  and to initialize the LayoutInflater)
     * @param marketList    used in the getView method to get elements from the list
     * @param datasource    used to check if a market is a favourite ( {@link MarketDataSource})
     * @param parent    the activity where the Adapter is called from (to return to this activity
     *                  in the end)
     */

    public MarketArrayAdapter(Context context, List<Market> marketList, MarketDataSource datasource, AppCompatActivity parent) {
        super(context, LIST_LAYOUT, marketList);

        mParent = parent;
        mMarketList = marketList;
        mLayoutInflater = LayoutInflater.from(context);
        mDataSource = datasource;
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
    public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        // Create the view hierarchy defined in "R.layout.market_list" (one text view and the
        // favourite-market star image button)
        @SuppressLint("ViewHolder")
        View rowView = mLayoutInflater.inflate(LIST_LAYOUT, parent, false);

        // Extract the content at the current position (get current market)
        final Market currentMarket = mMarketList.get(position);

        // Get view objects from view hierarchy (the text view and the image button)
        final TextView tvTitle = rowView.findViewById(MARKET_ITEM);
        final ImageButton favicon = rowView.findViewById(MARKET_ITEM_STAR);

        // Assign the current market name to the text view
        tvTitle.setText(currentMarket.toString());

        // Check if the market is favorite
        // (If it is in the "favourite database")
        if (mDataSource.checkMarketInFavourites(currentMarket)) {
           favicon.setImageResource(FAV_STAR);
           favicon.setTag(FAV_TAG);
       } else {
           favicon.setTag(UNFAV_TAG);
       }

        // Listen to add or delete favourite markets
       favicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (favicon.getTag().equals(UNFAV_TAG)) {
                    favicon.setImageResource(FAV_STAR);
                    favicon.setTag(FAV_TAG);

                    mDataSource.addMarketToFavourites(currentMarket);

                } else {
                    favicon.setImageResource(UNFAV_STAR);
                    favicon.setTag(UNFAV_TAG);

                    mDataSource.deleteMarketFromFavourites(currentMarket);
                }
            }
        });

        // Return to Main Activity when one market was selected
        // (by clicking on the name of the market)
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Selected: " + currentMarket);

                // Send back selected market
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRA_MARKET, currentMarket);
                mParent.setResult(MainActivity.RESULT_OK, returnIntent);
                mParent.finish();

            }
        });

        return rowView;
    }
}