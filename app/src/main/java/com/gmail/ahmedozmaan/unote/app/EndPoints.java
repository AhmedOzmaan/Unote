package com.gmail.ahmedozmaan.unote.app;

/**
 * Created by Lincoln on 06/01/16.
 * Here we declare the REST API endpoint urls.
 * If you are testing the app on localhost, use the correct Ip address of the computer on which php services are running.
 */
public class EndPoints {

    // localhost url
    //public static final String BASE_URL = "http://192.168.20.102/unote/v1";

    public static final String BASE_URL = "http://ahmedozmaan.16mb.com/v1";
    public static final String LOGIN = BASE_URL + "/user/login";
    public static final String USER = BASE_URL + "/user/_ID_";
    public static final String CHAT_ROOMS = BASE_URL + "/chat_rooms";
    public static final String USER_CHAT_ROOMS = BASE_URL + "/user_chat_rooms";
    public static final String CHAT_THREAD = BASE_URL + "/chat_rooms/_ID_";
    public static final String CHAT_ROOM_MESSAGE = BASE_URL + "/chat_rooms/_ID_/message";
}
