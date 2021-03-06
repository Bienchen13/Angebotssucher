package de.kathrin.angebote;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.kathrin.angebote.adapter.ProductArrayAdapter;
import de.kathrin.angebote.alarm.AlarmHandler;
import de.kathrin.angebote.alarm.AlarmReceiver;
import de.kathrin.angebote.alarm.BootReceiver;

import de.kathrin.angebote.database.ProductDataSource;
import de.kathrin.angebote.utlis.LayoutUtilsNotification;
import de.kathrin.angebote.utlis.NotificationUtils;

import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;

/**
 * Activity to control the notifications
 */
public class NotificationActivity extends AppCompatActivity {

    private static final String LOG_TAG = PROJECT_NAME + NotificationActivity.class.getSimpleName();

    private final List<String> productList = new ArrayList<>();
    private ProductDataSource productDataSource;
    private LayoutUtilsNotification lu;

    /**
     * Called automatically when entering this the first time activity.
     * Sets the layout, initializes the Add-Product-Button and registers the alarm receiver.
     * @param savedInstanceState - save old state -
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "In Create");

        // Set Layout
        setContentView(LayoutUtilsNotification.NOTIFICATION_ACTIVITY);

        // Init Helper Class to handle the access to the layout elements
        lu = new LayoutUtilsNotification(this);

        // Connect to product notification database
        productDataSource = new ProductDataSource(this);

        // Init the button to add products to the list
        initAddProductButton();

        // Initialize list view
        bindAdapterToListView();

        // Set alarm receiver
        registerAlarmAndBootReceivers();
    }

    /**
     * Enables an AlarmReceiver and a BootReceiver in the PackageManager.
     * Both are used for notifications.
     */
    private void registerAlarmAndBootReceivers() {
        final PackageManager pm = NotificationActivity.this.getPackageManager();

        // Alarm Receiver
        pm.setComponentEnabledSetting(
                new ComponentName(NotificationActivity.this, AlarmReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        // Boot Receiver
        pm.setComponentEnabledSetting(
                new ComponentName(NotificationActivity.this, BootReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Add the elements in the text view to the list and to the database on button or enter click
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initAddProductButton () {

        // Add a new product, when enter is clicked
        lu.PRODUCT_ADD_FIELD_VIEW.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyEvent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addProduct();
                    return true;
                }
                return false;
            }
        });

        // Add a new product, when the add button is clicked
        final EditText editText = lu.PRODUCT_ADD_FIELD_VIEW;
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() + 50 >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                        addProduct();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Add new product to list and database and reset the input field.
     */
    private void addProduct () {
        // Get product from Text View and reset it
        String product = lu.PRODUCT_ADD_FIELD_VIEW.getText().toString();
        lu.PRODUCT_ADD_FIELD_VIEW.setText("");

        if (!product.equals("")) {

            // Add product to list and database
            productList.add(product);
            productDataSource.addProductToNotificationDatabase(product);

            // Refresh the list view immediately
            lu.PRODUCT_LIST_VIEW.invalidateViews();
        }
    }

    /**
     * Connect the list view to the product array adapter
     */
    private void bindAdapterToListView () {
        lu.PRODUCT_LIST_VIEW.setAdapter(
                new ProductArrayAdapter(this, productList, productDataSource)
        );
    }

    /**
     * Called automatically every time entering this activity.
     * The database is opened and the current products to notify on offer are shown.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "On Resume");

        // Open database connection
        productDataSource.open();

        // Show all current products in the database
        productList.clear();
        productList.addAll(productDataSource.getAllProductsFromDatabase());
        lu.PRODUCT_LIST_VIEW.invalidateViews();
    }

    /**
     * Called automatically every time leaving this activity.
     * The database is closed and the alarm is set.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "On Stop");

        // Close database connection
        productDataSource.close();

        // Set or clear alarm
        updateAlarm();
    }

    /**
     * Set or cancel the alarm.
     * If there are no products in the list, cancel it.
     * Otherwise check if there is already an alarm set. If not, set it.
     */
    private void updateAlarm () {

        if (productList.isEmpty()) {
            // Cancel alarm and delete file
            AlarmHandler.cancelAlarm(this);
            NotificationUtils.clearAlarmFile(this);
            Log.v(LOG_TAG, "List is empty, no alarm set.");

        } else if (NotificationUtils.alarmIsSet(this)){
            // When file has alarm: good, do nothing
            Log.v(LOG_TAG, "Alarm is already set.");

            // Cancel alarm and set it again...
            AlarmHandler.cancelAlarm(this);
            Calendar date = NotificationUtils.getAlarmDateFromFile(this);
            AlarmHandler.setAlarm(this, date);

        } else {
            // Set alarm to next day and update file
            Calendar date = NotificationUtils.getTomorrow();
            AlarmHandler.setAlarm(this, date);
            NotificationUtils.writeAlarmToFile(this, date);
            Log.v(LOG_TAG, "Setting new alarm to " + date.getTime());
        }
    }
}
