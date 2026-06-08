package com.codex.todoreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

public class NotificationHelper {
    public static final String CHANNEL_ID = "task_reminders";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "寰呭姙鎻愰啋", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("寰呭姙浜嬮」鎻愬墠鎻愰啋");
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.rgb(37, 99, 235));
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    public static void notifyTask(Context context, Task task) {
        createChannel(context);
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent openIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode(),
            open,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        String type = "deadline".equals(task.type) ? "鎴" : "寮€濮?;
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("寰呭姙鎻愰啋锛? + task.title)
            .setContentText(type + "鏃堕棿鍒颁簡鎴栧嵆灏嗗埌杈?)
            .setAutoCancel(true)
            .setContentIntent(openIntent);
        if (Build.VERSION.SDK_INT < 26) {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(task.id.hashCode(), builder.build());
    }
}
