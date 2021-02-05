package de.kathrin.angebote.utlis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static de.kathrin.angebote.utlis.Strings.ALARM_FILE;
import static de.kathrin.angebote.utlis.Strings.DATE_FORMAT_ALARM;
import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;

public class NotificationUtils {

    private static final String LOG_TAG = PROJECT_NAME + NotificationUtils.class.getSimpleName();

    // PUBLIC FUNCTIONS

    /**
     * Create a Calendar Instance having the next monday, 9 o'clock as a date.
     * @return  Calendar instance
     */
    public static Calendar getNextMonday () {

        Calendar date = Calendar.getInstance();

        date.set(Calendar.DAY_OF_WEEK, 2);
        date.set(Calendar.HOUR_OF_DAY, 9);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.add(Calendar.DATE, 7);

        return date;
    }

    /**
     * Create a Calendar instance having the next day, 9 o'clock as a date.
     * @return  Calendar instance
     */
    public static Calendar getTomorrow () {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 9);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.add(Calendar.DAY_OF_YEAR, 1);
        return date;

    }

    /**
     * Restore the alarm date from the file
     * @param context   current context
     * @return  Calender instance, date read from the file
     */
    public static Calendar getAlarmDateFromFile (Context context) {
        String dateString = IOUtils.restoreStringFromFile(context, ALARM_FILE);
        Calendar date =  convertStringToCalendar(dateString);

        Log.v(LOG_TAG, "Got alarm " + date.getTime() + " from file.");
        return date;
    }

    /**
     * Write the alarm date to the file
     * @param context   current context
     * @param date      date to write to the file
     */
    public static void writeAlarmToFile (Context context, Calendar date) {
        Log.v(LOG_TAG, "Write " +  date.getTime() + " to file");

        String dateString = convertCalendarToString(date);
        IOUtils.saveStringInFile(context, dateString, ALARM_FILE);
    }

    /**
     * Clear the alarm date from the file.
     * @param context   current context
     */
    public static void clearAlarmFile (Context context) {
        IOUtils.saveStringInFile(context, "", ALARM_FILE);
        Log.v(LOG_TAG, "Cleared alarm file.");
    }

    /**
     * Check if there is an alarm set in the file
     * @param context   current context
     * @return  boolean, true if an alarm is set
     */
    public static boolean alarmIsSet (Context context) {
        String dateString = IOUtils.restoreStringFromFile(context, ALARM_FILE);

        return !dateString.equals("");
    }

    // PRIVATE FUNCTIONS

    /**
     * Convert a string into a calendar instance
     * @param dateString    string to convert
     * @return  calendar instance
     */
    @SuppressLint("SimpleDateFormat")
    private static Calendar convertStringToCalendar (String dateString) {
        Calendar calendar = Calendar.getInstance();

        try {
            Date date = new SimpleDateFormat(DATE_FORMAT_ALARM).parse(dateString);

            if (date != null) {
                calendar.setTime(date);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar;
    }

    /**
     * Convert a calendar instance into a string
     * @param calendar  calendar instance to convert
     * @return  string
     */
    @SuppressLint("SimpleDateFormat")
    private static String convertCalendarToString (Calendar calendar) {
        return new SimpleDateFormat(DATE_FORMAT_ALARM).format(calendar.getTime());
    }


}
