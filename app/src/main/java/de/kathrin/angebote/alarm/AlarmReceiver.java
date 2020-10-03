package de.kathrin.angebote.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.kathrin.angebote.MainActivity;

/**
 * Creates a new notification when the alarmManager send a signal
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + AlarmReceiver.class.getSimpleName();

    /**
     * Show a new notification. The method automatically is called, when an alarm
     * (from the alarmManager, set in {@link AlarmHandler}) is received
     * @param context   current context
     * @param intent    not used
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Alarm received.");

        NotificationController nc = new NotificationController(context);
        nc.addNewNotification("on Receive", "it woooorks");
    }
}
