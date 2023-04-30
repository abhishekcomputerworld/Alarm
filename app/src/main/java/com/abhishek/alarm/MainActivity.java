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

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String START_FOREGROUND_ACTION = "start-foreground";
    public static final String STOP_FOREGROUND_ACTION = "stop-foreground";

    private Button mButton;
    private boolean mAlarmEnabled = false;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // toggleAlarm();
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.setAction("com.example.myapp.ALARM_TRIGGER");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1 * 60 * 1000, pendingIntent);
                Toast.makeText(MainActivity.this, "Alarm set in " + 20 + " minute", Toast.LENGTH_LONG).show();

            }
        });


        // If the service is running, we assume the alarm is enabled
       /* if (isServiceRunning(AlarmService.class)) {
            mAlarmEnabled = true;
            mButton.setText("Stop Alarm");
        }*/
    }

    private void toggleAlarm() {
      //  mAlarmEnabled = !mAlarmEnabled;
        if (true) {
            startAlarm();
            mButton.setText("Stop Alarm");
        } else {
            stopAlarm();
            mButton.setText("Start Alarm");
        }
    }

    private void startAlarm2() {
        // Start the foreground service to keep the app running in the background
        Intent serviceIntent = new Intent(this, AlarmService.class);
        serviceIntent.setAction(AlarmService.START_FOREGROUND_ACTION);
        ContextCompat.startForegroundService(this, serviceIntent);

        // Save the alarm state to SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("alarm_enabled", true);
        editor.apply();
    }

    private void stopAlarm2() {
        // Stop the foreground service to allow the app to be closed
        Intent serviceIntent = new Intent(this, AlarmService.class);
        serviceIntent.setAction(AlarmService.STOP_FOREGROUND_ACTION);
        startService(serviceIntent);
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


    private void startAlarm3() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.example.myapp.ALARM_TRIGGER");
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        long timeInterval = 1 * 60 * 1000; // 20 minutes
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), timeInterval, alarmIntent);

        Toast.makeText(this, "Alarm started", Toast.LENGTH_SHORT).show();

    }

    private void stopAlarm3() {
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
            Toast.makeText(this, "Alarm stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void startAlarm() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest alarmWorker =
                new PeriodicWorkRequest.Builder(AlarmWorker.class, 30, TimeUnit.SECONDS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "alarmWorker", ExistingPeriodicWorkPolicy.REPLACE, alarmWorker
        );

        Toast.makeText(this, "Alarm started", Toast.LENGTH_SHORT).show();
    }

    private void stopAlarm() {
        WorkManager.getInstance(this).cancelUniqueWork("alarmWorker");
        Toast.makeText(this, "Alarm stopped", Toast.LENGTH_SHORT).show();
    }
}