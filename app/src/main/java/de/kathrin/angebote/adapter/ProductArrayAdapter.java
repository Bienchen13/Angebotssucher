package de.kathrin.angebote.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.kathrin.angebote.R;
import de.kathrin.angebote.database.ProductDataSource;

import static de.kathrin.angebote.utlis.LayoutUtilsNotification.PRODUCT_ITEM;
import static de.kathrin.angebote.utlis.LayoutUtilsNotification.PRODUCT_LIST;
import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;

/**
 * Adapter to extract the Product data and show it in a list view
 * (used in the Notification Activity)
 */
public class ProductArrayAdapter extends ArrayAdapter<String> {

    private static final String LOG_TAG = PROJECT_NAME + ProductArrayAdapter.class.getSimpleName();
    private static final int LIST_LAYOUT = R.layout.product_list;

    private final List<String> mProductList;
    private final ProductDataSource mProductDataSource;
    private final LayoutInflater mLayoutInflater;

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
    @SuppressLint("ClickableViewAccessibility")
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        // Create the view hierarchy defined in LIST_LAYOUT (one text view and the delete X)
        @SuppressLint("ViewHolder")
        final View rowView = mLayoutInflater.inflate(LIST_LAYOUT, parent, false);

        // Extract the content at the current position (get current product name)
        final String currentProduct = mProductList.get(position);

        // Get the textView from the hierarchy
        final TextView textView = rowView.findViewById(PRODUCT_ITEM);

        // Assign the current product name to it
        textView.setText(currentProduct);

        // Remove the product, when the X on the right is clicked
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(event.getRawX() >= (textView.getRight() - textView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        mProductList.remove(position);
                        mProductDataSource.deleteProductFromNotificationDatabase(currentProduct);

                        // Refreshes the list view immediately
                        ((ListView) parent.findViewById(PRODUCT_LIST)).invalidateViews();
                        return true;
                    }
                }
                return false;
            }
        });

        return rowView;
    }
}
