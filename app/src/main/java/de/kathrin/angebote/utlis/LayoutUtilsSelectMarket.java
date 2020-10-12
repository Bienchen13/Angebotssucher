package de.kathrin.angebote.utlis;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import de.kathrin.angebote.R;

public class LayoutUtilsSelectMarket {

    public static final int SELECT_MARKET_ACTIVITY  = R.layout.activity_select_market;

    public static final int MARKET_SEARCH_FIELD     = R.id.market_search_field;

    public static final int MARKET_RESULT_HEADER    = R.id.market_select_header;
    public static final int MARKET_RESULT_LIST      = R.id.market_select_list;

    public static final int MARKET_ITEM             = R.id.market_list_item;
    public static final int MARKET_ITEM_STAR        = R.id.market_fav_icon;


    // To get also the views..

    public final EditText MARKET_SEARCH_FIELD_VIEW;

    public final TextView MARKET_RESULT_HEADER_VIEW;
    public final ListView MARKET_RESULT_LIST_VIEW;

    public LayoutUtilsSelectMarket(Activity activity) {
        MARKET_SEARCH_FIELD_VIEW    = activity.findViewById(MARKET_SEARCH_FIELD);

        MARKET_RESULT_HEADER_VIEW   = activity.findViewById(MARKET_RESULT_HEADER);
        MARKET_RESULT_LIST_VIEW     = activity.findViewById(MARKET_RESULT_LIST);
    }
}
