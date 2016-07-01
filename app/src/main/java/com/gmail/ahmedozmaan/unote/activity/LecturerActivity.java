package com.gmail.ahmedozmaan.unote.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gmail.ahmedozmaan.unote.R;
import com.gmail.ahmedozmaan.unote.adapter.ThreadAdapter;
import com.gmail.ahmedozmaan.unote.app.Config;
import com.gmail.ahmedozmaan.unote.app.EndPoints;
import com.gmail.ahmedozmaan.unote.app.MyApplication;
import com.gmail.ahmedozmaan.unote.gcm.GcmIntentService;
import com.gmail.ahmedozmaan.unote.gcm.NotificationUtils;
import com.gmail.ahmedozmaan.unote.helper.MessageDbHelper;
import com.gmail.ahmedozmaan.unote.model.ChatRoom;
import com.gmail.ahmedozmaan.unote.model.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LecturerActivity extends AppCompatActivity implements ThreadAdapter.ItemClickCallback {

    private String TAG = LecturerActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private ThreadAdapter mAdapter;
    private ArrayList<Messages> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;


    private Boolean isFabOpen = false;
    private FloatingActionButton fab,fab1,fab2;
    private Animation fab_open, fab_close;
    public static Messages selectedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.e(TAG, "at star of Lecturer: ");

        if (MyApplication.getInstance().getPrefManager().getUser().getId() == null) {
            Toast.makeText(getApplicationContext(), "THis user is not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
        getSupportActionBar().setTitle(MyApplication.getInstance().getPrefManager().getUser().getName());


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        messageArrayList = new ArrayList<>();
        mAdapter = new ThreadAdapter(this, messageArrayList);
        mAdapter.setItemClickCallback(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                }
            }
        };
        fetchChatThreadNew();



        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LecturerActivity.this, SendMessageActivity.class);
                intent.putExtra("file_flag", "0");
               // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                fab1.startAnimation(fab_close);
                fab2.startAnimation(fab_close);
                final OvershootInterpolator interpolator = new OvershootInterpolator();
                ViewCompat.animate(fab).rotation(270f).withLayer().setDuration(300).setInterpolator(interpolator).start();
                isFabOpen = false;
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LecturerActivity.this, SendMessageActivity.class);
                intent.putExtra("file_flag", "1");
              //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                fab1.startAnimation(fab_close);
                fab2.startAnimation(fab_close);
                final OvershootInterpolator interpolator2 = new OvershootInterpolator();
                ViewCompat.animate(fab).rotation(270f).withLayer().setDuration(300).setInterpolator(interpolator2).start();
                isFabOpen = false;
            }
        });
        ActionBarEvents();
    }
    public void ActionBarEvents(){
        android.support.v7.app.ActionBar ab=getSupportActionBar();
        ab.setLogo(R.mipmap.icon_note);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }
    @Override
    protected void onResume() {
        super.onResume();

        // registering the receiver for new notification
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        NotificationUtils.clearNotifications();
    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
    /**
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     * */
    private void handlePushNotification(Intent intent) {
        Messages message = (Messages) intent.getSerializableExtra("message");
        String chatRoomId = intent.getStringExtra("chat_room_id");

        if (message != null && chatRoomId != null) {
            messageArrayList.add(message);
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }
        }
    }
    /**
     * fetching the chat rooms by making http call
     */
    public void fetchChatThreadNew() {
        MessageDbHelper messageDbHelper = new MessageDbHelper(getBaseContext());
        SQLiteDatabase sqLiteDatabase = messageDbHelper.getReadableDatabase();
        Cursor cursor = messageDbHelper.getMessagesByUser(MyApplication.getInstance().getPrefManager().getUser().getId(), sqLiteDatabase);
        if (cursor.moveToFirst()) {
            do {
                Messages messages = new Messages();
                messages.setMessageId(cursor.getString(0));
                messages.setMessageBody(cursor.getString(1));
                messages.setMessageRoom(cursor.getString(2));
                messages.setMessageTime(cursor.getString(3));
                messages.setMessageSenderId(cursor.getString(4));
                messages.setMessageSenderName(cursor.getString(5));
                messages.setMessageFileFlag(cursor.getString(6));
                messages.setMessageFileName(cursor.getString(7));
                messages.setMessageFileSize(cursor.getString(8));
                messages.setMessageFileLink(cursor.getString(9));
                messages.setMessageFilePath(cursor.getString(10));
                messageArrayList.add(messages);
                ;
            } while (cursor.moveToNext());
            sqLiteDatabase.close();
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                // recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
        Log.e(TAG, "end of fetch chat thread: ");
    }
    @Override
    public void onItemClick(int position) {

    }
    @Override
    public void onDownloadClick(int position) {
        selectedMessages = messageArrayList.get(position);
        File dir = new File(selectedMessages.getMessageFilePath());
        openFile(dir);
    }
    @Override
    public void onMessageClick(final int position) {
        selectedMessages = messageArrayList.get(position);
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.WindowTitleStyle);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
        builder.setTitle("DELETE MESSAGE");
        builder.setMessage("To:" + selectedMessages.getMessageRoom()+"\n"+ selectedMessages.getMessageBody());
        builder.setCancelable(false);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MessageDbHelper messageDbHelper = new MessageDbHelper(getBaseContext());
                SQLiteDatabase writeSqLiteDatabase = messageDbHelper.getWritableDatabase();
                messageDbHelper.deleteMessage(selectedMessages.getMessageId(), writeSqLiteDatabase);
                messageArrayList.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, messageArrayList.size());
                dialog.cancel();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_logout:
                ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.WindowTitleStyle);
                AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
                builder.setTitle("LOGOUT");
                builder.setMessage("ARE SURE THAT YOUR WANT TO CLEAR YOUR ACCOUNT");
                builder.setCancelable(false);

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyApplication.getInstance().logout();
                        getBaseContext().deleteDatabase(MessageDbHelper.DATABASE_NAME);
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
        }
        return super.onOptionsItemSelected(menuItem);
    }
    public  void openFile(File url){
        // Create URI
        File file = url;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "There isn't any program open this file!!", Toast.LENGTH_SHORT).show();
        }

    }
    public void animateFAB(){
        if(isFabOpen){
            final OvershootInterpolator interpolator = new OvershootInterpolator();
            ViewCompat.animate(fab).rotation(270f).withLayer().setDuration(300).setInterpolator(interpolator).start();
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");
        } else {
            final OvershootInterpolator interpolator = new OvershootInterpolator();
            ViewCompat.animate(fab).rotation(135f).withLayer().setDuration(300).setInterpolator(interpolator).start();
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");
        }
    }
}
