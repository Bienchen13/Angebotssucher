package de.kathrin.angebote;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static de.kathrin.angebote.MainActivity.RESULT_OK;

/**
 * From: https://developer.android.com/training/notify-user/build-notification
 * Create and display the notifications (and the channel).
 */
public class NotificationController {
    private static final String LOG_TAG = MainActivity.PROJECT_NAME + NotificationController.class.getSimpleName();
    private static final String CHANNEL_ID = "1";

    private static int notificationId = 0;
    private Context context;

    /**
     * Create Notification channel
     * @param context   the current context
     */
    public NotificationController (Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Creates and displays a new notification
     */
    public void addNewNotification (String title, String description) {

        // Create an explicit intent for an Activity in your app (where to go, when the
        // notification is tapped by the user)
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, RESULT_OK, intent, 0);

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                //.setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Without this, the notification is truncated to one line
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description))
                // Set the intent that will fire when the user taps the notification
                //.setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId has to be unique for each notification,
        notificationManager.notify(notificationId++, builder.build());

        Log.v(LOG_TAG, "New notification created");

    }

    /**
     * Has to be called in the beginning to send notifications.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Channel";
            String description = "Channel to publish the product notification.";

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Log.v(LOG_TAG, "Notification channel created.");
    }

}
