package com.normurodov_nazar.batterynotifier.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.normurodov_nazar.batterynotifier.Functions.Hey;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    BatteryManager batteryManager;
    int percentage;

    @Override
    public void onReceive(Context context, Intent intent) {
        batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        percentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        setAlarm(context);
    }

    private void setAlarm(Context context) {
        Calendar time = Calendar.getInstance();
        time.add(Calendar.MINUTE,percentage>90 ? 1 : 10);
        Hey.setAlarm(context,time);
    }
}