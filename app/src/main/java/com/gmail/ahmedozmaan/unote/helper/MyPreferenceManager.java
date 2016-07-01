package com.gmail.ahmedozmaan.unote.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gmail.ahmedozmaan.unote.model.User;

/**
 * Created by Lincoln on 07/01/16.
 * This class stores data in SharedPreferences.
 * Here we temporarily stores the unread push notifications in order to append them to new messages.
 *
 *
 * and add storeUser() and getUser() methods which stores the user information in shared preferences.
 * These methods will be called once the user successfully logged in.
 */
public class MyPreferenceManager {

    private String TAG = MyPreferenceManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "unote";

    // All Shared Preferences Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_CLAZZ = "user_clazz";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_VERIFICATION = "verification";
    private static final String KEY_CODE = "code";
    private static final String KEY_SENT_CODE = "sent_code";
    private static final String KEY_REGISTERED_ON_GCM = "registered";

    // Constructor
    public MyPreferenceManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_CLAZZ, user.getClazz());
        editor.commit();

        Log.e(TAG, "User is stored in shared preferences. " + user.getName() + ", " + user.getEmail());
    }
    public void storeVerify(int verify) {
        editor.putInt(KEY_VERIFICATION, verify);
        editor.commit();
    }
    public int getVerify() {
        return pref.getInt(KEY_VERIFICATION, 0);
    }
    public void storeCode(String code) {
        editor.putString(KEY_CODE, code);
        editor.commit();
    }
    public String getCode() {
        return pref.getString(KEY_CODE, null);
    }
    public void storeRegistered(int code) {
        editor.putInt(KEY_REGISTERED_ON_GCM, code);
        editor.commit();
    }
    public int getRegistered() {
        return pref.getInt(KEY_REGISTERED_ON_GCM, 0);
    }
    public void storeSentCode(int code) {
        editor.putInt(KEY_SENT_CODE, code);
        editor.commit();
    }
    public int getSentCode() {
        return pref.getInt(KEY_SENT_CODE, 0);
    }

    public User getUser() {
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, name, email, phone, clazz;
            id = pref.getString(KEY_USER_ID, null);
            name = pref.getString(KEY_USER_NAME, null);
            email = pref.getString(KEY_USER_EMAIL, null);
            phone = pref.getString(KEY_USER_PHONE, null);
            clazz = pref.getString(KEY_USER_CLAZZ, null);

            User user = new User(id, name, email, phone ,clazz);
            return user;
        }
        return null;
    }

    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
