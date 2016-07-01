package com.gmail.ahmedozmaan.unote.model;

import java.io.Serializable;

/**
 * Created by Lincoln on 07/01/16.
 * create three classes named User.java, Message.java and ChatRoom.java.
 * Theses classes will be used to create objects while parsing the json responses. You can also notice that,
 * these classes implements Serializable which allows us to pass objects to an activity using intents.
 */
public class ChatRoom implements Serializable {
    int unreadCount;
    String id, name, lastMessage, timestamp;

    public ChatRoom() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
