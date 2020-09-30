package de.kathrin.angebote.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import de.kathrin.angebote.MainActivity;
import de.kathrin.angebote.R;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.database.MarketDbHelper;
import de.kathrin.angebote.models.Market;

import static de.kathrin.angebote.SelectMarketActivity.EXTRA_MARKET;

public class ProductArrayAdapter extends ArrayAdapter {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + ProductArrayAdapter.class.getSimpleName();
    private static final int LIST_LAYOUT = R.layout.product_list;

    private List<String> mProductList;
    private LayoutInflater mLayoutInflater;

    /**
     * Constructor for the MarketArrayAdapter
     * @param context   the context where the Adapter is called from (used in the super constructor
     *                  and to initialize the LayoutInflater)
     * @param productList    used in the getView method to get elements from the list
     */

    public ProductArrayAdapter(Context context, List<String> productList) {
        super(context, LIST_LAYOUT, productList);

        mProductList = productList;
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
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        // Create the view hierarchy defined in "R.layout.product_list" (one text view)
        final View rowView = mLayoutInflater.inflate(LIST_LAYOUT, parent, false);

        // Extract the content at the current position (get current market)
        final String currentProduct = mProductList.get(position);

        // Get view objects from view hierarchy (the text view)
        final TextView textView = rowView.findViewById(R.id.product_item);

        // Assign the current product name to the text view
        textView.setText(currentProduct);

        final TextView deleteButton = rowView.findViewById(R.id.product_item_delete);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Clicked on the X.");
                mProductList.remove(position);

                // Refreshes the list view immediately
                ((ListView) parent.findViewById(R.id.product_list)).invalidateViews();
            }
        });

        return rowView;
    }
}
