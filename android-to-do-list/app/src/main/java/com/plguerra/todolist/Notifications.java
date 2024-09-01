package com.plguerra.todolist;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import static com.plguerra.todolist.CreateNote.Title;
import static com.plguerra.todolist.NoteEdit.Title_Copy;


public class Notifications extends ContextWrapper {
    public static final String CHANNEL_ID = "Reminders";
    public static final String CHANNEL_NAME = "Task Reminder";

    private NotificationManager mManager;

    public Notifications(Context base) {
        super(base);

        //Check OS Version to determine if channels are needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }



    //Create Channels if needed
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }



    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }



    //Create Notification
    public NotificationCompat.Builder getChannelNotification() {
        Intent resultIntent = new Intent(this, HomeActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_ONE_SHOT);

        if (Title != "") {
            return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle("ToDo Reminder")
                    .setContentText(Title)
                    .setSmallIcon(R.drawable.plus)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);
        }
        else{
            return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle("ToDo Reminder")
                    .setContentText(Title_Copy)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);
        }
    }


}