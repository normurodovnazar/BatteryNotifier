package com.normurodov_nazar.batterynotifier;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

public class RingStopper extends AppCompatActivity {
    TextView text;
    ImageView image;
    SharedPreferences preferences;
    NotificationManager manager;
    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_stopper);
        registerReceiver(receiver,new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        text = findViewById(R.id.full);
        image = findViewById(R.id.imageAlarm);
        preferences = Hey.getPreferences(this);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        animateAll();
    }

    private void animateAll() {
        AlphaAnimation animation = new AlphaAnimation(1,0);
        animation.setDuration(500);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        image.setAnimation(animation);
        text.setAnimation(animation);
        animation.start();
    }


    private void stopR() {
        preferences.edit().putBoolean(Key.needStop,true).apply();
        manager.cancelAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        stopR();
    }
}