package com.gmail.ahmedozmaan.unote.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.gmail.ahmedozmaan.unote.R;
import com.gmail.ahmedozmaan.unote.app.EndPoints;
import com.gmail.ahmedozmaan.unote.app.MyApplication;
import com.gmail.ahmedozmaan.unote.model.User;
/**
 * Before setting the contentView, we’ll check for user session in shared preferences.
 * If the user is already logged in, the MainActivity will be launched.

 > login() method makes an http request to login endpoint by passing name and email as post parameters.
 On the server a new user will be created and the json response will be served.

 > After parsing the json, user session will be created
 by storing the user object in shared preferences and MainActivity will be launched.
 */
public class LoginActivity extends AppCompatActivity {
    private String TAG = LoginActivity.class.getSimpleName();
    private EditText inputPhone;
    private TextInputLayout inputLayoutPhone;
    private Button btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Check for login session. It user is already logged in
         * redirect him to main activity
         * */
        if (MyApplication.getInstance().getPrefManager().getUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inputPhone = (EditText) findViewById(R.id.input_phone);
        btnEnter = (Button) findViewById(R.id.btn_enter);

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String myphone = inputPhone.getText().toString();
                String id = inputPhone.getText().toString().trim();
                if (id.equals("")) {
                    inputPhone.setError("Fadan gali lambarkaaga");
                    return;
                }
                if(myphone.length()==9 && myphone != null && myphone.matches("61[0-9]{7,9}"))       {
                    // Check if no view has focus:
                    view = LoginActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    ContextThemeWrapper ctw = new ContextThemeWrapper(LoginActivity.this, R.style.WindowTitleStyle);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
                    builder.setTitle("NUMBER CONFIRMATION:");
                    builder.setMessage("+252" + inputPhone.getText().toString() + "\n\nis your phone number above correct?");
                    builder.setCancelable(false)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    login();
                                }
                            })
                            .setNegativeButton("EDIT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    builder.show();
                }
                else {
                    inputPhone.setError("Fadlan lambar saxan gali");
                }
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
    /**
     * logging in user. Will make http post request with name, email
     * as parameters
     */
    private void login() {
        final String phone = inputPhone.getText().toString();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        // user successfully logged in

                        JSONObject userObj = obj.getJSONObject("user");
                        User user = new User();
                        user.setName(userObj.getString("name"));
                        user.setEmail(userObj.getString("email"));
                        user.setClazz(userObj.getString("clazz"));
                        user.setPhone(userObj.getString("phone"));
                        user.setId(userObj.getString("user_id"));

                        // storing user in shared preferences
                        MyApplication.getInstance().getPrefManager().storeUser(user);

                        // start main activity
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    } else {
                        // login error - simply toast the message
                        Toast.makeText(getApplicationContext(), "lambarkan ma diiwaan gashano", Toast.LENGTH_LONG).show();

                        //  Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Error In Connection  ", Toast.LENGTH_SHORT).show();
                }
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
                params.put("phone", phone);
                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }


}
