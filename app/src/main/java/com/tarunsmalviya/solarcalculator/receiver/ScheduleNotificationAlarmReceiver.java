package com.tarunsmalviya.solarcalculator.receiver;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.tarunsmalviya.solarcalculator.util.CommonMethod;
import com.tarunsmalviya.solarcalculator.util.SunAlgorithm;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ScheduleNotificationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (CommonMethod.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            double[] times = SunAlgorithm.calculateTimes(System.currentTimeMillis(), location.getLatitude(), location.getLongitude());
                            if (times != null && times.length == 2) {
                                registerNotificationAlarm(context, times[0], 1);
                                registerNotificationAlarm(context, times[1], 2);
                            }
                        }
                    });
        }
    }

    private void registerNotificationAlarm(Context context, double millis, int type) {
        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Math.round(millis));

        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        intent.putExtra(NotificationAlarmReceiver.TYPE, type);
        intent.putExtra(NotificationAlarmReceiver.TIME, time.format(calendar.getTime()));
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, Math.round(millis), alarmIntent);
    }
}
