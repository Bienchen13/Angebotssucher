package de.kathrin.angebote.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.kathrin.angebote.R;
import de.kathrin.angebote.database.ProductDataSource;

import static de.kathrin.angebote.utlis.LayoutUtilsNotification.PRODUCT_ITEM;
import static de.kathrin.angebote.utlis.LayoutUtilsNotification.PRODUCT_ITEM_DELETE;
import static de.kathrin.angebote.utlis.LayoutUtilsNotification.PRODUCT_LIST;
import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;

/**
 * Adapter to extract the Product data and show it in a list view
 * (used in the Notification Activity)
 */
public class ProductArrayAdapter extends ArrayAdapter<String> {

    private static final String LOG_TAG = PROJECT_NAME + ProductArrayAdapter.class.getSimpleName();
    private static final int LIST_LAYOUT = R.layout.product_list;

    private List<String> mProductList;
    private ProductDataSource mProductDataSource;
    private LayoutInflater mLayoutInflater;

    /**
     * Constructor for the ProductArrayAdapter
     * @param context   the context where the Adapter is called from (used in the super constructor
     *                  and to initialize the LayoutInflater)
     * @param productList    used in the getView method to get elements from the list
     * @param productDataSource needed in getView method to delete elements from the database
     */

    public ProductArrayAdapter(Context context, List<String> productList, ProductDataSource productDataSource) {
        super(context, LIST_LAYOUT, productList);

        mProductList = productList;
        mProductDataSource = productDataSource;
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
        // Create the view hierarchy defined in LIST_LAYOUT (one text view and the delete X)
        @SuppressLint("ViewHolder")
        final View rowView = mLayoutInflater.inflate(LIST_LAYOUT, parent, false);

        // Extract the content at the current position (get current product name)
        final String currentProduct = mProductList.get(position);

        // Get the text view from the view hierarchy and assign the current product name to it
        ((TextView)rowView.findViewById(PRODUCT_ITEM)).setText(currentProduct);

        // When clicking on the X the element is removed from the list view and the database
        rowView.findViewById(PRODUCT_ITEM_DELETE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Clicked on the X.");

                mProductList.remove(position);
                mProductDataSource.deleteProductFromNotificationDatabase(currentProduct);

                // Refreshes the list view immediately
                ((ListView) parent.findViewById(PRODUCT_LIST)).invalidateViews();
            }
        });

        return rowView;
    }
}
