package com.normurodov_nazar.batterynotifier.Activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.normurodov_nazar.batterynotifier.Functions.Hey;
import com.normurodov_nazar.batterynotifier.Functions.Key;
import com.normurodov_nazar.batterynotifier.R;
import com.normurodov_nazar.batterynotifier.Receivers.AlarmReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    TextView percentage, info, startWork, status, adError;
    SharedPreferences preferences;
    ImageView settings;
    boolean charging = false, working = false;
    AdView adView;
    CountDownTimer timer;
    AlarmManager manager;
    PendingIntent pendingIntent;
    ProgressBar progressBar;
    ActivityResultLauncher<Intent> launcher;

    final BroadcastReceiver percentageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int st = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            charging = st == BatteryManager.BATTERY_STATUS_CHARGING;
            if (charging) status.setText(R.string.charging);
            else status.setText(R.string.not_charging_now);
            int l = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int s = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int p = l * 100 / s;
            percentage.setBackgroundColor(getColor(p >= 20 ? R.color.green : R.color.red));
            String x = p + " %";
            percentage.setText(x);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, initializationStatus -> Log.e("status",initializationStatus.toString()));
        getPermission();
    }

    private void initVars() {
        settings = findViewById(R.id.settings);
        settings.setOnClickListener(v -> {
            boolean vibration = preferences.getBoolean(Key.vibration, true);
            Hey.showPopupMenu(this, settings, new ArrayList<>(Arrays.asList(
                    getString(vibration ? R.string.disableVibration : R.string.enableVibration),
                    getString(R.string.alarmMusic)
            )), position -> {
                if (position == 0) preferences.edit().putBoolean(Key.vibration, !vibration).apply();
                else {
                    Intent i = new Intent();
                    i.setType("audio/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    launcher.launch(i);
                }
            });
        });
        progressBar = findViewById(R.id.progressAd);
        adError = findViewById(R.id.adError);
        adError.setOnClickListener(v -> loadBanner());
        status = findViewById(R.id.status);
        adView = findViewById(R.id.adBanner);
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                showError();
                Hey.print("loadAdError",loadAdError.getMessage());
                if (loadAdError.getCode() == 0) adError.setText(getString(R.string.error_network));
                else {
                    String a = getString(R.string.error) + ":" + loadAdError.getMessage();
                    adError.setText(a);
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                showAd();
            }

        });
        loadBanner();
        preferences = Hey.getPreferences(this);
        startWork = findViewById(R.id.startWork);
        startWork.setOnClickListener(v -> onStartWork());
        if (working) startWork.setText(R.string.stop);
        info = findViewById(R.id.infor);
        percentage = findViewById(R.id.percentage);
        registerReceiver(percentageReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setWorking();
                doWithStartWorking();
            }

            @Override
            public void onFinish() {
                if (timer != null) timer.start();
            }
        };
        timer.start();
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
    }

    private void onResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            File file = null;
            try {
                Intent o = result.getData();
                if (o!=null){
                    file = new File(Hey.getPath(this, o.getData()));
                }else Hey.showToast(this,getString(R.string.error));
            } catch (Exception e) {
                e.printStackTrace();
                Hey.showToast(this,e.getLocalizedMessage());
            }
            if (file != null) {
                if (file.exists()) {
                    preferences.edit().putString(Key.path,file.getPath()).apply();
                    Hey.showToast(this, getString(R.string.selected));
                }else Hey.showToast(this, getString(R.string.error));
            }
        }
    }

    private void showError() {
        progressBar.setVisibility(View.INVISIBLE);
        adError.setVisibility(View.VISIBLE);
        adView.setVisibility(View.INVISIBLE);
    }

    private void showAd() {
        progressBar.setVisibility(View.INVISIBLE);
        adError.setVisibility(View.INVISIBLE);
        adView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        adError.setVisibility(View.INVISIBLE);
        adView.setVisibility(View.INVISIBLE);
    }

    private void doWithStartWorking() {
        if (!working) startWork.setText(R.string.start_work);
        else startWork.setText(R.string.started);
    }

    private void loadBanner() {
        showLoading();
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void setWorking() {
        SharedPreferences p = Hey.getPreferences(this);
        long aTime = p.getLong("t", 0), cTime = Calendar.getInstance().getTimeInMillis();
        working = aTime > cTime;
    }

    private void onStartWork() {
        setWorking();
        if (working) {
            startWork.setText(R.string.start_work);
            manager.cancel(pendingIntent);
            preferences.edit().putLong("t", 0).apply();
            Hey.showDialogWithText(this, getString(R.string.stopped), false);
        } else {
            startWork.setText(getString(R.string.stop));
            setAlarm();
            Hey.showDialogWithText(this, getString(R.string.startedNow), true);
        }
        doWithStartWorking();
        working = !working;
    }


    private void setAlarm() {
        Calendar time = Calendar.getInstance();
        time.add(Calendar.SECOND, 10);
        Hey.setAlarm(this, time);
    }

    private void getPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else initVars();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length==1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initVars();
            else {
                Toast.makeText(getApplicationContext(), getString(R.string.per), Toast.LENGTH_SHORT).show();
                finish();
            }
        }else {
            Toast.makeText(getApplicationContext(), getString(R.string.per), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(percentageReceiver);
        timer.cancel();
    }
}