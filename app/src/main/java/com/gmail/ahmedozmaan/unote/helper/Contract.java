package com.gmail.ahmedozmaan.unote.helper;

/**
 * Created by AhmedOzmaan on 2/10/2016.
 */
public class Contract {
    public static abstract class Message {
        public static final String MESSAGE_ID = "_id";
        public static final String MESSAGE_ROOM = "message_room";
        public static final String MESSAGE_BODY ="message_body";
        public static final String MESSAGE_TIME ="message_time";
        public static final String MESSAGE_SENDER_ID ="message_sender_id";
        public static final String MESSAGE_SENDER_NAME ="message_sender_name";
        public static final String MESSAGE_FILE_FLAG ="message_file_flag";
        public static final String MESSAGE_FILE_NAME = "message_file_name";
        public static final String MESSAGE_FILE_SIZE ="message_file_size";
        public static final String MESSAGE_FILE_PATH ="message_file_path";
        public static final String MESSAGE_FILE_LINK ="message_file_link";
        public static final String TABLE_NAME = "unote_message";
    }
    public static abstract class Room {
        public static final String ROOM_ID = "_id";
        public static final String ROOM_NAME = "message_address";
        public static final String LAST_MESSAGE = "last_message";
        public static final String UN_READ_COUNT = "un_read_count";
        public static final String TABLE_NAME = "unote_room";
    }
}
