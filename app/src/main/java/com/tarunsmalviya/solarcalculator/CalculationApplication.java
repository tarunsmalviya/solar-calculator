package com.tarunsmalviya.solarcalculator;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class CalculationApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("location.realm")
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
