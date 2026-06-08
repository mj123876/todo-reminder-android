package com.codex.todoreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("taskId");
        Task task = TaskStore.find(context, id);
        if (task == null || task.done) return;
        task.notified = true;
        TaskStore.upsert(context, task);
        NotificationHelper.notifyTask(context, task);
    }
}
