package de.kathrin.angebote.utlis;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import de.kathrin.angebote.R;

public class LayoutUtilsMain {

    public static final int MAIN_ACTIVITY           = R.layout.activity_main;

    public static final int OFFER_POPUP             = R.layout.description_popup;
    public static final int OFFER_POPUP_TITLE       = R.id.popup_title;
    public static final int OFFER_POPUP_DESCRIPTION = R.id.popup_description;
    public static final int OFFER_POPUP_IMAGE       = R.id.popup_image;

    public static final int OFFER_RESULT_HEADER     = R.id.offer_result_header;
    public static final int OFFER_RESULT_LIST       = R.id.offer_result_list;

    public static final int OFFER_SEARCH_FIELD      = R.id.offer_search_field;
    public static final int OFFER_SEARCH_BUTTON     = R.id.offer_search_button;

    public static final int MARKET_SELECT           = R.id.select_market;


    // To get also the views..

    public final TextView MARKET_SELECT_VIEW;

    public final TextView OFFER_RESULT_HEADER_VIEW;
    public final ListView OFFER_RESULT_LIST_VIEW;

    public final EditText OFFER_SEARCH_FIELD_VIEW;
    public final Button   OFFER_SEARCH_BUTTON_VIEW;

    public LayoutUtilsMain(Activity activity) {
        MARKET_SELECT_VIEW        = activity.findViewById(MARKET_SELECT);

        OFFER_RESULT_HEADER_VIEW  = activity.findViewById(OFFER_RESULT_HEADER);
        OFFER_RESULT_LIST_VIEW    = activity.findViewById(OFFER_RESULT_LIST);

        OFFER_SEARCH_FIELD_VIEW   = activity.findViewById(OFFER_SEARCH_FIELD);
        OFFER_SEARCH_BUTTON_VIEW  = activity.findViewById(OFFER_SEARCH_BUTTON);
    }

    // For the popup:

    public TextView OFFER_POPUP_TITLE_VIEW;
    public TextView OFFER_POPUP_DESCRIPTION_VIEW;
    public ImageView OFFER_POPUP_IMAGE_VIEW;

    private View popupView;

    public void setPopupView(View view) {
        this.popupView = view;

        OFFER_POPUP_TITLE_VIEW          = popupView.findViewById(OFFER_POPUP_TITLE);
        OFFER_POPUP_DESCRIPTION_VIEW    = popupView.findViewById(OFFER_POPUP_DESCRIPTION);
        OFFER_POPUP_IMAGE_VIEW          = popupView.findViewById(OFFER_POPUP_IMAGE);
    }



}
