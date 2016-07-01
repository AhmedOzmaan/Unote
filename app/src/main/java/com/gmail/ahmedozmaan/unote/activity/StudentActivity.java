package com.gmail.ahmedozmaan.unote.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import com.gmail.ahmedozmaan.unote.R;
import com.gmail.ahmedozmaan.unote.adapter.ThreadAdapter;
import com.gmail.ahmedozmaan.unote.app.Config;
import com.gmail.ahmedozmaan.unote.app.EndPoints;
import com.gmail.ahmedozmaan.unote.app.MyApplication;
import com.gmail.ahmedozmaan.unote.gcm.NotificationUtils;
import com.gmail.ahmedozmaan.unote.helper.MessageDbHelper;
import com.gmail.ahmedozmaan.unote.model.ChatRoom;
import com.gmail.ahmedozmaan.unote.model.Messages;

public class StudentActivity extends AppCompatActivity implements ThreadAdapter.ItemClickCallback {

    private String TAG = StudentActivity.class.getSimpleName();

    private String chatRoomId;
    private int unReadCount;
    private RecyclerView recyclerView;
    public static ThreadAdapter mAdapter;
    private ArrayList<Messages> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static Messages selectedMessages;
    private static final int EDIT = 0;
    private static final int DELETE= 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        unReadCount = intent.getIntExtra("un_read_count",0);
        String title = intent.getStringExtra("name");
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (chatRoomId == null) {
            Toast.makeText(getApplicationContext(), "Chat room not found!", Toast.LENGTH_SHORT).show();
            finish();
        }

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
    public  void openFile(File url){
        // Create URI
        File file = url;
        Uri uri = Uri.fromFile(file);
        if (!url.exists()) {
            Toast.makeText(this, "File is not exist!!", Toast.LENGTH_SHORT).show();
            return;
        }

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
        if(unReadCount != 0) {
            messageDbHelper.updateRoomUnreadMessage(chatRoomId, sqLiteDatabase);
        }
        Cursor cursor = messageDbHelper.getMessagesByRoom(chatRoomId, sqLiteDatabase);
        if (cursor.moveToFirst()) {
            do {
                Messages messages = new Messages();
                messages.setMessageId(cursor.getString(0));
                messages.setMessageBody(cursor.getString(1));
                messages.setMessageRoom("0");
                messages.setMessageTime(cursor.getString(3));
                messages.setMessageSenderId(cursor.getString(4));
                messages.setMessageSenderName(cursor.getString(5));
                messages.setMessageFileFlag(cursor.getString(6));
                messages.setMessageFileName(cursor.getString(7));
                messages.setMessageFileSize(cursor.getString(8));
                messages.setMessageFileLink(cursor.getString(9));
                messages.setMessageFilePath(cursor.getString(10));
                messageArrayList.add(messages);

            }while (cursor.moveToNext()) ;
        } sqLiteDatabase.close();
        mAdapter.notifyDataSetChanged();
        if (mAdapter.getItemCount() > 1) {
            //recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }
    @Override
    public void onItemClick(int position) {

    }
    @Override
    public void onDownloadClick(int position) {
        selectedMessages = messageArrayList.get(position);
        String PATH = Environment.getExternalStorageDirectory() + "/UNOTE/";
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File outputFile = new File(dir, selectedMessages.getMessageFileName());
        if(selectedMessages.getMessageFilePath().equals("0")) {
            selectedMessages.setMessageFilePath(outputFile.getPath());
            final DownloadTask downloadTask = new DownloadTask(StudentActivity.this, outputFile, "Downloading file");
            downloadTask.execute("http://www.ahmedozmaan.16mb.com/extra/uploads/" + selectedMessages.getMessageFileName());
        }else {
            openFile(outputFile);
        }
    }
    @Override
    public void onMessageClick(final int position) {
        selectedMessages = messageArrayList.get(position);
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.WindowTitleStyle);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
        builder.setTitle("DELETE MESSAGE");
        builder.setMessage("FROM:" + selectedMessages.getMessageSenderName()+"\n"+ selectedMessages.getMessageBody());
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

    public static class DownloadTask extends AsyncTask<String, Integer, String> {
        private ProgressDialog mPDialog;
        private Context mContext;
        private PowerManager.WakeLock mWakeLock;
        private File mTargetFile;
        public DownloadTask(Context context,File targetFile,String dialogMessage) {
            this.mContext = context;
            this.mTargetFile = targetFile;
            mPDialog = new ProgressDialog(context);

            mPDialog.setMessage(dialogMessage);
            mPDialog.setIndeterminate(true);
            mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mPDialog.setCancelable(false);
            // reference to instance to use inside listener
            final DownloadTask me = this;
            mPDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    me.cancel(true);
                }
            });
            Log.i("DownloadTask", "Constructor done");
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                Log.i("DownloadTask","Response " + connection.getResponseCode());

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(mTargetFile,false);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        Log.i("DownloadTask","Cancelled");
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();

            mPDialog.show();

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mPDialog.setIndeterminate(false);
            mPDialog.setMax(100);
            mPDialog.setProgress(progress[0]);

        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("DownloadTask", "Work Done! PostExecute");
            mWakeLock.release();
            mPDialog.dismiss();
            if (result != null) {
                Log.i("Download error: " + result, "canceled");
                Toast.makeText(mContext, "Download error: " + result, Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(mContext,"File Downloaded", Toast.LENGTH_SHORT).show();
            MessageDbHelper messageDbHelper = new MessageDbHelper(mContext);
            SQLiteDatabase writeSqLiteDatabase = messageDbHelper.getWritableDatabase();
            messageDbHelper.updateMessageFilePath(selectedMessages, writeSqLiteDatabase);
            mAdapter.notifyDataSetChanged();
        }
    }
}
