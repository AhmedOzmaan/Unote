package com.gmail.ahmedozmaan.unote.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.ahmedozmaan.unote.R;
import com.gmail.ahmedozmaan.unote.app.MyApplication;
import com.gmail.ahmedozmaan.unote.helper.MessageDbHelper;

/**
 * Before setting the contentView, we’ll check for user session in shared preferences.
 * If the user is already logged in, the MainActivity will be launched.

 > login() method makes an http request to login endpoint by passing name and email as post parameters.
 On the server a new user will be created and the json response will be served.

 > After parsing the json, user session will be created
 by storing the user object in shared preferences and MainActivity will be launched.
 */
public class VerifyActivity extends AppCompatActivity {
    private String TAG = VerifyActivity.class.getSimpleName();
    private EditText inputCode;
    private Button btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Check for login session. It user is already logged in
         * redirect him to main activity
         * */
        setContentView(R.layout.activity_verify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inputCode = (EditText) findViewById(R.id.input_code);
        btnEnter = (Button) findViewById(R.id.btn_enter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkTheCode();
            }
        });
        if( MyApplication.getInstance().getPrefManager().getSentCode() == 0) {
            sendSMSMessage();
        }
        dibulaabo();
    }
    public void dibulaabo(){

//qoraalka hoos ka muuqda
        SpannableString TotalString = new SpannableString("If you do not recieve the code click here to know the error, or re-new the account, click here");
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                ContextThemeWrapper ctw = new ContextThemeWrapper(VerifyActivity.this, R.style.WindowTitleStyle);
                AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
                builder.setTitle("POSSIBLE ERRORS:");
                builder.setMessage("1.Service unavailable\n 2.Airplane mode on\n 3.Poor connection\n 4.Zero balance\n 5.Jam network");
                builder.setCancelable(false)

                        .setPositiveButton("Go Back", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                builder.show();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.parseColor("#2196F3"));
                ds.setTypeface(Typeface.DEFAULT_BOLD);
                ds.setTypeface(Typeface.SANS_SERIF);
            }
        };
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                MyApplication.getInstance().logout();
                //getApplication().deleteDatabase(MessageDbHelper.DATABASE_NAME);
                startActivity(new Intent(VerifyActivity.this, LoginActivity.class));
                finish();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setTypeface(Typeface.DEFAULT_BOLD);
                ds.setTypeface(Typeface.SANS_SERIF);
                ds.setColor(Color.parseColor("#E040FB"));
            }
        };
        TotalString.setSpan(clickableSpan1, 31, 41, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TotalString.setSpan(clickableSpan2, 84, 94, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) findViewById(R.id.textView_clickable);
        textView.setText(TotalString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }
    private void checkTheCode(){
        final String code = inputCode.getText().toString();
        if(MyApplication.getInstance().getPrefManager().getCode().equals(code)){
            MyApplication.getInstance().getPrefManager().storeVerify(1);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        else {
            ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.WindowTitleStyle);
            AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
            builder.setTitle("INVALID CODE:");
            builder.setMessage("Enter the 5-digit code you recieved via SMS in the 'Access Code' field and try again.");
            builder.setCancelable(false)

                    .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            builder.show();
        }
    }

    protected void sendSMSMessage() {
        int rondombin = (int) (Math.random() * 90000) + 10000;
        String rumdom = String.valueOf(rondombin);
        MyApplication.getInstance().getPrefManager().storeCode(rumdom);
        String lastString = "Welcome To Unote \nYour verification code is: " + rumdom;
        String phoneNo = MyApplication.getInstance().getPrefManager().getUser().getPhone();
        try {
            String SENT = "sent";
            String DELIVERED = "delivered";
            Intent sentIntent = new Intent(SENT);
     /*Create Pending Intents*/
            PendingIntent sentPI = PendingIntent.getBroadcast(
                    getApplicationContext(), 0, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Intent deliveryIntent = new Intent(DELIVERED);

            PendingIntent deliverPI = PendingIntent.getBroadcast(
                    getApplicationContext(), 0, deliveryIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
     /* Register for SMS send action */
            registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String result = "Code not Sent";
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            result = "Code Sent successfully";
                            MyApplication.getInstance().getPrefManager().storeSentCode(1);
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            result = "Code not Sent";
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            result = "Airplane mode on";
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            result = "No PDU defined";
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            result = "No service";
                            break;
                    }
                    Toast.makeText(getApplicationContext(), result,
                            Toast.LENGTH_LONG).show();
                }

            }, new IntentFilter(SENT));
     /* Register for Delivery event */
            registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    Toast.makeText(getApplicationContext(), "Deliverd",
                            Toast.LENGTH_LONG).show();
                }

            }, new IntentFilter(DELIVERED));

      /*Send SMS*/
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, lastString, sentPI,
                    deliverPI);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),
                    ex.getMessage().toString(), Toast.LENGTH_LONG)
                    .show();
            ex.printStackTrace();
        }
    }

}
