package com.gmail.ahmedozmaan.unote.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gmail.ahmedozmaan.unote.R;
import com.gmail.ahmedozmaan.unote.app.EndPoints;
import com.gmail.ahmedozmaan.unote.app.MyApplication;
import com.gmail.ahmedozmaan.unote.gcm.GcmIntentService;
import com.gmail.ahmedozmaan.unote.helper.MessageDbHelper;
import com.gmail.ahmedozmaan.unote.model.ChatRoom;
import com.gmail.ahmedozmaan.unote.model.DeviceDocuments;
import com.gmail.ahmedozmaan.unote.model.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SendMessageActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener {

    private String TAG = SendMessageActivity.class.getSimpleName();

    Messages uploadingMessages = new Messages();
    private String selectedFilePath, selectedChatRoom;
    public static String SERVER_URL = "http://ahmedozmaan.16mb.com/extra/UploadToServer.php";
    static final int CUSTOM_DIALOG_ID = 0;  File root;
    EditText messageBody;
    TextView messageSender, fileNameTextView, fileSizeTextView;
    Button btnSend, attachButton,btn_cancel ;
    ImageView imagefile;
    ListView dialog_ListView;
    Spinner spinner;
    ArrayList<String> chatRoomsIds, chatRoomsName;

    DeviceDocuments documents = new DeviceDocuments();
    ProgressBar progressBar;
    ProgressDialog progressFile;
    ArrayAdapter<String> adapter;
    RelativeLayout fileLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        messageBody = (EditText) findViewById(R.id.message);
        messageSender = (TextView) findViewById(R.id.message_sender);
        messageSender.setText("From: "+ MyApplication.getInstance().getPrefManager().getUser().getName());

        btn_cancel = (Button)findViewById(R.id.btn_cancel_message);btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLecturerActivity();
            }
        });

        spinner = (Spinner) findViewById(R.id.chat_room_spinner);


        fetchChatRoomsNew();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, chatRoomsName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        progressBar = (ProgressBar)findViewById(R.id.prgressbar_good);
        attachButton = (Button) findViewById(R.id.attach_button);
        progressFile = new ProgressDialog(this);
        spinner.setAdapter(adapter);
        root = new File(Environment.getExternalStorageDirectory().getParent());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                uploadingMessages.setMessageRoom(chatRoomsIds.get(position));
                selectedChatRoom = chatRoomsName.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Intent intent = getIntent();
        getSupportActionBar().setTitle("Send message with file");
        fileLayout = (RelativeLayout)findViewById(R.id.file);
        uploadingMessages.setMessageFileFlag(intent.getStringExtra("file_flag"));
        if(uploadingMessages.getMessageFileFlag().equals("0")){
            uploadingMessages.setMessageFileSize("0");
            uploadingMessages.setMessageFileName("0");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Send message");
            fileLayout.setVisibility(View.GONE);

            btnSend = (Button) findViewById(R.id.btn_send_message); btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendMessage();

                }
            });
        }else
        {
            imagefile = (ImageView)findViewById(R.id.file_icon); attachButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(CUSTOM_DIALOG_ID);
                }
            });
            btnSend = (Button) findViewById(R.id.btn_send_message); btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                attachButton.setEnabled(false);
                progressFile.setTitle("SEND FILE");
                progressFile.setMessage("Please wait...");
                progressFile.setCancelable(false);
                progressFile.show();
                prepareFile();
                }
            });
            fileNameTextView = (TextView) findViewById(R.id.file_name);
            fileSizeTextView = (TextView) findViewById(R.id.file_size);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        }
    }

     Dialog finalDialog1 = null; Dialog finalDialog = null;
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
            case CUSTOM_DIALOG_ID:
                dialog = new Dialog(SendMessageActivity.this);dialog.setContentView(R.layout.dialoglayout_last); dialog.setTitle("Choose file");dialog.setCancelable(true);dialog.setCanceledOnTouchOutside(true);
                final TabHost host;
                finalDialog1 = dialog;finalDialog = dialog;
                int layoutResources = R.layout.doc_layout;
                ListAllFiles(root);
                dialog_ListView = (ListView)dialog.findViewById(R.id.dialog_ListView_last);
                dialog_ListView.setOnItemClickListener(this);
                host = (TabHost)dialog.findViewById(R.id.tabHost);
                host.setup();
                //First Tab
                //Tab 1
                TabHost.TabSpec spec = host.newTabSpec("Tab PDF");
                spec.setContent(R.id.tab);
                spec.setIndicator("PDF");
                imagefile.setImageResource(R.mipmap.pdf_icon);
                host.addTab(spec);

                //Tab 2
                spec = host.newTabSpec("Tab PPT");
                spec.setContent(R.id.tab);
                spec.setIndicator("PPT");
                host.addTab(spec);
                //Tab 3
                spec = host.newTabSpec("Tab DOC");
                spec.setContent(R.id.tab);
                spec.setIndicator("DOC");
                spec.setIndicator("DOC");
                host.addTab(spec);
                //Tab 4
                spec = host.newTabSpec("Tab TXT");
                spec.setContent(R.id.tab);
                spec.setIndicator("TXT");
                host.addTab(spec);
                host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                    public void onTabChanged(String tabId) {
                        ArrayAdapter directoryList;
                        switch (host.getCurrentTab()) {
                            case 0:
                                directoryList = new ArrayAdapter(getBaseContext(), R.layout.pdf_layout, R.id.files_id_pdf, documents.PDF.getFileName());
                                documents.setFlag(1);
                                dialog_ListView.setAdapter(directoryList);
                                imagefile.setImageResource(R.mipmap.pdf_icon);

                                break;
                            case 1:
                                directoryList = new ArrayAdapter(getBaseContext(), R.layout.ppt_layout, R.id.files_id_ppt, documents.PPT.getFileName());
                                documents.setFlag(2);
                                dialog_ListView.setAdapter(directoryList);
                                imagefile.setImageResource(R.mipmap.ppt_icon);
                                break;
                            case 2:
                                directoryList = new ArrayAdapter(getBaseContext(), R.layout.doc_layout, R.id.files_id_doc, documents.DOC.getFileName());
                                documents.setFlag(3);
                                dialog_ListView.setAdapter(directoryList);
                                imagefile.setImageResource(R.mipmap.doc_icon);

                                break;
                            case 3:
                                directoryList = new ArrayAdapter(getBaseContext(), R.layout.txt_layout, R.id.files_id_txt, documents.TXT.getFileName());
                                documents.setFlag(4);
                                dialog_ListView.setAdapter(directoryList);
                                imagefile.setImageResource(R.mipmap.txt_icon);

                                break;
                            default:
                                break;
                        }
                    }
                });
                host.setCurrentTab(1);
        }
        return dialog;
    }
    private String  getFileName(File file){
        String modifiedFileSize = null;
        double fileSize = 0.0;
        fileSize = (double) file.length();//in Bytes
        Date lastModified = new Date(file.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy  HH:mm");
        String formattedDateString = formatter.format(lastModified);
        if (fileSize < 1024) {
            modifiedFileSize = String.valueOf(fileSize).concat("B");
        } else if (fileSize > 1024 && fileSize < (1024 * 1024)) {
            modifiedFileSize = String.valueOf(Math.round((fileSize / 1024 * 100.0)) / 100.0).concat("KB");
        } else {
            modifiedFileSize = String.valueOf(Math.round((fileSize / (1024 * 1024) * 100.0)) / 100.0).concat("MB");
        }
      return  file.getName()+"\n\n"+  formattedDateString+ "\t\t" +modifiedFileSize;
    }
    private void   getSelectedFile(String  path){
        selectedFilePath = path;
        File file = new File(path);
        String modifiedFileSize = null;
        double fileSize = 0.0;
        fileSize = (double) file.length();//in Bytes
        if (fileSize < 1024) {
            modifiedFileSize = String.valueOf(fileSize).concat("B");
        } else if (fileSize > 1024 && fileSize < (1024 * 1024)) {
            modifiedFileSize = String.valueOf(Math.round((fileSize / 1024 * 100.0)) / 100.0).concat("KB");
        } else {
            modifiedFileSize = String.valueOf(Math.round((fileSize / (1024 * 1024) * 100.0)) / 100.0).concat("MB");
        }
        fileNameTextView.setText(file.getName());
        fileSizeTextView.setText(modifiedFileSize);
        uploadingMessages.setMessageFileFlag("1");
    }
    void ListAllFiles(File f){
        File listFile[] = f.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    ListAllFiles(listFile[i]);
                }
                else {
                    if (listFile[i].getName().endsWith(".pdf")){
                        documents.PDF.addFiLeName(getFileName(listFile[i]));
                        documents.PDF.addFiLePath(listFile[i].getPath());
                    }else  if (listFile[i].getName().endsWith(".ppt") || listFile[i].getName().endsWith(".pptx")){
                        documents.PPT.addFiLeName(getFileName(listFile[i]));
                        documents.PPT.addFiLePath(listFile[i].getPath());
                    }else  if (listFile[i].getName().endsWith(".doc") || listFile[i].getName().endsWith(".docx")){
                        documents.DOC.addFiLeName(getFileName(listFile[i]));
                        documents.DOC.addFiLePath(listFile[i].getPath());
                    }else  if (listFile[i].getName().endsWith(".txt")){
                    documents.TXT.addFiLeName(getFileName(listFile[i]));
                    documents.TXT.addFiLePath(listFile[i].getPath());
                }
                }
            }
        }
    }
    public int uploadFile(final String selectedFilePath){
        int serverResponseCode = 0;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);

        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileSizeTextView.setText("Source File Doesn't Exist: " + selectedFilePath);
                    progressBar.setVisibility(View.GONE);
                    progressFile.dismiss();
                    attachButton.setEnabled(true);
                }
            });
            return 0;
        }else{
            try{
                progressBar.setVisibility(View.VISIBLE);
                final FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(SERVER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",selectedFilePath);

                //creating new data output stream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data output stream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to bufferSize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputStream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileNameTextView.setText("File Upload completed.");
                            progressBar.setVisibility(View.GONE);
                            attachButton.setEnabled(true);
                            progressFile.dismiss();
                            sendMessage();
                        }
                    });
                }
                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SendMessageActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                        attachButton.setEnabled(true);
                        progressFile.dismiss();
                        progressBar.setVisibility(View.GONE);
                    }
                });

                Log.e("Upload file to server", "error: " + e.getMessage(), e);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SendMessageActivity.this, "URL error!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        progressFile.dismiss();
                        attachButton.setEnabled(true);
                    }
                });
                Log.e("Upload file to server", "error: " + e.getMessage(), e);
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SendMessageActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        progressFile.dismiss();
                        attachButton.setEnabled(true);

                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SendMessageActivity.this, "Got Exception : see logcat ",Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        progressFile.dismiss();
                        attachButton.setEnabled(true);
                    }
                });
            }
            return serverResponseCode;
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void sendMessage() {
        uploadingMessages.setMessageBody(this.messageBody.getText().toString().trim());
        if (TextUtils.isEmpty(uploadingMessages.getMessageBody()) || TextUtils.isEmpty(uploadingMessages.getMessageRoom())) {
            Toast.makeText(getApplicationContext(), "Fill required fields", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            attachButton.setEnabled(true);
            return;
        }
        final   ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("SEND MESSAGE");
        progress.setMessage("Please wait...");
        progress.setCancelable(false);
        progress.show();
        uploadingMessages.setMessageSenderName(MyApplication.getInstance().getPrefManager().getUser().getName());
        uploadingMessages.setMessageSenderId(MyApplication.getInstance().getPrefManager().getUser().getId());
        String endPoint = EndPoints.CHAT_ROOM_MESSAGE.replace("_ID_", uploadingMessages.getMessageRoom());

        Log.e(TAG, "endpoint: " + endPoint);

        this.messageBody.setText("");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String data) {
                Log.e(TAG, "response: " + data);

                try {
                    JSONObject datObj = new JSONObject(data);

                    // check for error
                    if (datObj.getBoolean("error") == false) {
                        JSONObject mObj = datObj.getJSONObject("message");
                        JSONObject uObj = datObj.getJSONObject("user");
                            Messages messages = new Messages();
                            messages.setMessageId(mObj.getString("message_id"));
                            messages.setMessageBody(mObj.getString("message"));
                            messages.setMessageTime(mObj.getString("created_at"));
                            messages.setMessageRoom(selectedChatRoom);
                            messages.setMessageFileFlag(mObj.getString("file_flag"));
                            messages.setMessageFileName(mObj.getString("file_name"));
                            messages.setMessageFileSize(mObj.getString("file_size"));
                            messages.setMessageFileLink(mObj.getString("file_link"));
                            if(messages.getMessageFileFlag().equals("1")){
                                messages.setMessageFilePath(selectedFilePath);
                                Log.e(TAG, "Selected path: " + selectedFilePath);
                            }else {
                                messages.setMessageFilePath("0");
                            }
                            messages.setMessageSenderId(uObj.getString("sender_id"));
                            messages.setMessageSenderName(uObj.getString("sender_name"));

                        MessageDbHelper messageDbHelper = new MessageDbHelper(getApplicationContext());
                        SQLiteDatabase sqLiteDatabase = messageDbHelper.getWritableDatabase();

                        messageDbHelper.addMessage(messages, sqLiteDatabase);
                        sqLiteDatabase.close();
                        // To dismiss the dialog progressBar.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        attachButton.setEnabled(true);
                        progress.dismiss();
                        launchLecturerActivity();

                    } else {
                        Toast.makeText(getApplicationContext(), "Error Connection", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        attachButton.setEnabled(true);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    attachButton.setEnabled(true);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Error Connection: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                attachButton.setEnabled(true);
            }


        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", uploadingMessages.getMessageSenderId());
                params.put("user_name", uploadingMessages.getMessageSenderName());
                params.put("message", uploadingMessages.getMessageBody());
                params.put("file_flag", uploadingMessages.getMessageFileFlag());
                params.put("file_name", uploadingMessages.getMessageFileName());
                params.put("file_size", uploadingMessages.getMessageFileSize());
                Log.e(TAG, "Params: " + params.toString());

                return params;
            };
        };


        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }
    private void prepareFile(){
        uploadingMessages.setMessageBody(this.messageBody.getText().toString().trim());
        if (TextUtils.isEmpty(uploadingMessages.getMessageBody()) || TextUtils.isEmpty(uploadingMessages.getMessageRoom())) {
            Toast.makeText(getApplicationContext(), "Fill required fields", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            attachButton.setEnabled(true);
            return;
        }
        if (uploadingMessages.getMessageFileFlag().equals("1")){
            uploadingMessages.setMessageFileSize(fileSizeTextView.getText().toString());
            if(uploadingMessages.getMessageFileSize().equals(0)){
                progressBar.setVisibility(View.GONE);
                progressFile.dismiss();
                attachButton.setEnabled(true);
                Toast.makeText(SendMessageActivity.this, "Please attach file first", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadingMessages.setMessageFileName(fileNameTextView.getText().toString());
            //on upload button Click
            if (selectedFilePath != null) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //creating new thread to handle Http Operations
                            uploadFile(selectedFilePath);
                        }
                    }).start();
                } else {
                    Toast.makeText(SendMessageActivity.this, "Please check the connection", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    progressFile.dismiss();
                    attachButton.setEnabled(true);
                }
            }
            else{
                Toast.makeText(SendMessageActivity.this,"Please choose a File First",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                attachButton.setEnabled(true);
                progressFile.dismiss();
            }
        }
    }
    private void launchLecturerActivity() {
        Intent intent = new Intent(SendMessageActivity.this, LecturerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    public void fetchChatRoomsNew(){
        chatRoomsIds = new ArrayList<String>();
        chatRoomsName = new ArrayList<String>();
        MessageDbHelper messageDbHelper = new MessageDbHelper(getBaseContext());
        SQLiteDatabase sqLiteDatabase = messageDbHelper.getReadableDatabase();
        Cursor cursor = messageDbHelper.getRoom(sqLiteDatabase);
        if (cursor.moveToFirst()) {
            do {
                chatRoomsIds.add(cursor.getString(0));
                chatRoomsName.add(cursor.getString(3));
            }while (cursor.moveToNext());
        }
        else{
            sqLiteDatabase.close();
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

       try {

           getSelectedFile(documents.getSelectedFilePath(position));
           dismissDialog(CUSTOM_DIALOG_ID);
        }
        catch (Exception e) {
            dismissDialog(CUSTOM_DIALOG_ID);

       }
    }

    public static class UploadTask extends AsyncTask<String, Integer, String> {
        private ProgressDialog mPDialog;
        private Context mContext;
        private PowerManager.WakeLock mWakeLock;
        private File mTargetFile;
        public UploadTask(Context context,File targetFile,String dialogMessage) {
            this.mContext = context;
            this.mTargetFile = targetFile;
            mPDialog = new ProgressDialog(context);

            mPDialog.setMessage(dialogMessage);
            mPDialog.setIndeterminate(true);
            mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mPDialog.setCancelable(false);
            // reference to instance to use inside listener
            final UploadTask me = this;
            mPDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    me.cancel(true);
                }
            });
            Log.i("DownloadTask", "Constructor done");
        }

        @Override
        protected String doInBackground(String... sUrl) {
            return "kk";
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();

            mPDialog.show();

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mPDialog.setIndeterminate(false);
            mPDialog.setMax(100);
            mPDialog.setProgress(progress[0]);

        }
        public int uploadFile(final String selectedFilePath){
            int serverResponseCode = 0;
            HttpURLConnection connection;
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            int bytesRead,bytesAvailable,bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File selectedFile = new File(selectedFilePath);

            String[] parts = selectedFilePath.split("/");
            final String fileName = parts[parts.length-1];

            if (!selectedFile.isFile()){
                //fileSizeTextView.setText("Source File Doesn't Exist: " + selectedFilePath);
                return 0;
            }else{
                try{
                    final FileInputStream fileInputStream = new FileInputStream(selectedFile);
                    URL url = new URL(SERVER_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//Allow Inputs
                    connection.setDoOutput(true);//Allow Outputs
                    connection.setUseCaches(false);//Don't use a cached Copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file",selectedFilePath);

                    //creating new data output stream
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());

                    //writing bytes to data output stream
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + selectedFilePath + "\"" + lineEnd);

                    dataOutputStream.writeBytes(lineEnd);

                    //returns no. of bytes present in fileInputStream
                    bytesAvailable = fileInputStream.available();
                    //selecting the buffer size as minimum of available bytes or 1 MB
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    //setting the buffer as byte array of size of bufferSize
                    buffer = new byte[bufferSize];

                    //reads bytes from FileInputStream(from 0th index of buffer to bufferSize)
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);

                    //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                    while (bytesRead > 0){
                        //write the bytes read from inputStream
                        dataOutputStream.write(buffer,0,bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable,maxBufferSize);
                        bytesRead = fileInputStream.read(buffer,0,bufferSize);
                    }
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    serverResponseCode = connection.getResponseCode();
                    String serverResponseMessage = connection.getResponseMessage();

                    //Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                    //response code of 200 indicates the server status OK
                    if(serverResponseCode == 200){
                                //"File Upload completed.");
                               // sendMessage();
                    }
                    //closing the input and output streams
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //"File Not Found";
                    Log.e("Upload file to server", "error: " + e.getMessage(), e);
                } catch (MalformedURLException e) {
                    e.printStackTrace();//"URL error!"
                    Log.e("Upload file to server", "error: " + e.getMessage(), e);
                } catch (IOException e) {
                    e.printStackTrace();//"Cannot Read/Write File!"
                }
                catch (Exception e) {
                    e.printStackTrace();//"Got Exception : see logcat "
                }
                return serverResponseCode;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("DownloadTask", "Work Done! PostExecute");
            mWakeLock.release();
            mPDialog.dismiss();
            if (result != null) {
                Log.i("Download error: " + result, "canceled");
                Toast.makeText(mContext, "Download error: " + result, Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(mContext,"File Downloaded", Toast.LENGTH_SHORT).show();
           // MessageDbHelper messageDbHelper = new MessageDbHelper(mContext);
            //SQLiteDatabase writeSqLiteDatabase = messageDbHelper.getWritableDatabase();
            //messageDbHelper.updateMessageFilePath(selectedMessages,writeSqLiteDatabase);
            //mAdapter.notifyDataSetChanged();
        }
    }
}
