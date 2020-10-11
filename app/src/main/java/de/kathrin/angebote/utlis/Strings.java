package de.kathrin.angebote.utlis;

public class Strings {

    // General title for debugging
    public static final String PROJECT_NAME = "Angebote.";

    // Main Activity
    public static final String NO_MARKET_SELECTED = "Wählen Sie zu erst einen Markt aus!";
    public static final String NO_SERVER_CONNECTION =
            "Verbindung zum Server fehlgeschlagen. Es konnten keine Angebote geladen werden.";
    public static final String NO_OFFERS_FOUND = "Keine Angebote zu Ihrer Anfrage gefunden";

    // Select Market Activity
    public static final String EXTRA_MARKET = PROJECT_NAME + "EXTRA_MARKET";
    public static final String FOUND_MARKETS = "Gefundene Märkte: ";

    // Market Array Adapter
    public static final String FAV_TAG = "fav";
    public static final String UNFAV_TAG = "unfav";

    // Alarm Handler
    public static final String ALARM_ACTION = "de.kathrin.angebote.notification";

    // Boot Receiver - Notification from the OS
    public static final String BOOT_COMPLETE_ACTION = "android.intent.action.BOOT_COMPLETED";

    // Notification Controller
    public static final String CHANNEL_NAME = "Notification Channel";
    public static final String CHANNEL_DESCRIPTION = "Channel to publish the product notification.";

    // DbHelper
    public static final String DB_NAME = "angebote.db";

    // OfferList
    public static final String DATE_FORMAT_OFFER_LIST = "dd.MM.yyyy";

    // IOUtils

    // Server request URLs
    static final String URL_EDEKA_OFFERS = "https://www.edeka.de/eh/service/eh/offers?";
    static final String URL_EDEKA_MARKETS = "https://www.edeka.de/search.xml";

    // File Access Strings
    static final String TEXTFILE_ENDING = ".txt";
    static final String DEFAULT_MARKET_FILE = "default_market.txt";

    // MarketUtils
    public static final String UTF8 = "UTF-8";


}
