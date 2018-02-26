package tech.bam.RNBatchPush;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.batch.android.Batch;

import java.util.ArrayList;

public class PushService extends JobIntentService {
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1002;

    static ArrayList<Integer> ids = new ArrayList<>();

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, PushService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (Batch.Push.shouldDisplayPush(this, intent)) // Check that the push is valid
        {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "cleochannel");
            // Build your own notification here...

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                //CharSequence name = getString(R.string.channel_name);
                //String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("cleochannel", "Cleo", importance);
                //channel.setDescription(description);
                // Register the channel with the system
                NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                m.createNotificationChannel(channel);
            }

            // Assuming you have a drawable named notification_icon, can otherwise be anything you want
            builder.setContentTitle(intent.getStringExtra(Batch.Push.TITLE_KEY))
                    .setContentText(intent.getStringExtra(Batch.Push.ALERT_KEY))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // Create intent
            Intent launchIntent = new Intent(); // Create your own intent
            Batch.Push.appendBatchData(intent, launchIntent); // Call this method to add tracking data to your intent to track opens

            // Finish building the notification using the launchIntent
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Display your notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // "id" is supposed to be a unique id, in order to be able to update the notification if you want.
            // If you don't care about updating it, you can simply make a random it, like below
            int id = (int) (Math.random() * Integer.MAX_VALUE);
            ids.add(id);
            notificationManager.notify(id, builder.build());

            // Call Batch to keep track of that notification
            Batch.Push.onNotificationDisplayed(this, intent);
        }
    }
}
