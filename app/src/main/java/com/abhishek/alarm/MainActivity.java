package com.abhishek.alarm;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String START_FOREGROUND_ACTION = "start-foreground";
    public static final String STOP_FOREGROUND_ACTION = "stop-foreground";

    private Button mButton;
    private boolean mAlarmEnabled = false;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mButton = findViewById(R.id.button);
        mAlarmEnabled = preferences.getBoolean("alarm_enabled",false);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlarmEnabled = preferences.getBoolean("alarm_enabled",false);
                toggleAlarm();
            }
        });


        // If the service is running, we assume the alarm is enabled
        if (mAlarmEnabled) {
            mButton.setText("Stop Alarm");
        }else{
            mButton.setText("Start Alarm");
        }
    }

    private void toggleAlarm() {
       // startAlarm();
         if (!mAlarmEnabled) {
            startAlarm();
            mButton.setText("Stop Alarm");
        } else {
            stopAlarm();
            mButton.setText("Start Alarm");
        }
    }



    private boolean isServiceRunning(Class<? extends Service> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlarmReceiverRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if (serviceInfo.service.getClassName().equals(AlarmReceiver.class.getName())) {
                return true;
            }
        }
        return false;
    }
    private void stopAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction("com.example.myapp.ALARM_TRIGGER");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("alarm_enabled", false);
            editor.commit(); // Use commit instead of apply
            Toast.makeText(this, "Alarm stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void startAlarm() {
       /* Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest alarmWorker = new PeriodicWorkRequest.Builder(AlarmWorker.class, 30, TimeUnit.SECONDS).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("alarmWorker", ExistingPeriodicWorkPolicy.REPLACE, alarmWorker);
        Toast.makeText(this, "Alarm started", Toast.LENGTH_SHORT).show();*/

         alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.setAction("com.example.myapp.ALARM_TRIGGER");
         alarmIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 20 * 60 * 1000, alarmIntent);
        Toast.makeText(MainActivity.this, "Alarm set in " + 20 + " minute", Toast.LENGTH_LONG).show();

        // Save the alarm state to SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("alarm_enabled", true);
        editor.apply();
    }


}