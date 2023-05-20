package com.abhishek.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "alarm_channel";

    public static final String START_FOREGROUND_ACTION = "start-foreground";
    public static final String STOP_FOREGROUND_ACTION = "stop-foreground";
    private NotificationManager mNotificationManager;

    private static final long INTERVAL_MILLIS = 20 * 10 * 1000; // 20 minutes
    private static final int NOTIFICATION_UPDATE_INTERVAL_MILLIS = 1000;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            playAlarm();
            mHandler.postDelayed(this, INTERVAL_MILLIS);
        }
    };
    private NotificationCompat.Builder mNotificationBuilder;

    private boolean mIsForegroundServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler.postDelayed(mRunnable, INTERVAL_MILLIS);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(START_FOREGROUND_ACTION)) {
                startForegroundService();
                mIsForegroundServiceRunning = true;
            } else if (action != null && action.equals(STOP_FOREGROUND_ACTION)) {
                stopForegroundService();
                mIsForegroundServiceRunning = false;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service, so we return null
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null)  mediaPlayer.stop();
        mHandler.removeCallbacks(mRunnable);
    }

    private void playAlarm() {
      /* Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(this, notificationUri);
        ringtone.play();*/
      /*

       AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.example.myapp.ALARM_TRIGGER");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1 * 30 * 1000, pendingIntent);
      //  Toast.makeText(this, "Alarm set in " + 1 + " seconds",Toast.LENGTH_LONG).show();
 */
        mediaPlayer=MediaPlayer.create(this, R.raw.alarm_clock);
        mediaPlayer.start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Alarm Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private void startForegroundService() {
        mNotificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Alarm is running")
                .setContentText("Click to stop alarm")
                .setContentIntent(createStopAlarmIntent())
                .setOngoing(true);

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void stopForegroundService() {
      if(mediaPlayer!=null)  mediaPlayer.stop();
      stopForeground(true);
    }

    private PendingIntent createStopAlarmIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(STOP_FOREGROUND_ACTION);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
    }


}
