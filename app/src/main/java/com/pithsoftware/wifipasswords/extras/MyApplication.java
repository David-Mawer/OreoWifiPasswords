package com.pithsoftware.wifipasswords.extras;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;

import com.pithsoftware.wifipasswords.R;
import com.pithsoftware.wifipasswords.database.PasswordDB;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MyApplication extends Application {

    private static MyApplication sInstance;
    private static PasswordDB mPasswordDB;
    private static AtomicInteger mOpenCounter = new AtomicInteger();

    public static boolean mAppWentBackground;

    public static final String FIRST_LAUNCH = "first_launch";
    public static final String DEVICE_UUID = "uuid";

    public static final String NO_PASSWORD_TEXT = "no password";

    public static boolean sIsDark;
    public static String sMyUUID;
    public static boolean sShouldAutoUpdateList;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mPasswordDB = new PasswordDB(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Generate UUID for use with Crashlytics
        if(sharedPreferences.getBoolean(FIRST_LAUNCH, true)) {
            generateUUID();
        } else {
            sMyUUID = sharedPreferences.getString(DEVICE_UUID, "");
        }


        mAppWentBackground = true;
        sShouldAutoUpdateList = true;

        sIsDark = sharedPreferences.getBoolean(getString(R.string.pref_dark_theme_key), false);
    }


    public static MyApplication getInstance() {
        return sInstance;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

    public synchronized static PasswordDB getWritableDatabase() {

        if (mPasswordDB == null && mOpenCounter.incrementAndGet() == 1) {
            mPasswordDB = new PasswordDB(getAppContext());
        }
        return mPasswordDB;
    }

    public synchronized static void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            mPasswordDB.close();
        }

    }

    public static void darkTheme(CheckBoxPreference preference) {
        sIsDark = preference.isChecked();
    }

    private void generateUUID() {

        sMyUUID = UUID.randomUUID().toString();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(DEVICE_UUID, sMyUUID).apply();

    }

}
