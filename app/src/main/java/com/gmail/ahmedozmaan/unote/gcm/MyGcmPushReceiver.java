/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.ahmedozmaan.unote.gcm;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.gmail.ahmedozmaan.unote.activity.LecturerActivity;
import com.gmail.ahmedozmaan.unote.helper.MessageDbHelper;
import com.gmail.ahmedozmaan.unote.model.ChatRoom;
import com.gmail.ahmedozmaan.unote.model.Messages;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import com.gmail.ahmedozmaan.unote.activity.StudentActivity;
import com.gmail.ahmedozmaan.unote.app.Config;
import com.gmail.ahmedozmaan.unote.app.MyApplication;

public class MyGcmPushReceiver extends GcmListenerService {

    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();

    private NotificationUtils notificationUtils;

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param bundle Data bundle containing message data as key/value pairs.
     *               For Set of keys use data.keySet().
     */

    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        String title = bundle.getString("title");
        Boolean isBackground = Boolean.valueOf(bundle.getString("is_background"));
        String flag = bundle.getString("flag");
        String data = bundle.getString("data");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "title: " + title);
        Log.d(TAG, "isBackground: " + isBackground);
        Log.d(TAG, "flag: " + flag);
        Log.d(TAG, "data: " + data);

        if (flag == null)
            return;

        if(MyApplication.getInstance().getPrefManager().getUser() == null){
            // user is not logged in, skipping push notification
            Log.e(TAG, "user is not logged in, skipping push notification");
            return;
        }
        switch (Integer.parseInt(flag)) {
            case Config.PUSH_TYPE_CHATROOM:
                // push notification belongs to a chat room
                processChatRoomPush(title, isBackground, data);
                break;
        }
    }

    /**
     * Processing chat room push message
     * this message will be broadcasts to all the activities registered
     * */
    private void processChatRoomPush(String title, boolean isBackground, String data) {
        if (!isBackground) {

            try {
                JSONObject datObj = new JSONObject(data);

                JSONObject mObj = datObj.getJSONObject("message");
                JSONObject uObj = datObj.getJSONObject("user");
                // skip the message if the message belongs to same user as
                // the user would be having the same message when he was sending
                // but it might differs in your scenario
                if (uObj.getString("sender_id").equals(MyApplication.getInstance().getPrefManager().getUser().getId())) {
                    Log.e(TAG, "Skipping the push message as it belongs to same user");
                    return;
                }
                Messages messages = new Messages();
                messages.setMessageId(mObj.getString("message_id"));
                messages.setMessageBody(mObj.getString("message"));
                messages.setMessageTime(mObj.getString("created_at"));
                messages.setMessageRoom(mObj.getString("chat_room_id"));
                messages.setMessageFileFlag(mObj.getString("file_flag"));
                messages.setMessageFileName(mObj.getString("file_name"));
                messages.setMessageFileSize(mObj.getString("file_size"));
                messages.setMessageFileLink(mObj.getString("file_link"));
                messages.setMessageFilePath("0");
                messages.setMessageSenderId(uObj.getString("sender_id"));
                messages.setMessageSenderName(uObj.getString("sender_name"));

                MessageDbHelper messageDbHelper = new MessageDbHelper(getApplicationContext());
                SQLiteDatabase writeSqLiteDatabase = messageDbHelper.getWritableDatabase();
                SQLiteDatabase readSqLiteDatabase = messageDbHelper.getWritableDatabase();
                if(MyApplication.getInstance().getPrefManager().getUser().getClazz().equals("ALL")) {
                    String Name = messageDbHelper.getRoomName(messages.getMessageRoom(),writeSqLiteDatabase);
                    messages.setMessageRoom(Name);
                }
                messageDbHelper.addMessage(messages, writeSqLiteDatabase);

                ChatRoom room = loadRoom(messageDbHelper.getRoomUnreadMessage(messages.getMessageRoom(), readSqLiteDatabase));
                room.setUnreadCount(room.getUnreadCount()+ 1);
                messageDbHelper.updateRoomUnreadMessage(room.getId(), room.getLastMessage(),room.getUnreadCount(), writeSqLiteDatabase);

                writeSqLiteDatabase.close();
                readSqLiteDatabase.close();

                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_CHATROOM);
                    pushNotification.putExtra("message", messages);
                    pushNotification.putExtra("chat_room_id", messages.getMessageRoom());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils();
                    notificationUtils.playNotificationSound();
                } else {

                    // app is in background. show the message in notification try
                    Intent resultIntent;
                    if(MyApplication.getInstance().getPrefManager().getUser().getClazz() =="All" ){
                         resultIntent = new Intent(getApplicationContext(), LecturerActivity.class);
                    }else{

                         resultIntent = new Intent(getApplicationContext(), StudentActivity.class);
                    }
                    resultIntent.putExtra("chat_room_id", messages.getMessageRoom());
                    showNotificationMessage(getApplicationContext(), title, messages.getMessageSenderName()+ " : " + messages.getMessageBody(), messages.getMessageTime(), resultIntent);
                }

            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Error Connection " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }

    private ChatRoom loadRoom(Cursor cursor){
        ChatRoom room = new ChatRoom();
        if (cursor.moveToFirst()) {
            do {
                room.setLastMessage(cursor.getString(0));
                room.setUnreadCount(cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        return room;

    }

    /**
     * Showing notification with text only
     * */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

}
