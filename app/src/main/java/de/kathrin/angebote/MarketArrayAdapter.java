package de.kathrin.angebote;

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

import java.util.List;

import static de.kathrin.angebote.SelectMarketActivity.EXTRA_MARKET;

public class MarketArrayAdapter extends ArrayAdapter {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + MarketArrayAdapter.class.getSimpleName();

    private SelectMarketActivity mParent;
    private List<Market> mMarketList;
    private LayoutInflater mLayoutInflater;
    private MarketDataSource mDataSource;

    public MarketArrayAdapter(Context context, List<Market> marketList, MarketDataSource datasource, SelectMarketActivity parent) {
        super(context, R.layout.market_list, marketList);

        mParent = parent;
        mMarketList = marketList;
        mLayoutInflater = LayoutInflater.from(context);
        mDataSource = datasource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        View rowView = mLayoutInflater.inflate(R.layout.market_list, parent, false);

        // Get current object
        final Market currentMarket = mMarketList.get(position);

        // Get view objects from view hierarchy
        TextView tvTitle = rowView.findViewById(R.id.list_item);
        final ImageButton favicon = rowView.findViewById(R.id.favicon);

        // Fill view object with contents from current object
        tvTitle.setText(currentMarket.toString());

        // Check if favorite
        if (mDataSource.checkMarketInFavourites(currentMarket)) {
           favicon.setImageResource(drawable.btn_star_big_on);
           favicon.setTag("fav");
       } else {
           favicon.setTag("unfav");
       }

        // Add or delete favourite markets
       favicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (favicon.getTag().equals("unfav")) {
                    favicon.setImageResource(drawable.btn_star_big_on);
                    favicon.setTag("fav");

                    mDataSource.addMarketToFavourites(currentMarket);

                } else {
                    favicon.setImageResource(drawable.btn_star_big_off);
                    favicon.setTag("unfav");

                    mDataSource.deleteMarketFromFavourites(currentMarket);
                }
            }
        });

        // Return to Main Activity when one market was selected
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