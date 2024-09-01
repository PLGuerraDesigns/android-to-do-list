package com.plguerra.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateNote extends AppCompatActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    //Obtain Fields from note_create.xml
    private EditText task, taskDescription, dueDate, reminder;


    static String Title = "";               //Store Task Title
    static int Task_ID = 0;                 //Store Task ID
    Calendar c = Calendar.getInstance();    //Calendar Instance


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_create);

        //Obtain Buttons and other fields from note_create.xml
        Button save = findViewById(R.id.btnSave);
        Button clear = findViewById(R.id.Clear);
        task = findViewById(R.id.Task);
        dueDate = findViewById(R.id.DueDate);
        reminder = findViewById(R.id.Reminder);
        taskDescription = findViewById(R.id.TaskDescription);

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
                }
                else {
                    Toast.makeText(getApplicationContext(), "Set Date First", Toast.LENGTH_LONG).show();
                }
            }

        });

        //Listening for Button Presses
        save.setOnClickListener(this);
        clear.setOnClickListener(this);

    }



    public void onClick(View v){

        switch (v.getId()){

            //If finished creating Task
            case R.id.btnSave:
                //Get information in fields
                Title = task.getText().toString();
                String Description = taskDescription.getText().toString();
                String dueDateContent = dueDate.getText().toString();
                String reminderContent = reminder.getText().toString();

                //Make sure the Task Title is not empty
                if(Title.length() != 0) {
                    createNewNote(Title, Description, dueDateContent, reminderContent);
                    setAlarm(c);
                    openMainPage();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please add a Task", Toast.LENGTH_LONG).show();
                }

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
    public void openMainPage(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

    }



    //Create New Task based on information obtained
    void createNewNote(String noteTitle, String description, String DueDateIn, String ReminderIn){
        //Create a ContentValues object
        ContentValues myCV = new ContentValues();
        //Put key_value pairs based on the column names, and the values
        myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE,noteTitle);
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT,description);
        myCV.put(ToDoProvider.TODO_TABLE_COL_DEADLINE,DueDateIn);
        myCV.put(ToDoProvider.TODO_TABLE_COL_REMINDER,ReminderIn);

        //Perform the insert function using the ContentProvider
        getContentResolver().insert(ToDoProvider.CONTENT_URI,myCV);
        //Set the projection for the columns to be returned
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT};
        //Perform a query to get all rows in the DB
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,null);
        Task_ID = myCursor.getCount();
        Toast.makeText(getApplicationContext(), "Task Added", Toast.LENGTH_LONG).show();
    }


    //Set Notification alarm
    private void setAlarm(Calendar c){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Task_ID, intent, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }



    //Cancel Notification alarm
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Task_ID, intent, 0);
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

        String myFormat = "MM/dd/yy" ;
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat , Locale. getDefault ()) ;

        Date date = c.getTime();
        dueDate.setText(sdf.format(date));
    }
}

