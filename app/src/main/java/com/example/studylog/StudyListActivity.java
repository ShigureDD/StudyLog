package com.example.studylog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudyListActivity extends AppCompatActivity {
    private ListView listView;
    private List<Map<String, Object>> dataList;
    private Cursor cursor;
    private Button addNoteButton;
    private StudyLogSQLite studylogdb;
    private SQLiteDatabase DB;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setView();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RefreshList();
    }


    private void setView(){
        if(studylogdb == null) {
            studylogdb = new StudyLogSQLite(this);
        }
        listView=(ListView)findViewById (R.id.list);
        addNoteButton = (Button) findViewById(R.id.add_note_button);
        dataList = new ArrayList<Map<String, Object>>();
        studylogdb = new StudyLogSQLite(this);
        DB = studylogdb.getReadableDatabase();

    }
    public void RefreshList() {
        int size = dataList.size();
        if (size > 0) {
            dataList.removeAll(dataList);
            adapter.notifyDataSetChanged();
        }
        cursor = DB.query("studylog", null, null, null, null, null, "_id DESC");

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String uri= cursor.getString(cursor.getColumnIndex("photo"));
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("_id",id);
            map.put("title", title);
            map.put("content", content);
            map.put("date", date);
            map.put("photo",uri);
            dataList.add(map);
        }
        adapter=new SimpleAdapter(this, dataList, R.layout.list_adpater, new String[]{"title", "date"}, new int[]{ R.id.content, R.id.date});
        listView.setAdapter(adapter);
    }

    private void setListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String content = listView.getItemAtPosition(position) + "";
                String[] separated = content.split(",");
                String date = separated[0].substring(separated[0].indexOf("=") +1);
                String uri= separated[1].substring(separated[1].indexOf("=") +1);
                String _id =separated[2].substring(separated[2].indexOf("=") +1);
                String title = separated[3].substring(separated[3].indexOf("=") +1);
                String note = separated[4].substring(separated[4].indexOf("=") +1, separated[4].indexOf("}"));
                Log.v("content",content);
                Intent myIntent = new Intent(StudyListActivity.this, Edit.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", title);
                bundle.putString("note", note);
                bundle.putString("path", uri);
                bundle.putInt("enter_state", 1);
                myIntent.putExtras(bundle);
                startActivity(myIntent);

            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view,final int pos, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StudyListActivity.this);
                builder.setTitle("Delete");
                builder.setMessage("Are You Sure to Delete?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = listView.getItemAtPosition(pos) + "";
                        Log.v("test","Content "+content);
                        String[] separated = content.split(",");
                        String date = separated[0].substring(separated[0].indexOf("=") +1);
                        String uri= separated[1].substring(separated[1].indexOf("=") +1);
                        String _id =separated[2].substring(separated[2].indexOf("=") +1);
                        String title = separated[3].substring(separated[3].indexOf("=") +1);
                        String note = separated[4].substring(separated[4].indexOf("=") +1, separated[4].indexOf("}"));

                        DB.delete("studylog", "_id = ?", new String[]{_id});
                        RefreshList();
                    }

                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();
                return true;
            }
        });
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudyListActivity.this, Edit.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", "");
                bundle.putString("note", "");
                bundle.putString("path","GET");
                bundle.putInt("enter_state", 0);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

}
