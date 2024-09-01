package com.plguerra.todolist;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

//Create HomeActivity and implement the OnClick listener
public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String Task_ID = "com.plguerra.todolist.Task_ID";       //Store Task ID to pass to NoteEdit
    private TextView noListText;    //Obtain TextView from activity_home.xml




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnNewNote).setOnClickListener(this);
        noListText = findViewById(R.id.NoListText);

    }



    //Load Tasks when Main Page is resumed
    @Override
    protected void onResume()
    {
        super.onResume();
        LoadList();
    }



    @Override
    public void onClick(View v){

        //If new Note, call openNoteCreator()
        if (v.getId() == R.id.btnNewNote) {
            openNoteCreator();
        }
    }



    //Open Create Note Page
    public void openNoteCreator(){
        Intent intent = new Intent(this, CreateNote.class);
        startActivity(intent);
    }



    //Open Note Editor Page
    public void openNoteEditor(Integer ID){
        Intent intent = new Intent(this, NoteEdit.class);
        intent.putExtra(Task_ID, ID.toString());
        startActivity(intent);
    }



    //Populate ListView With Tasks
    public void LoadList(){
        ArrayAdapter<String> adapter;   //Array adapter for List View
        final ListView listView = findViewById(R.id.taskList);
        //Perform a query to get all rows in the DB
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI,null,null,null,null);
        //Array Lists to store Task information
        ArrayList<String> taskList = new ArrayList<>();
        final ArrayList<String> descriptionList = new ArrayList<>();
        final ArrayList<String> dueDateList = new ArrayList<>();
        final ArrayList<String> reminderList = new ArrayList<>();

        //Iterate Through Table Rows to store Task information
        assert myCursor != null;
        for(myCursor.moveToFirst(); !myCursor.isAfterLast(); myCursor.moveToNext()) {
            taskList.add(myCursor.getString(1));
            descriptionList.add(myCursor.getString(2));
            dueDateList.add(myCursor.getString(3));
            reminderList.add(myCursor.getString(4));
        }

        //If No Tasks Exist then Show feedback
        if(taskList.size() == 0){
                noListText.setText("Nothing To Do");
            }
        else{
            noListText.setText("");
        }

        //Populate ListView
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,taskList);
        listView.setAdapter(adapter);

        //Listening For Task Selection and opening Note Editor
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Integer ID = position;
                openNoteEditor(ID);
            }
        });
    }

}
