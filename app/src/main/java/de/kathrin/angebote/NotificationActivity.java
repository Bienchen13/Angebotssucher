package de.kathrin.angebote;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.adapter.ProductArrayAdapter;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.database.ProductDataSource;

public class NotificationActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + NotificationActivity.class.getSimpleName();

    private List<String> productList = new ArrayList<>();
    private ProductDataSource productDataSource;

    /**
     * Called automatically when entering this the first time activity.
     * Sets the layout.
     * @param savedInstanceState - save old state -
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "In Create");

        // Set Layout
        setContentView(R.layout.activity_set_notifications);

        // Define On Click Button Reaction
        View.OnClickListener onAddButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Add Product to List");

                String product = ((EditText) findViewById(R.id.add_product_field)).getText().toString();
                Log.v(LOG_TAG, product);

                productList.add(product);
                productDataSource.addProductToNotificationDatabase(product);

                ((ListView)findViewById(R.id.product_list)).invalidateViews();
            }
        };

        // Add Reaction to Button
        findViewById(R.id.add_product_button).setOnClickListener(onAddButtonClickListener);

        // Connect to product notification database
        productDataSource = new ProductDataSource(this);

        // Bind Adapter to List View
        ProductArrayAdapter arrayAdapter = new ProductArrayAdapter(this, productList, productDataSource);
        ((ListView)findViewById(R.id.product_list)).setAdapter(arrayAdapter);


    }

    /**
     * Called automatically every time entering this activity.
     * The database is opened.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "On Resume");

        // Open database connection
        productDataSource.open();

        productList.addAll(productDataSource.getAllProductsFromDatabase());
        ((ListView)findViewById(R.id.product_list)).invalidateViews();

        Log.v(LOG_TAG, "Added:" + productList.toString() + ", " + productList.size() + " elements.");
    }

    /**
     * Called automatically every time leaving this activity.
     * The database is closed.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "On Pause");

        // Close database connection
        productDataSource.close();

    }
}
