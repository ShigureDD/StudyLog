package com.example.studylog;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Edit extends Activity implements View.OnClickListener {
    private EditText eTitle;
    private TextView date;
    private EditText econtent;
    private Button btn_save;
    private Button btn_cancel;
    private ImageView photo;
    private StudyLogSQLite studylogdb;
    public int enter_state = 0;
    public String last_content;
    public String last_title;
    public String uri_to_string="";
    public String last_photo;
    public String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editpage);
        InitView();
    }

    private void InitView() {
        eTitle= (EditText) findViewById(R.id.edit_title);
        date = (TextView) findViewById(R.id.date);
        econtent = (EditText) findViewById(R.id.content);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        studylogdb = new StudyLogSQLite(this);
        photo = (ImageView) findViewById(R.id.imageV);

        Date dates = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = sdf.format(dates);
        date.setText(dateString);

        Bundle myBundle = this.getIntent().getExtras();
        last_title = myBundle.getString("title");
        last_content = myBundle.getString("note");
        enter_state = myBundle.getInt("enter_state");
        last_photo = myBundle.getString("path");
        eTitle.setText(last_title);
        econtent.setText(last_content);
        Log.v("string","no text"+last_photo);
        if(last_photo.equals("GET")||last_photo.equals("null")){
            photo.setImageResource(R.drawable.add_photo);
        }
        else{
            Uri uri = getMediaUriFromPath(Edit.this,last_photo);
            addImageView(uri);
        }
        btn_cancel.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        photo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                SQLiteDatabase database = studylogdb.getReadableDatabase();

                String content = econtent.getText().toString();

                String Title= eTitle.getText().toString();
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String dateString = sdf.format(date);
                if (enter_state == 0) {
                    if (!Title.equals("")) {
                        if (!content.equals("")) {
                            ContentValues cv = new ContentValues();
                            cv.put("title",Title);
                            cv.put("content", content);
                            cv.put("date", dateString);
                            cv.put("photo",path);
                            database.insert("studylog", null, cv);
                            finish();
                        } else {
                            Toast.makeText(Edit.this, "NO CONTENT", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Edit.this, "NO Title", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    ContentValues cv = new ContentValues();
                    cv.put("title",Title);
                    cv.put("content", content);
                    cv.put("photo",path);
                    cv.put("date", dateString);
                    database.update("studylog", cv, "title = ?", new String[]{last_title});
                    database.update("studylog", cv, "content = ?", new String[]{last_content});
                    database.update("studylog", cv, "photo = ?", new String[]{last_photo});
                    database.update("studylog", cv, "date = ?", new String[]{dateString});

                    finish();
                }
                break;
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.imageV:
                openGallery();
        }
    }
    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this).setMessage("Please give the premission of get image")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(Edit.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},10);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select File"), 11);
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), 11);
                }
                return;
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 11) {
                Uri uri = data.getData();
                path = convertMediaUriToPath(uri);
                addImageView(uri);
            }
        }
    }

    private void addImageView(Uri uri) {
        uri_to_string= uri.toString();
        photo.setImageURI(uri);
        Log.v("uri",uri_to_string);
    }

    public String convertMediaUriToPath(Uri uri) {
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj,  null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    public static Uri getMediaUriFromPath(Context context, String path) {
        Uri uri = null;
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri, null, MediaStore.Images.Media.DISPLAY_NAME + "= ?",
                        new String[] {path.substring(path.lastIndexOf("/") + 1)}, null);
        if(cursor.moveToFirst()) {
            uri = ContentUris.withAppendedId(mediaUri,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        }
        cursor.close();
        return uri;
    }

}

