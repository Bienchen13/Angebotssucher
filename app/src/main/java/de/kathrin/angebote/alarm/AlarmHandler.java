package de.kathrin.angebote.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import de.kathrin.angebote.MainActivity;

/**
 * Class to set and cancel the repeated offer notification
 */
public class AlarmHandler {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + AlarmHandler.class.getSimpleName();

    public static final String ALARM_ACTION = "de.kathrin.angebote.notification";
    public static final int REQUEST_CODE = 0;

    /**
     * Set the repeating alarm. At the time, the alarmManager wakes the app and calls the
     * {@link AlarmReceiver}.
     * @param context   current context
     */
    public static void setAlarm (final Context context) {

        // Set the stating time for the alarm
        Calendar time = Calendar.getInstance();

        //calendar.set(Calendar.DAY_OF_WEEK, 1);
        //Log.v(LOG_TAG, "Calender on:" + calendar.getTime());



        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the repeating alarm (every week, starting at monday)
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,                    // wake phone up
                time.getTimeInMillis(),                 // every monday
                //AlarmManager.INTERVAL_DAY * 7,   // each week once
                1000 * 60, // every minute
                createIntent(context));

        Log.v(LOG_TAG, "Alarm is set.");

    }

    /**
     * Cancel the repeating alarm.
     * @param context   current context
     */
    public static void cancelAlarm (final Context context) {
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createIntent(context));
        Log.v(LOG_TAG, "Canceled alarm.");

    }

    /**
     * Create the same intent for set and cancel alarm. They need both the same intent!
     * The intent specifies, which class is notified, when the alarmManger sends a broadcast signal.
     * @param context   current context
     * @return  a {@link PendingIntent} instance
     */
    private static PendingIntent createIntent (Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ALARM_ACTION);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, 0);

    }
}
