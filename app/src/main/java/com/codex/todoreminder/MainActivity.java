package com.codex.todoreminder;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int DARK = Color.rgb(17, 24, 39);
    private static final int MUTED = Color.rgb(100, 116, 139);

    private EditText titleInput;
    private EditText remindAmountInput;
    private EditText notesInput;
    private Spinner remindUnitSpinner;
    private RadioGroup typeGroup;
    private Button dateButton;
    private Button saveButton;
    private LinearLayout listLayout;
    private Calendar selectedTime;
    private String editingId;
    private String filter = "all";
    private final SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.createChannel(this);
        selectedTime = Calendar.getInstance();
        selectedTime.add(Calendar.HOUR_OF_DAY, 1);
        buildUi();
        requestNotificationPermissionIfNeeded();
        refreshList();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(24));
        scroll.addView(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        TextView title = text("寰呭姙鎻愰啋", 30, DARK, true);
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button notifyButton = button("閫氱煡鏉冮檺");
        notifyButton.setOnClickListener(v -> requestNotificationPermissionIfNeeded());
        header.addView(notifyButton);
        root.addView(header);

        titleInput = input("渚嬪锛氭彁浜ゅ疄楠屾姤鍛?);
        root.addView(label("浜嬮」"));
        root.addView(titleInput);

        typeGroup = new RadioGroup(this);
        typeGroup.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton deadline = radio("鏈夋埅姝㈡棩鏈?, "deadline");
        RadioButton start = radio("鏈夊紑濮嬫棩鏈?, "start");
        typeGroup.addView(deadline, new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        typeGroup.addView(start, new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        deadline.setChecked(true);
        root.addView(typeGroup);

        root.addView(label("鏃堕棿"));
        dateButton = button(formatter.format(selectedTime.getTime()));
        dateButton.setOnClickListener(v -> pickDateTime());
        root.addView(dateButton);

        LinearLayout remindRow = new LinearLayout(this);
        remindRow.setOrientation(LinearLayout.HORIZONTAL);
        remindAmountInput = input("30");
        remindAmountInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        remindAmountInput.setText("30");
        remindUnitSpinner = new Spinner(this);
        remindUnitSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"鍒嗛挓", "灏忔椂", "澶?}));
        LinearLayout amountBox = verticalBox("鎻愬墠", remindAmountInput);
        LinearLayout unitBox = verticalBox("鍗曚綅", remindUnitSpinner);
        remindRow.addView(amountBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        remindRow.addView(unitBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(remindRow);

        notesInput = input("澶囨敞锛屽彲閫?);
        notesInput.setMinLines(2);
        root.addView(label("澶囨敞"));
        root.addView(notesInput);

        saveButton = button("淇濆瓨寰呭姙");
        saveButton.setTextColor(Color.WHITE);
        saveButton.setBackgroundColor(BLUE);
        saveButton.setOnClickListener(v -> saveTask());
        root.addView(saveButton, blockParams());

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        addTab(tabs, "鍏ㄩ儴", "all");
        addTab(tabs, "鎴", "deadline");
        addTab(tabs, "寮€濮?, "start");
        addTab(tabs, "瀹屾垚", "done");
        root.addView(tabs);

        listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        root.addView(listLayout);
        setContentView(scroll);
    }

    private void addTab(LinearLayout tabs, String text, String value) {
        Button tab = button(text);
        tab.setOnClickListener(v -> {
            filter = value;
            refreshList();
        });
        tabs.addView(tab, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    }

    private void pickDateTime() {
        DatePickerDialog dateDialog = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedTime.set(Calendar.YEAR, year);
            selectedTime.set(Calendar.MONTH, month);
            selectedTime.set(Calendar.DAY_OF_MONTH, day);
            TimePickerDialog timeDialog = new TimePickerDialog(this, (timeView, hour, minute) -> {
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                selectedTime.set(Calendar.SECOND, 0);
                dateButton.setText(formatter.format(selectedTime.getTime()));
            }, selectedTime.get(Calendar.HOUR_OF_DAY), selectedTime.get(Calendar.MINUTE), true);
            timeDialog.show();
        }, selectedTime.get(Calendar.YEAR), selectedTime.get(Calendar.MONTH), selectedTime.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "璇峰～鍐欎簨椤?, Toast.LENGTH_SHORT).show();
            return;
        }

        String type = selectedType();
        int amount = parseInt(remindAmountInput.getText().toString(), 0);
        int unitPos = remindUnitSpinner.getSelectedItemPosition();
        long unitMs = unitPos == 1 ? 3600000L : unitPos == 2 ? 86400000L : 60000L;

        Task task = editingId == null ? new Task() : TaskStore.find(this, editingId);
        if (task == null) task = new Task();
        if (task.id == null) task.id = UUID.randomUUID().toString();
        task.title = title;
        task.type = type;
        task.at = selectedTime.getTimeInMillis();
        task.remindAmount = amount;
        task.remindUnit = unitPos;
        task.remindMs = amount * unitMs;
        task.notes = notesInput.getText().toString().trim();
        task.notified = false;

        TaskStore.upsert(this, task);
        ReminderScheduler.schedule(this, task);
        clearForm();
        refreshList();
        Toast.makeText(this, "宸蹭繚瀛?, Toast.LENGTH_SHORT).show();
    }

    private void refreshList() {
        listLayout.removeAllViews();
        List<Task> tasks = TaskStore.load(this);
        tasks.sort(Comparator.comparingLong(t -> t.at));
        int count = 0;
        for (Task task : tasks) {
            if (!matchesFilter(task)) continue;
            listLayout.addView(taskView(task));
            count++;
        }
        if (count == 0) {
            TextView empty = text("杩樻病鏈夊緟鍔?, 18, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(28), 0, dp(28));
            listLayout.addView(empty);
        }
    }

    private boolean matchesFilter(Task task) {
        if ("done".equals(filter)) return task.done;
        if ("deadline".equals(filter) || "start".equals(filter)) return !task.done && filter.equals(task.type);
        return !task.done;
    }

    private View taskView(Task task) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        box.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams params = blockParams();
        params.setMargins(0, dp(8), 0, dp(8));
        box.setLayoutParams(params);

        CheckBox cb = new CheckBox(this);
        cb.setText(task.title);
        cb.setTextSize(18);
        cb.setTextColor(task.done ? MUTED : DARK);
        cb.setChecked(task.done);
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.done = isChecked;
            TaskStore.upsert(this, task);
            if (isChecked) ReminderScheduler.cancel(this, task.id);
            else ReminderScheduler.schedule(this, task);
            refreshList();
        });
        box.addView(cb);

        String typeText = "deadline".equals(task.type) ? "鎴" : "寮€濮?;
        TextView meta = text(typeText + "锛? + formatter.format(task.at) + "\n鎻愰啋锛氭彁鍓?" + task.remindAmount + unitName(task.remindUnit), 14, MUTED, false);
        box.addView(meta);
        if (task.notes != null && !task.notes.isEmpty()) {
            box.addView(text(task.notes, 14, DARK, false));
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button edit = button("缂栬緫");
        edit.setOnClickListener(v -> editTask(task));
        Button delete = button("鍒犻櫎");
        delete.setTextColor(Color.rgb(153, 27, 27));
        delete.setOnClickListener(v -> {
            ReminderScheduler.cancel(this, task.id);
            TaskStore.delete(this, task.id);
            refreshList();
        });
        actions.addView(edit, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        actions.addView(delete, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.addView(actions);
        return box;
    }

    private void editTask(Task task) {
        editingId = task.id;
        titleInput.setText(task.title);
        ((RadioButton) typeGroup.findViewWithTag(task.type)).setChecked(true);
        selectedTime.setTimeInMillis(task.at);
        dateButton.setText(formatter.format(selectedTime.getTime()));
        remindAmountInput.setText(String.valueOf(task.remindAmount));
        remindUnitSpinner.setSelection(task.remindUnit);
        notesInput.setText(task.notes);
        saveButton.setText("鏇存柊寰呭姙");
    }

    private void clearForm() {
        editingId = null;
        titleInput.setText("");
        notesInput.setText("");
        remindAmountInput.setText("30");
        remindUnitSpinner.setSelection(0);
        ((RadioButton) typeGroup.findViewWithTag("deadline")).setChecked(true);
        selectedTime = Calendar.getInstance();
        selectedTime.add(Calendar.HOUR_OF_DAY, 1);
        dateButton.setText(formatter.format(selectedTime.getTime()));
        saveButton.setText("淇濆瓨寰呭姙");
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 20);
        }
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (manager != null && !manager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                    .setTitle("鍏佽绮剧‘鎻愰啋")
                    .setMessage("涓轰簡鎸変綘璁剧疆鐨勬椂闂存彁閱掞紝闇€瑕佸湪绯荤粺璁剧疆閲屽厑璁哥簿纭椆閽熴€?)
                    .setPositiveButton("鍘昏缃?, (dialog, which) -> startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)))
                    .setNegativeButton("绋嶅悗", null)
                    .show();
            }
        }
    }

    private String selectedType() {
        View checked = typeGroup.findViewById(typeGroup.getCheckedRadioButtonId());
        return checked == null ? "deadline" : String.valueOf(checked.getTag());
    }

    private RadioButton radio(String text, String tag) {
        RadioButton rb = new RadioButton(this);
        rb.setText(text);
        rb.setTag(tag);
        return rb;
    }

    private LinearLayout verticalBox(String title, View child) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 0, dp(8), 0);
        box.addView(label(title));
        box.addView(child);
        return box;
    }

    private TextView label(String text) {
        TextView view = text(text, 13, MUTED, true);
        view.setPadding(0, dp(12), 0, dp(4));
        return view;
    }

    private EditText input(String hint) {
        EditText edit = new EditText(this);
        edit.setHint(hint);
        edit.setSingleLine(false);
        edit.setBackgroundColor(Color.WHITE);
        edit.setPadding(dp(10), dp(8), dp(10), dp(8));
        return edit;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return view;
    }

    private LinearLayout.LayoutParams blockParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (Exception ignored) { return fallback; }
    }

    private String unitName(int unit) {
        if (unit == 1) return "灏忔椂";
        if (unit == 2) return "澶?;
        return "鍒嗛挓";
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
