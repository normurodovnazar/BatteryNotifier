package com.normurodov_nazar.batterynotifier;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StopRingReceiver extends BroadcastReceiver {
    SharedPreferences preferences;
    NotificationManager manager;
    @Override
    public void onReceive(Context context, Intent intent) {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        preferences = Hey.getPreferences(context);
        preferences.edit().putBoolean(Key.needStop,true).apply();
        manager.cancelAll();
    }
}