package com.codex.todoreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

public class ReminderScheduler {
    public static void schedule(Context context, Task task) {
        if (task == null || task.done || task.id == null) return;
        long triggerAt = Math.max(System.currentTimeMillis() + 1000, task.at - task.remindMs);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("taskId", task.id);
        PendingIntent pending = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
            } else {
                manager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending);
            }
        } catch (SecurityException ex) {
            if (Build.VERSION.SDK_INT >= 23) manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
            else manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending);
        }
    }

    public static void cancel(Context context, String id) {
        if (id == null) return;
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) manager.cancel(pending);
    }

    public static void scheduleAll(Context context) {
        List<Task> tasks = TaskStore.load(context);
        for (Task task : tasks) {
            if (!task.done && !task.notified) schedule(context, task);
        }
    }
}
