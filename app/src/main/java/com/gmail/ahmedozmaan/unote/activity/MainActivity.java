package com.gmail.ahmedozmaan.unote.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gmail.ahmedozmaan.unote.helper.MessageDbHelper;
import com.gmail.ahmedozmaan.unote.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.gmail.ahmedozmaan.unote.R;
import com.gmail.ahmedozmaan.unote.adapter.RoomAdapter;
import com.gmail.ahmedozmaan.unote.app.Config;
import com.gmail.ahmedozmaan.unote.app.EndPoints;
import com.gmail.ahmedozmaan.unote.app.MyApplication;
import com.gmail.ahmedozmaan.unote.gcm.GcmIntentService;
import com.gmail.ahmedozmaan.unote.gcm.NotificationUtils;
import com.gmail.ahmedozmaan.unote.helper.SimpleDividerItemDecoration;
import com.gmail.ahmedozmaan.unote.model.ChatRoom;
import com.gmail.ahmedozmaan.unote.model.Messages;
/**
 * In order to receive the push notifications, device has to support google play services.
 * So checkPlayServices() method is used to check the availability of google play services.
 * If the play services are not available, we’ll simply close the app.

 > Register a broadcast receiver in onResume() method for both REGISTRATION_COMPLETE and PUSH_NOTIFICATION intent filters.

 > Unregister the broadcast receiver in onPause() method.

 > Create an instance of broadcast receiver in onCreate() method
 in which onReceive() method will be triggered whenever gcm registration process is completed and a new push message is received.
 * */
public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private RoomAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Check for login session. If not logged in launch
         * login activity
         * */
        if (MyApplication.getInstance().getPrefManager().getUser() == null) {
            launchLoginActivity();

        }else if (MyApplication.getInstance().getPrefManager().getVerify() == 0) {
           launchVerifyActivity();
        }
        /**
         * Always check for google play services availability before
         * proceeding further with GCM
         * */
        else if (checkPlayServices()) {
            if(MyApplication.getInstance().getPrefManager().getRegistered() == 0) {
                registerGCM();
            }
            if (MyApplication.getInstance().getPrefManager().getUser().getClazz().equals("ALL")) {

                if(!checkRecords()) {
                    fetchLecturerChatRooms();
                }
                launchLecturerActivity();
            } else {
                    setContentView(R.layout.activity_main);
                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    setSupportActionBar(toolbar);

                    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

                    /**
                     * Broadcast receiver calls in two scenarios
                     * 1. gcm registration is completed
                     * 2. when new push notification is received
                     * */
                    mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {

                            // checking for type intent filter
                            if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                                // gcm successfully registered
                                // now subscribe to `global` topic to receive app wide notifications
                                subscribeToGlobalTopic();

                            } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                                // gcm registration id is stored in our server's MySQL
                                // Log.e(TAG, "GCM registration id is sent to our server");

                            } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                                // new push notification is received
                                handlePushNotification(intent);
                            }
                        }
                    };

                    chatRoomArrayList = new ArrayList<>();
                    mAdapter = new RoomAdapter(this, chatRoomArrayList);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                            getApplicationContext()
                    ));
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(mAdapter);

                    recyclerView.addOnItemTouchListener(new RoomAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new RoomAdapter.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {
                            // when chat is clicked, launch full chat thread activity
                            ChatRoom chatRoom = chatRoomArrayList.get(position);
                            Intent intent;
                            if(MyApplication.getInstance().getPrefManager().getUser().getClazz().equals("ALL")) {
                                intent = new Intent(MainActivity.this, LecturerActivity.class);
                            }else {
                                intent = new Intent(MainActivity.this, StudentActivity.class);
                            }
                            intent.putExtra("chat_room_id", chatRoom.getId());
                            intent.putExtra("name", chatRoom.getName());
                            intent.putExtra("un_read_count",chatRoom.getUnreadCount());
                            startActivity(intent);
                        }

                        @Override
                        public void onLongClick(View view, int position) {

                        }
                    }));
                if(!checkRecords()) {
                    fetchStudentChatRooms();
                }
                loadChatRooms();
            }
    }

//        ActionBarEvents();
    }
    public void ActionBarEvents(){
        android.support.v7.app.ActionBar ab=getSupportActionBar();
        ab.setLogo(R.mipmap.icon_note);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }
    private Boolean checkRecords(){
        Boolean status = false;
        MessageDbHelper messageDbHelper = new MessageDbHelper(getBaseContext());
        SQLiteDatabase sqLiteDatabase = messageDbHelper.getReadableDatabase();
        long rc = messageDbHelper.getRoomCount(sqLiteDatabase);
        if(rc > 0){
            status = true;
        }
        return  status;

    }
    private void clearRecords(){
        MessageDbHelper messageDbHelper = new MessageDbHelper(getBaseContext());
        SQLiteDatabase sqLiteDatabase = messageDbHelper.getReadableDatabase();
         messageDbHelper.clearRooms(sqLiteDatabase);
    }
    /**
     * Handles new push notification
     */
    private void handlePushNotification(Intent intent) {
        int type = intent.getIntExtra("type", -1);

        // if the push is of chat room message
        // simply update the UI unread uploadingMessages count
        if (type == Config.PUSH_TYPE_CHATROOM) {
            Messages message = (Messages) intent.getSerializableExtra("message");
            String chatRoomId = intent.getStringExtra("chat_room_id");

            if (message != null && chatRoomId != null) {
                updateRow(chatRoomId, message);
            }
        } else if (type == Config.PUSH_TYPE_USER) {
            // push belongs to user alone
            // just showing the message in a toast
            Messages message = (Messages) intent.getSerializableExtra("message");
            Toast.makeText(getApplicationContext(), "New push: " + message.getMessageBody(), Toast.LENGTH_LONG).show();
        }


    }
    /**
     * Updates the chat list unread count and the last message
     */
    private void updateRow(String chatRoomId, Messages message) {
        for (ChatRoom cr : chatRoomArrayList) {
            if (cr.getId().equals(chatRoomId)) {
                int index = chatRoomArrayList.indexOf(cr);
                cr.setLastMessage(message.getMessageBody());
                cr.setUnreadCount(cr.getUnreadCount() + 1);
                chatRoomArrayList.remove(index);
                chatRoomArrayList.add(index, cr);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }
    /**
     * fetching the chat rooms by making http call
     */
    public void loadChatRooms(){
        MessageDbHelper messageDbHelper = new MessageDbHelper(getBaseContext());
        SQLiteDatabase sqLiteDatabase = messageDbHelper.getReadableDatabase();
        Cursor cursor = messageDbHelper.getRoom(sqLiteDatabase);
        chatRoomArrayList.clear();
        if (cursor.moveToFirst()){
            do {
                ChatRoom cr = new ChatRoom();
                cr.setId(cursor.getString(0));
                cr.setLastMessage(cursor.getString(1));
                cr.setUnreadCount(cursor.getInt(2));
                cr.setName(cursor.getString(3));
                chatRoomArrayList.add(cr);
            }while (cursor.moveToNext());
        }sqLiteDatabase.close();
        mAdapter.notifyDataSetChanged();
    }
    private void fetchLecturerChatRooms() {
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    EndPoints.CHAT_ROOMS, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.e(TAG, "response: " + response);

                    try {
                        JSONObject obj = new JSONObject(response);
                        MessageDbHelper messageDbHelper = new MessageDbHelper(getApplicationContext());
                        SQLiteDatabase sqLiteDatabase = messageDbHelper.getWritableDatabase();

                        // check for error flag
                        if (obj.getBoolean("error") == false) {
                            JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                            for (int i = 0; i < chatRoomsArray.length(); i++) {
                                JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                                ChatRoom room = new ChatRoom();
                                room.setId(chatRoomsObj.getString("chat_room_id"));
                                room.setName(chatRoomsObj.getString("name"));
                                room.setLastMessage("");
                                room.setUnreadCount(0);
                                room.setTimestamp(chatRoomsObj.getString("created_at"));
                                messageDbHelper.addRoom(room, sqLiteDatabase);
                            }
                            sqLiteDatabase.close();
                            // subscribing to all chat room topics
                            //subscribeToAllTopics();

                        } else {
                            clearRecords();
                            // error in fetching chat rooms
                            Toast.makeText(getApplicationContext(), "Error In Connection " , Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "json parsing error: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error In Connection ", Toast.LENGTH_LONG).show();
                        clearRecords();
                    }


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                    Toast.makeText(getApplicationContext(), "Error In Connection ", Toast.LENGTH_SHORT).show();
                }
            });

            //Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(strReq);
    }
    private void fetchStudentChatRooms() {

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    EndPoints.USER_CHAT_ROOMS, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.e(TAG, "response: " + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        MessageDbHelper messageDbHelper = new MessageDbHelper(getApplicationContext());
                        SQLiteDatabase sqLiteDatabase = messageDbHelper.getWritableDatabase();

                        // check for error flag
                        if (obj.getBoolean("error") == false) {
                            JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                            for (int i = 0; i < chatRoomsArray.length(); i++) {
                                JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                                ChatRoom room = new ChatRoom();
                                room.setId(chatRoomsObj.getString("chat_room_id"));
                                room.setName(chatRoomsObj.getString("name"));
                                room.setLastMessage("");
                                room.setUnreadCount(0);
                                room.setTimestamp(chatRoomsObj.getString("created_at"));
                                messageDbHelper.addRoom(room, sqLiteDatabase);
                            }
                            sqLiteDatabase.close();loadChatRooms();
                            // subscribing to all chat room topics
                            subscribeToAllTopics();
                        } else {
                            clearRecords();
                            // error in fetching chat rooms
                            Toast.makeText(getApplicationContext(), "Error In Connection ", Toast.LENGTH_LONG).show();
                        }


                    } catch (JSONException e) {
                        clearRecords();
                        Log.e(TAG, "json parsing error: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error In Connection  ", Toast.LENGTH_SHORT).show();
                    }
                    mAdapter.notifyDataSetChanged();


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                    Toast.makeText(getApplicationContext(), "Error In Connection ", Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("clazz", MyApplication.getInstance().getPrefManager().getUser().getClazz());

                    Log.e(TAG, "params: " + params.toString());
                    return params;
                }
            };

            //Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(strReq);
        loadChatRooms();
    }
    // subscribing to global topic
    private void subscribeToGlobalTopic() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
        intent.putExtra(GcmIntentService.TOPIC, Config.TOPIC_GLOBAL);
        startService(intent);
    }
    // Subscribing to all chat room topics
    private void subscribeToAllTopics() {
        for (ChatRoom cr : chatRoomArrayList) {
            Intent intent = new Intent(this, GcmIntentService.class);
            intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
            intent.putExtra(GcmIntentService.TOPIC, cr.getId());
            startService(intent);
        }
    }
    private void launchLoginActivity() {

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Log.e(TAG, "launchLoginActivity");
        startActivity(intent);
        finish();
    }
    private void launchVerifyActivity() {
        Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void launchLecturerActivity() {
        startActivity(new Intent(this, LecturerActivity.class));
        finish();
        //Intent intent = new Intent(MainActivity.this, LecturerActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //startActivity(intent);
        //finish();
    }
    // starting the service to register with GCM
    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clearing the notification tray
        NotificationUtils.clearNotifications();
    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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


}
