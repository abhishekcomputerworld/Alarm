package com.abhishek.alarm;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AlarmWorker extends Worker {
    private static final String NOTIFICATION_CHANNEL_ID = "alarm_channel";

    private MediaPlayer mediaPlayer;
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 1;

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {
        playSound();
        showNotification(getApplicationContext(), "Alarm", "Alarm is ringing");
        return Result.success();
    }

    private void playSound() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.civil_defense);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
    private void showNotification(Context context, String title, String message) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(defaultSoundUri)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
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

    @Override
    public void onStopped() {
        super.onStopped();
        stopSound();
    }
}
