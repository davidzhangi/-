package com.david.blesample;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;

/**
 * Created by david on 2019-12-03.
 */
public class AppApplication extends Application {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CrashReport.initCrashReport(getApplicationContext(), "562520c0ee", BuildConfig.DEBUG);
        String rootDir = MMKV.initialize(this);
        System.out.println("mmkv root: " + rootDir);
    }

}
