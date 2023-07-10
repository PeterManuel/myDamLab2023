package com.classgist.filetransfer;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
public class SharePointActivity extends AppCompatActivity {
    ServerSocket serverSocket = null;
    private TextView chatTextView;
    private Button sendButton;
    private Button loadButton;

    private WifiP2pInfo wifiP2pInfo;
    private Socket socket;

    private OutputStream outputStream;
    private InputStream inputStream;

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private static final int FILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_point);

        chatTextView = findViewById(R.id.chatTextView);
        sendButton = findViewById(R.id.sendButton);
        loadButton = findViewById(R.id.loadButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile();
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        Intent intent = getIntent();
        wifiP2pInfo = intent.getParcelableExtra("wifiP2pInfo");

        broadcastReceiver = new WiFiDirectBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        new WifiDirectTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Set the file type to all types
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }


    private void sendFile(Uri fileUri) {
        try {
            InputStream fileInputStream = getContentResolver().openInputStream(fileUri);
            if (fileInputStream != null) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                fileInputStream.close();
                outputStream.close();
                appendToChat("Me: File sent");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile() {
        // Open a file picker or implement your own file loading logic
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                new SendFileTask().execute(fileUri);
            }
        }
    }

    class SendFileTask extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... params) {
            Uri fileUri = params[0];
            try {
                InputStream fileInputStream = getContentResolver().openInputStream(fileUri);
                if (fileInputStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            appendToChat("Me: File sent");
        }
    }

    private void receiveFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        // Save the received file to the desired location
                    }
                    inputStream.close();
                    appendToChat("Peer: File received");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void appendToChat(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatTextView.append(message + "\n");
            }
        });
    }

    private class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        }
    }

    class WifiDirectTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                try {
                    serverSocket = new ServerSocket(5000);
                    socket = serverSocket.accept();
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return "server";
            } else if (wifiP2pInfo.groupFormed) {
                int port = 5000;
                try {
                    socket = new Socket(wifiP2pInfo.groupOwnerAddress.getHostAddress(), port);
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return "client";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!TextUtils.isEmpty(result)) {
                Toast.makeText(getApplicationContext(), "Connection successful: " + result, Toast.LENGTH_SHORT).show();
                receiveFile();
            }
        }
    }

    // Remaining code for other tasks
}
