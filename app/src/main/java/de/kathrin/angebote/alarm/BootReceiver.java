package de.kathrin.angebote.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import de.kathrin.angebote.utlis.NotificationUtils;

import static de.kathrin.angebote.utlis.Strings.BOOT_COMPLETE_ACTION;

/**
 * Register the alarm after a reboot.
 */
public class BootReceiver extends BroadcastReceiver {

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
