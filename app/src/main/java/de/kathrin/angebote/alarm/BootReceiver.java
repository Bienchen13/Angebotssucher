package de.kathrin.angebote.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import de.kathrin.angebote.utlis.NotificationUtils;

/**
 * Register the alarm after a reboot.
 */
public class BootReceiver extends BroadcastReceiver {

    // Notification from the OS
    public static final String BOOT_COMPLETE_ACTION = "android.intent.action.BOOT_COMPLETED";

    /**
     * Set the alarmHandler new after a reboot
     * @param context   current context
     * @param intent    not used
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // Only set the alarm handler new after a reboot
        if (BOOT_COMPLETE_ACTION.equals(intent.getAction())) {
            if (NotificationUtils.alarmIsSet(context)) {
                Calendar date = NotificationUtils.getAlarmDateFromFile(context);
                AlarmHandler.setAlarm(context, date);
            }
        }

    }
}
