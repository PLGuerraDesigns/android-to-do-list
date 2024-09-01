package com.plguerra.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import static com.plguerra.todolist.CreateNote.Task_ID;       //Get Task ID from CreateNote
import static com.plguerra.todolist.NoteEdit.Task_ID_Copy;    //Get Task ID from NoteEdit

public class AlertReceiver extends BroadcastReceiver {

    @Override
    //Handle Scheduling Alerts Based on TaskID.
    public void onReceive(Context context, Intent intent) {
        //Called From CreateNote
        if (Task_ID != 0){
            Notifications notifications = new Notifications(context);
            NotificationCompat.Builder nb = notifications.getChannelNotification();
            notifications.getManager().notify(Task_ID, nb.build());
        }
        else{
            //Called From NoteEdit
            Notifications notifications = new Notifications(context);
            NotificationCompat.Builder nb = notifications.getChannelNotification();
            notifications.getManager().notify(Task_ID_Copy, nb.build());
        }
    }


}
