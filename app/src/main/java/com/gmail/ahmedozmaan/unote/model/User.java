package com.gmail.ahmedozmaan.unote.model;

import java.io.Serializable;

/**
 * Created by Lincoln on 07/01/16.
 * create three classes named User.java, Message.java and ChatRoom.java.
 * Theses classes will be used to create objects while parsing the json responses. You can also notice that,
 * these classes implements Serializable which allows us to pass objects to an activity using intents.
 */
public class User implements Serializable {
    String id;
    String name;
    String email;
    String phone;
    String clazz;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }


    public User() {
    }

    public User(String id, String name, String email,  String phone, String clazz) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.clazz = clazz;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
