package com.example.calendar2;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.net.URI;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent receivedintent) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);

        SharedPreferences sharedPref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        Uri alarmUri = null;
        try {
            alarmUri = Uri.parse(sharedPref.getString("uri", ""));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if(alarmUri == null || alarmUri.toString() == ""){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        final MediaPlayer mediaPlayer = MediaPlayer.create(context,alarmUri);
        mediaPlayer.start();

        long[] pattern = {0, 1000, 100, 1000, 100};

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.EFFECT_DOUBLE_CLICK));

        String title = "Incoming Event";
        String description = " There is an incoming event";
        if(receivedintent.hasExtra("eventName"))
            title = receivedintent.getStringExtra("eventName");
        if(receivedintent.hasExtra("eventDescription"))
            description = receivedintent.getStringExtra("eventDescription");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.calendar_icon)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define

        notificationManager.notify(757, builder.build());
    }
}
