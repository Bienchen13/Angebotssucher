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
    public static final int MARKET_SEARCH_BUTTON    = R.id.market_search_button;

    public static final int MARKET_RESULT_HEADER    = R.id.market_select_header;
    public static final int MARKET_RESULT_LIST      = R.id.market_select_list;


    // To get also the views..

    private Activity activity;

    public EditText MARKET_SEARCH_FIELD_VIEW;
    public Button MARKET_SEARCH_BUTTON_VIEW;

    public TextView MARKET_RESULT_HEADER_VIEW;
    public ListView MARKET_RESULT_LIST_VIEW;

    public LayoutUtilsSelectMarket(Activity activity) {
        this.activity = activity;

        MARKET_SEARCH_FIELD_VIEW    = this.activity.findViewById(MARKET_SEARCH_FIELD);
        MARKET_SEARCH_BUTTON_VIEW   = this.activity.findViewById(MARKET_SEARCH_BUTTON);

        MARKET_RESULT_HEADER_VIEW   = this.activity.findViewById(MARKET_RESULT_HEADER);
        MARKET_RESULT_LIST_VIEW     = this.activity.findViewById(MARKET_RESULT_LIST);
    }
}
