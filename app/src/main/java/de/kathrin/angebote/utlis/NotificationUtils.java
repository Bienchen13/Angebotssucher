package de.kathrin.angebote.utlis;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.kathrin.angebote.MainActivity;

public class NotificationUtils {

    static final String ALARM_FILE = "alarm.txt";

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + NotificationUtils.class.getSimpleName();
    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

    public static Calendar getNextMonday () {

        Calendar date = Calendar.getInstance();

        date.set(Calendar.DAY_OF_WEEK, 2);
        date.set(Calendar.HOUR_OF_DAY, 9);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.add(Calendar.DATE, 7);

        return date;
    }

    public static Calendar getAlarmDateFromFile (Context context) {
        String dateString = IOUtils.restoreStringFromFile(context, ALARM_FILE);
        Calendar date =  convertStringToCalendar(dateString);

        Log.v(LOG_TAG, "Got alarm " + date.getTime() + " from file.");
        return date;
    }

    public static void writeAlarmToFile (Context context, Calendar date) {
        Log.v(LOG_TAG, "Write " +  date.getTime() + " to file");

        String dateString = convertCalendarToString(date);
        IOUtils.saveStringInFile(context, dateString, ALARM_FILE);
    }

    public static void clearAlarmFile (Context context) {
        IOUtils.saveStringInFile(context, "", ALARM_FILE);
        Log.v(LOG_TAG, "Cleared alarm file.");
    }

    public static boolean alarmIsSet (Context context) {
        String dateString = IOUtils.restoreStringFromFile(context, ALARM_FILE);

        return !dateString.equals("");
    }

    private static Calendar convertStringToCalendar (String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        try {
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String convertCalendarToString (Calendar calendar) {
        return new SimpleDateFormat(DATE_FORMAT).format(calendar.getTime());
    }


}
