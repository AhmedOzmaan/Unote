package com.gmail.ahmedozmaan.unote.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.gmail.ahmedozmaan.unote.model.ChatRoom;
import com.gmail.ahmedozmaan.unote.model.Messages;

/**
 * Created by AhmedOzman on 2/10/2016.
 */
public class MessageDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "unote.db";
    public static final int DATABASE_VERSION = 1;
    Context context;

    public MessageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String messageQuery = "CREATE TABLE " + Contract.Message.TABLE_NAME + "(" +
                Contract.Message.MESSAGE_ID +   " TEXT,"+
                Contract.Message.MESSAGE_BODY + " TEXT,"+
                Contract.Message.MESSAGE_ROOM + " TEXT,"+
                Contract.Message.MESSAGE_TIME + " TEXT,"+
                Contract.Message.MESSAGE_SENDER_ID + " TEXT,"+
                Contract.Message.MESSAGE_SENDER_NAME + " TEXT,"+
                Contract.Message.MESSAGE_FILE_FLAG + " TEXT,"+
                Contract.Message.MESSAGE_FILE_NAME + " TEXT,"+
                Contract.Message.MESSAGE_FILE_SIZE + " TEXT,"+
                Contract.Message.MESSAGE_FILE_LINK + " TEXT,"+
                Contract.Message.MESSAGE_FILE_PATH +  " TEXT );";
        db.execSQL(messageQuery);
        String roomQuery = "CREATE TABLE " + Contract.Room.TABLE_NAME + "(" +
                Contract.Room.ROOM_ID + " TEXT,"+
                Contract.Room.LAST_MESSAGE + " TEXT,"+
                Contract.Room.UN_READ_COUNT + " INTEGER,"+
                Contract.Room.ROOM_NAME + " TEXT );";
        db.execSQL(roomQuery);
      // LoadData(db);

        }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public  void addMessage(Messages message, SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Message.MESSAGE_ID,message.getMessageId());
        contentValues.put(Contract.Message.MESSAGE_ROOM,message.getMessageRoom());
        contentValues.put(Contract.Message.MESSAGE_BODY,message.getMessageBody());
        contentValues.put(Contract.Message.MESSAGE_TIME, message.getMessageTime());
        contentValues.put(Contract.Message.MESSAGE_SENDER_ID, message.getMessageSenderId());
        contentValues.put(Contract.Message.MESSAGE_SENDER_NAME, message.getMessageSenderName());
        contentValues.put(Contract.Message.MESSAGE_FILE_FLAG, message.getMessageFileFlag());
        contentValues.put(Contract.Message.MESSAGE_FILE_NAME, message.getMessageFileName());
        contentValues.put(Contract.Message.MESSAGE_FILE_SIZE, message.getMessageFileSize());
        contentValues.put(Contract.Message.MESSAGE_FILE_LINK, message.getMessageFileLink());
        contentValues.put(Contract.Message.MESSAGE_FILE_PATH, message.getMessageFilePath());
        sqLiteDatabase.insert(Contract.Message.TABLE_NAME, null, contentValues);
    }
    public  void addRoom(ChatRoom room, SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Room.ROOM_ID, room.getId());
        contentValues.put(Contract.Room.ROOM_NAME, room.getName());
        contentValues.put(Contract.Room.LAST_MESSAGE, room.getLastMessage());
        contentValues.put(Contract.Room.UN_READ_COUNT, room.getUnreadCount());
        sqLiteDatabase.insert(Contract.Room.TABLE_NAME, null, contentValues);
    }


    public Cursor getMessages(SQLiteDatabase sqLiteDatabase){
        String[] projection = {Contract.Message.MESSAGE_ID,
                Contract.Message.MESSAGE_BODY,
                Contract.Message.MESSAGE_ROOM,
                Contract.Message.MESSAGE_TIME,
                Contract.Message.MESSAGE_SENDER_ID,
                Contract.Message.MESSAGE_SENDER_NAME,
                Contract.Message.MESSAGE_FILE_FLAG,
                Contract.Message.MESSAGE_FILE_NAME,
                Contract.Message.MESSAGE_FILE_SIZE,
                Contract.Message.MESSAGE_FILE_LINK,
                Contract.Message.MESSAGE_FILE_PATH};
        Cursor cursor = sqLiteDatabase.query(Contract.Message.TABLE_NAME,projection,null,null,null,null,null);
        return  cursor;
    }
    public Cursor getMessagesByRoom(String Room, SQLiteDatabase sqLiteDatabase){
        String[] projection = {Contract.Message.MESSAGE_ID,
                Contract.Message.MESSAGE_BODY,
                Contract.Message.MESSAGE_ROOM,
                Contract.Message.MESSAGE_TIME,
                Contract.Message.MESSAGE_SENDER_ID,
                Contract.Message.MESSAGE_SENDER_NAME,
                Contract.Message.MESSAGE_FILE_FLAG,
                Contract.Message.MESSAGE_FILE_NAME,
                Contract.Message.MESSAGE_FILE_SIZE,
                Contract.Message.MESSAGE_FILE_LINK,
                Contract.Message.MESSAGE_FILE_PATH};
        String selection = Contract.Message.MESSAGE_ROOM+" LIKE ?";
        String[] selectionArg = {Room};
        Cursor cursor = sqLiteDatabase.query(Contract.Message.TABLE_NAME,projection,selection,selectionArg,null,null,null);
        return  cursor;
    }
    public Cursor getMessagesByFile(String userId, String fileFlag,SQLiteDatabase sqLiteDatabase){
        String[] projection = {Contract.Message.MESSAGE_ID,
                Contract.Message.MESSAGE_BODY,
                Contract.Message.MESSAGE_ROOM,
                Contract.Message.MESSAGE_TIME,
                Contract.Message.MESSAGE_SENDER_ID,
                Contract.Message.MESSAGE_SENDER_NAME,
                Contract.Message.MESSAGE_FILE_FLAG,
                Contract.Message.MESSAGE_FILE_NAME,
                Contract.Message.MESSAGE_FILE_SIZE,
                Contract.Message.MESSAGE_FILE_LINK,
                Contract.Message.MESSAGE_FILE_PATH};
        String selection = Contract.Message.MESSAGE_SENDER_ID+" LIKE ? AND "+Contract.Message.MESSAGE_FILE_FLAG+" LIKE ?";
        String[] selectionArg = {userId,fileFlag};
        Cursor cursor = sqLiteDatabase.query(Contract.Message.TABLE_NAME,projection,selection,selectionArg,null,null,null);
        return  cursor;
    }
    public Cursor getMessagesByUser(String userId, SQLiteDatabase sqLiteDatabase){
        String[] projection = {Contract.Message.MESSAGE_ID,
                Contract.Message.MESSAGE_BODY,
                Contract.Message.MESSAGE_ROOM,
                Contract.Message.MESSAGE_TIME,
                Contract.Message.MESSAGE_SENDER_ID,
                Contract.Message.MESSAGE_SENDER_NAME,
                Contract.Message.MESSAGE_FILE_FLAG,
                Contract.Message.MESSAGE_FILE_NAME,
                Contract.Message.MESSAGE_FILE_SIZE,
                Contract.Message.MESSAGE_FILE_LINK,
                Contract.Message.MESSAGE_FILE_PATH};
        String selection = Contract.Message.MESSAGE_SENDER_ID+" LIKE ?";
        String[] selectionArg = {userId};
        Cursor cursor = sqLiteDatabase.query(Contract.Message.TABLE_NAME,projection,selection,selectionArg,null,null,null);
        return  cursor;
    }
    public Cursor getRoom(SQLiteDatabase sqLiteDatabase){
        String[] projection = {Contract.Room.ROOM_ID,
                Contract.Room.LAST_MESSAGE,
                Contract.Room.UN_READ_COUNT,
                Contract.Room.ROOM_NAME};
        Cursor cursor = sqLiteDatabase.query(Contract.Room.TABLE_NAME,projection,null,null,null,null,null);
        return  cursor;
    }
    public String getRoomName(String id, SQLiteDatabase sqLiteDatabase) {
        String[] projection = {Contract.Room.ROOM_NAME};
        String selection = Contract.Room.ROOM_ID + " LIKE ?";
        String[] selectionArg = {id};
        Cursor cursor = sqLiteDatabase.query(Contract.Room.TABLE_NAME, projection, selection, selectionArg, null, null, null);
        String roomName = id;
        if (cursor.moveToFirst()) {
            do {
                 roomName = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return roomName;
    }


    public Cursor getRoomUnreadMessage(String roomId, SQLiteDatabase sqLiteDatabase){
        String[] projection = {
                Contract.Room.LAST_MESSAGE,
                Contract.Room.UN_READ_COUNT};
        String selection = Contract.Room.ROOM_ID + " LIKE ?";
        String[] selectionArg = {roomId};
        Cursor cursor = sqLiteDatabase.query(Contract.Room.TABLE_NAME,projection,selection,selectionArg,null,null,null);
        return  cursor;
    }


    public void deleteMessage(String id, SQLiteDatabase sqLiteDatabase){
        String selection = Contract.Message.MESSAGE_ID + " LIKE ?";
        String[] selectionArg = {id};
        sqLiteDatabase.delete(Contract.Message.TABLE_NAME, selection, selectionArg);
    }
    public void deleteRoom(String id, SQLiteDatabase sqLiteDatabase){
        String selection = Contract.Room.ROOM_ID + " LIKE ?";
        String[] selectionArg = {id};
        sqLiteDatabase.delete(Contract.Room.TABLE_NAME, selection, selectionArg);
    }

    public  void updateMessage(String id, Messages message, SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Message.MESSAGE_ROOM,message.getMessageRoom());
        contentValues.put(Contract.Message.MESSAGE_BODY,message.getMessageBody());
        contentValues.put(Contract.Message.TABLE_NAME, message.getMessageTime());
        contentValues.put(Contract.Message.MESSAGE_FILE_FLAG,message.getMessageFileFlag());
        contentValues.put(Contract.Message.MESSAGE_SENDER_ID,message.getMessageId());
        contentValues.put(Contract.Message.MESSAGE_SENDER_NAME,message.getMessageSenderName());
        String selection = Contract.Message.MESSAGE_ID+" LIKE ?";
        String[] selectionArg = {id};
        sqLiteDatabase.update(Contract.Message.TABLE_NAME, contentValues, selection, selectionArg);
    }
    public  void updateMessageFilePath(Messages message, SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Message.MESSAGE_FILE_PATH, message.getMessageFilePath());
        String selection = Contract.Message.MESSAGE_ID+" LIKE ?";
        String[] selectionArg = {message.getMessageId()};
        sqLiteDatabase.update(Contract.Message.TABLE_NAME, contentValues, selection, selectionArg);
    }
    public  void updateRoomUnreadMessage(String roomId, SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Room.LAST_MESSAGE, "");
        contentValues.put(Contract.Room.UN_READ_COUNT, 0);
        String selection = Contract.Room.ROOM_ID + " LIKE ?";
        String[] selectionArg = {roomId};
        sqLiteDatabase.update(Contract.Room.TABLE_NAME, contentValues, selection, selectionArg);
    }

    public void LoadData(SQLiteDatabase sqLiteDatabase) {
        int count = 0;
        for (int i=0; i<5; i++){
            ChatRoom cr = new ChatRoom();
            cr.setId(String.valueOf(i));
            cr.setLastMessage("");
            cr.setUnreadCount(i);
            cr.setName("chatRoom " + i);
            cr.setTimestamp("2016-04-21");
            addRoom(cr, sqLiteDatabase);
            for (int j=0; j<5; j++){
                Messages messages = new Messages();
                messages.setMessageId(String.valueOf(count));
                messages.setMessageBody("message " + count);
                messages.setMessageRoom("chatRoom " + i);
                messages.setMessageTime("2016-04-21");
                messages.setMessageSenderId(String.valueOf(j));
                messages.setMessageSenderName("User " + j);
                if(count % 2 == 0){
                    messages.setMessageFileFlag("1");
                }else {
                    messages.setMessageFileFlag("0");
                }
                messages.setMessageFileName("File " + count + ".pdf");
                messages.setMessageFileSize(j + "0 MB");
                messages.setMessageFileLink("uploads/File" + count);
                messages.setMessageFilePath("document/File" + count);
                addMessage(messages, sqLiteDatabase);
                count++;
            }

        }

    }

    public long getRoomCount(SQLiteDatabase sqLiteDatabase) {
        return DatabaseUtils.queryNumEntries(sqLiteDatabase, Contract.Room.TABLE_NAME);
    }
    public void  clearRooms(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("delete from "+  Contract.Room.TABLE_NAME);
    }

}
