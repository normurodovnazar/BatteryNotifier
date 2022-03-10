package com.normurodov_nazar.batterynotifier.Functions;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.normurodov_nazar.batterynotifier.Receivers.AlarmReceiver;
import com.normurodov_nazar.batterynotifier.R;
import com.normurodov_nazar.batterynotifier.Activities.RingStopper;
import com.normurodov_nazar.batterynotifier.Receivers.StopRingReceiver;

import java.util.ArrayList;
import java.util.Calendar;

public class Hey{
    public static SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences("a",Context.MODE_PRIVATE);
    }

    public static void setAlarm(Context context, Calendar time) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putLong("t", time.getTimeInMillis()).apply();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
        manager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }

    public static void showNotification(Context context) {
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context,0,new Intent(context, StopRingReceiver.class),PendingIntent.FLAG_MUTABLE|PendingIntent.FLAG_ONE_SHOT);
        }else {
            pendingIntent = PendingIntent.getBroadcast(context,0,new Intent(context, StopRingReceiver.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_ONE_SHOT);
        }
        PendingIntent activity;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            activity = PendingIntent.getActivity(context,0,new Intent(context, RingStopper.class),PendingIntent.FLAG_MUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        }else {
            activity = PendingIntent.getActivity(context,0,new Intent(context, RingStopper.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        }
        NotificationCompat.Action action = new NotificationCompat.Action(null,context.getString(R.string.stop_ring),pendingIntent);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "full")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_a)
                .setContentText(context.getString(R.string.full))
                .setContentTitle(context.getString(R.string.stop_ring)).addAction(action).setColor(context.getColor(R.color.blue))
                .setPriority(NotificationCompat.PRIORITY_HIGH).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(activity,true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("full","Title",NotificationManager.IMPORTANCE_HIGH);
            builder.setChannelId("full");
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, builder.build());
    }

    public static void showDialogWithText(Activity activity,String s, boolean closeApp) {
        new AlertDialog.Builder(activity)
                .setMessage(s)
                .setTitle("")
                .setNegativeButton("OK", (dialog, which) ->{
                    if(closeApp) activity.finish();
                }).setCancelable(false).show();
    }

    public static void showPopupMenu(Context context, View item, ArrayList<String> items, ItemClickListener itemClickListener) {
        PopupMenu menu = new PopupMenu(context, item);
        for (String s : items) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, items.indexOf(s), s);
        }
        menu.setOnMenuItemClickListener(item1 -> {
            itemClickListener.onItemClick(item1.getOrder());
            return true;
        });
        menu.show();
    }

    public static void print(String tag,String message){
        Log.e(tag,message);
    }

    public static void showToast(Context context,String text){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static String getPath(Context context,Uri uri) throws Exception{
        String[] p = {MediaStore.Audio.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri,p,null,null,null);
        if (cursor!=null){
            int columnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            String data;
            try {
                data = cursor.getString(columnIndex);
            }catch (Exception e){
                throw new Exception(e.getLocalizedMessage());
            }
            cursor.close();
            if (data==null) data = "as";
            return data;
        }else return "asd";
    }
}
