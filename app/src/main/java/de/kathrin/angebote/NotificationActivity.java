package de.kathrin.angebote;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.adapter.ProductArrayAdapter;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.utlis.LayoutUtilsSelectMarket;

public class NotificationActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + NotificationActivity.class.getSimpleName();

    private List<String> productList = new ArrayList<>();

    /**
     * Called automatically when entering this the first time activity.
     * Sets the layout.
     * @param savedInstanceState - save old state -
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

                ((ListView)findViewById(R.id.product_list)).invalidateViews();
            }
        };

        // Add Reaction to Button
        findViewById(R.id.add_product_button).setOnClickListener(onAddButtonClickListener);

        ProductArrayAdapter arrayAdapter = new ProductArrayAdapter(this, productList);
        ((ListView)findViewById(R.id.product_list)).setAdapter(arrayAdapter);
    }
}
