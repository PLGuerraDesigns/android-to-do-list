package com.plguerra.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;



public class NoteEdit extends AppCompatActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    //Obtain Fields from note_edit.xml
    private EditText task, taskDescription, dueDate, reminder;
    CheckBox taskCompleted;
    private TextView DDRLabel;


    Calendar c = Calendar.getInstance();                            //Calendar Instance
    private int Task_ID;                                            //Store Task ID
    private String taskTitle, taskDes, taskDueDate, taskReminder;   //Store Task information
    static int Task_ID_Copy;                                        //Copy of Task ID to pass to Notifications class
    static String Title_Copy;                                       //Copy of Task Title to pass to Notifications class


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);

        //Obtain Buttons and other fields from NoteEdit.xml
        Button done = findViewById(R.id.btnDone);
        Button delete = findViewById(R.id.btnDeleteNote);
        Button clear = findViewById(R.id.Clear);
        taskCompleted = findViewById(R.id.TaskCompleted);
        task = findViewById(R.id.Task);
        taskDescription = findViewById(R.id.TaskDescription);
        dueDate = findViewById(R.id.DueDate);
        reminder = findViewById(R.id.Reminder);
        DDRLabel = findViewById(R.id.DDRText);

        //Retrieving Task ID from HomeActivity
        Intent intent = getIntent();

        Task_ID = Integer.valueOf(intent.getStringExtra(HomeActivity.Task_ID)) + 1;
        Task_ID_Copy = Task_ID;

        //Load Task information
        LoadTask(Task_ID);


        //Add a label for Date and Reminder if the fields contain values
        if (taskDueDate.length() != 0 || taskReminder.length() != 0) {
            DDRLabel.setText("Date & Reminder:");
        }


        //Listening for Date Selection
        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "Date Picker");

            }

        });

        //Listening for Time Selection
        reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dueDate.getText().toString().length() != 0) {
                    DialogFragment timePicker = new TimePickerFragment();
                    timePicker.show(getSupportFragmentManager(), "Time Picker");
                } else {
                    Toast.makeText(getApplicationContext(), "Set Date First", Toast.LENGTH_LONG).show();
                }
            }

        });

        //Listening for Button Presses
        done.setOnClickListener(this);
        delete.setOnClickListener(this);
        clear.setOnClickListener(this);
    }



    public void onClick(View v) {

        switch (v.getId()) {

            //If finished viewing/editing Task
            case R.id.btnDone:
                //Delete Task if it has been completed and cancel alarms
                if (taskCompleted.isChecked()) {
                    cancelAlarm();
                    deleteNote(Task_ID, 1);
                    openMainPage();
                }
                //Update Task with information in fields
                else {
                    String Title = task.getText().toString();
                    String Description = taskDescription.getText().toString();
                    String dueDateContent = dueDate.getText().toString();
                    String reminderContent = reminder.getText().toString();

                    //Make sure the Task Title is not empty
                    if (Title.length() != 0) {
                        UpdateTask(Title, Description, dueDateContent, reminderContent);
                        setAlarm(c);
                        openMainPage();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Please add a Task", Toast.LENGTH_LONG).show();
                    }
                }

                break;

            //Delete Task and cancel alarms
            case R.id.btnDeleteNote:
                deleteNote(Task_ID, 0);
                cancelAlarm();
                openMainPage();
                break;

            //Clear Due Date and alarm
            case R.id.Clear:
                dueDate.setText("");
                reminder.setText("");
                cancelAlarm();

            //This shouldn't happen
            default:
                break;
        }

    }


    //Return to Main Page if Called
    public void openMainPage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

    }



    //Delete task based on ID
    void deleteNote(int ID, int Type) {
        //Get the current number of tasks
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, null, null, null, null);
        int TotalTasks = myCursor.getCount();

        //Delete Task with given ID
        int didWork = getContentResolver().delete(Uri.parse(ToDoProvider.CONTENT_URI + "/" + ID), null, null);
        if (didWork == 1) {
            //If it did work, then check if the task was completed or deleted by user and display.
            if (Type == 0) {
                Toast.makeText(getApplicationContext(), "Task Deleted", Toast.LENGTH_LONG).show();
            }
            if (Type == 1) {
                Toast.makeText(getApplicationContext(), "Task Completed", Toast.LENGTH_LONG).show();
            }
        }

        //Create a ContentValues object
        ContentValues myCV = new ContentValues();
        //Based on the previous number of tasks and the task ID deleted, decrement the following IDs.
        for (int i = ID + 1; i < TotalTasks + 1; i++) {
            myCV.put(ToDoProvider.TODO_TABLE_COL_ID, i - 1);
            getContentResolver().update(Uri.parse(ToDoProvider.CONTENT_URI + "/" + i), myCV, null, null);
        }
    }


    //Update the Task information based on ID in the Database
    void UpdateTask(String TaskTitle, String description, String DueDateIn, String ReminderIn) {
        //Create a ContentValues object
        ContentValues myCV = new ContentValues();

        //Put key_value pairs based on the column names, and the values
        myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, TaskTitle);
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, description);
        myCV.put(ToDoProvider.TODO_TABLE_COL_DEADLINE, DueDateIn);
        myCV.put(ToDoProvider.TODO_TABLE_COL_REMINDER, ReminderIn);

        getContentResolver().update(Uri.parse(ToDoProvider.CONTENT_URI + "/" + Task_ID), myCV, null, null);
    }



    //Set Notification alarm
    private void setAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Task_ID_Copy, intent, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }



    //Cancel Notification alarm
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Task_ID_Copy, intent, 0);
        alarmManager.cancel(pendingIntent);
    }



    //Get Values from TimePicker
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        String timeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());

        reminder.setText(timeText);
    }



    //Get Values from DatePicker
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);

        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        Date date = c.getTime();
        dueDate.setText(sdf.format(date));
    }



    //Load Task Information Based on ID
    public void LoadTask(int TaskID) {

        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, null, ToDoProvider.TODO_TABLE_COL_ID + " = " + TaskID, null, null);

        myCursor.moveToFirst();
        taskTitle = myCursor.getString(1);
        taskDes = myCursor.getString(2);
        taskDueDate = myCursor.getString(3);
        taskReminder = myCursor.getString(4);

        Title_Copy = taskTitle;
        task.setText(taskTitle);
        taskDescription.setText(taskDes);
        dueDate.setText(taskDueDate);
        reminder.setText(taskReminder);

    }
}

