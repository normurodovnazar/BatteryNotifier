package com.normurodov_nazar.batterynotifier.Receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.normurodov_nazar.batterynotifier.Functions.Hey;
import com.normurodov_nazar.batterynotifier.Functions.Key;

public class StopRingReceiver extends BroadcastReceiver {
    NotificationManager manager;
    @Override
    public void onReceive(Context context, Intent intent) {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Hey.getPreferences(context).edit().putBoolean(Key.needStop,true).apply();
        manager.cancelAll();
    }
}