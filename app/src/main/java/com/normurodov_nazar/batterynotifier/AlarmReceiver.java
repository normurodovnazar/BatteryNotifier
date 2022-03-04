package com.normurodov_nazar.batterynotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.os.Vibrator;

import java.io.File;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    SharedPreferences preferences;
    BatteryManager batteryManager;
    boolean isFull;
    int percentage;
    Calendar time;
    Context context;

    AudioManager audioManager;
    MediaPlayer mediaPlayer;
    Vibrator vibrator;
    CountDownTimer vibrateTimer,volumeTimer,stopTimer;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        preferences = Hey.getPreferences(context);
        isFull = preferences.getBoolean("f",false);
        batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        percentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        time= Calendar.getInstance();
        if (isFull && percentage==100) setAlarmLonger(); else
            if (percentage == 100) notifyUser(); else setAlarmDependBattery();
    }

    private void setAlarmDependBattery() {
        if (isFull) preferences.edit().putBoolean("f",false).apply();
        if (percentage==99) time.add(Calendar.MINUTE,1); else time.add(Calendar.SECOND,(100-percentage)*30);
        Hey.setAlarm(context,time);
    }

    private void notifyUser() {
        preferences.edit().putBoolean("f",true).putBoolean("needStop",false).apply();
        setAlarmLonger();
        initializeAll();
        startTimers();
        Hey.showNotification(context);
    }

    private void initializeAll() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);vibrator.vibrate(500);
        File file = new File(preferences.getString(Key.path,"as"));
        if (file.exists()){
            Uri uri = Uri.fromFile(file);
            playCustomMusic(uri);
        }else {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (uri!=null) playCustomMusic(uri);
            else playDefaultMusic();
        }
        vibrateTimer = new CountDownTimer(10000,2000) {
            @Override
            public void onTick(long l) {
                vibrator.vibrate(1000);
            }

            @Override
            public void onFinish() {
                if (vibrateTimer !=null) vibrateTimer.start();
            }
        };
        volumeTimer = new CountDownTimer(5000,1500) {
            @Override
            public void onTick(long l) {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE,AudioManager.FLAG_PLAY_SOUND);
            }

            @Override
            public void onFinish() {
                if (volumeTimer !=null) volumeTimer.start();
            }
        };
        stopTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                SharedPreferences preferences = context.getSharedPreferences("a",Context.MODE_PRIVATE);
                if (preferences.getBoolean("needStop",false)) stopAll();
            }

            @Override
            public void onFinish() {
                if (stopTimer!=null) stopTimer.start();
            }
        };
    }

    private void playCustomMusic(Uri uri) {
        mediaPlayer = MediaPlayer.create(context,uri);
        mediaPlayer.setOnPreparedListener(mp -> {
            if (mediaPlayer!=null) mediaPlayer.start(); else playDefaultMusic();
        });
    }

    private void startTimers() {
        if (Hey.getPreferences(context).getBoolean(Key.vibration,true)) vibrateTimer.start();
        volumeTimer.start();
        stopTimer.start();
    }

    private void playDefaultMusic(){
        mediaPlayer = MediaPlayer.create(context,R.raw.m);
        mediaPlayer.setOnPreparedListener(mp -> {
            if (mediaPlayer!=null) mediaPlayer.start();
        });
    }

    private void stopAll() {
        mediaPlayer.stop();mediaPlayer.release();
        if (vibrateTimer!=null)vibrateTimer.cancel();
        volumeTimer.cancel();
        stopTimer.cancel();
    }

    private void setAlarmLonger() {
        time.add(Calendar.HOUR,1);
        Hey.setAlarm(context,time);
    }
}