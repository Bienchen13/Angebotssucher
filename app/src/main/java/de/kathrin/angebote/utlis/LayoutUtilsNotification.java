package de.kathrin.angebote.utlis;

import android.app.Activity;
import android.widget.EditText;
import android.widget.ListView;

import de.kathrin.angebote.R;

public class LayoutUtilsNotification {

    public static final int NOTIFICATION_ACTIVITY   = R.layout.activity_set_notifications;

    public static final int PRODUCT_ADD_FIELD       = R.id.product_add_field;
    public static final int PRODUCT_LIST            = R.id.product_notification_list;

    public static final int PRODUCT_ITEM            = R.id.product_item_name;


    // To get also the views..

    public final EditText PRODUCT_ADD_FIELD_VIEW;
    public final ListView PRODUCT_LIST_VIEW;

    public LayoutUtilsNotification(Activity activity) {
        PRODUCT_ADD_FIELD_VIEW      = activity.findViewById(PRODUCT_ADD_FIELD);
        PRODUCT_LIST_VIEW           = activity.findViewById(PRODUCT_LIST);
    }

}
