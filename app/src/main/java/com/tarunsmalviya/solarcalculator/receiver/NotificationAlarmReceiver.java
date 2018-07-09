package com.tarunsmalviya.solarcalculator.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.tarunsmalviya.solarcalculator.R;

public class NotificationAlarmReceiver extends BroadcastReceiver {

    public static final String TYPE = "type";
    public static final String TIME = "time";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras().containsKey(TYPE) && intent.getExtras().containsKey(TIME)) {
            int type = intent.getExtras().getInt(TYPE);
            String time = intent.getExtras().getString(TIME);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, null)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), type == 1 ? R.drawable.ic_sunrise : R.drawable.ic_sunset))
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText((type == 1 ? "Sunrise" : "Sunset") + " starts at " + time)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(type == 1 ? 101 : 202, mBuilder.build());
        }
    }
}
