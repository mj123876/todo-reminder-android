package com.codex.todoreminder;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskStore {
    private static final String PREFS = "tasks";
    private static final String KEY = "items";

    public static List<Task> load(Context context) {
        ArrayList<Task> result = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Task task = new Task();
                task.id = item.optString("id");
                task.title = item.optString("title");
                task.type = item.optString("type", "deadline");
                task.at = item.optLong("at");
                task.remindMs = item.optLong("remindMs");
                task.remindAmount = item.optInt("remindAmount", 30);
                task.remindUnit = item.optInt("remindUnit", 0);
                task.notes = item.optString("notes");
                task.done = item.optBoolean("done");
                task.notified = item.optBoolean("notified");
                result.add(task);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public static void save(Context context, List<Task> tasks) {
        JSONArray array = new JSONArray();
        for (Task task : tasks) {
            JSONObject item = new JSONObject();
            try {
                item.put("id", task.id);
                item.put("title", task.title);
                item.put("type", task.type);
                item.put("at", task.at);
                item.put("remindMs", task.remindMs);
                item.put("remindAmount", task.remindAmount);
                item.put("remindUnit", task.remindUnit);
                item.put("notes", task.notes);
                item.put("done", task.done);
                item.put("notified", task.notified);
                array.put(item);
            } catch (Exception ignored) {
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, array.toString()).apply();
    }

    public static Task find(Context context, String id) {
        for (Task task : load(context)) {
            if (task.id != null && task.id.equals(id)) return task;
        }
        return null;
    }

    public static void upsert(Context context, Task task) {
        List<Task> tasks = load(context);
        boolean updated = false;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).id.equals(task.id)) {
                tasks.set(i, task);
                updated = true;
                break;
            }
        }
        if (!updated) tasks.add(task);
        save(context, tasks);
    }

    public static void delete(Context context, String id) {
        List<Task> tasks = load(context);
        tasks.removeIf(task -> task.id != null && task.id.equals(id));
        save(context, tasks);
    }
}
