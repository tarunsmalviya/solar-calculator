package com.tarunsmalviya.solarcalculator;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tarunsmalviya.solarcalculator.receiver.ScheduleNotificationAlarmReceiver;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class CalculationApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initRealm();
        registerAlarm();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("location.realm")
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);
    }

    private void registerAlarm() {
        Intent intent = new Intent(getApplicationContext(), ScheduleNotificationAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 00);
        c.set(Calendar.MINUTE, 10);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }
}
