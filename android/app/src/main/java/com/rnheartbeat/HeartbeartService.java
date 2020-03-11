package com.rnheartbeat;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.Build;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class HeartbeartService extends Service {

    private static final int SERVICE_NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "HEARTBEAT";
    private WebSocketClient mWebSocketClient;


    private final Handler handler = new Handler();
    private RunnableCode rc = null;

    private final class  RunnableCode implements Runnable {
        private String websocketUrl;
        private Service service;
        public RunnableCode(String websocketUrl, Service service) {
            this.websocketUrl = websocketUrl;
            this.service = service;
        }

        @Override
        public void run() {
            if (mWebSocketClient == null)
                connectWebSocket(websocketUrl, service);
            Context context = getApplicationContext();
            Intent myIntent = new Intent(context, HeartbeatEventService.class);
            context.startService(myIntent);
            HeadlessJsTaskService.acquireWakeLockNow(context);
            handler.postDelayed(this, 2000);
        }
    };

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "HEARTBEAT", importance);
            channel.setDescription("CHANEL DESCRIPTION");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.handler.removeCallbacks(rc);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            String websocketUrl = extras.getString("websocketUrl");
            Log.e("websocketUrl", websocketUrl);
            rc = new RunnableCode(websocketUrl, this);
        }
        this.handler.post(rc);
        createNotificationChannel();
        /*
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Heartbeat service")
                .setContentText("Running...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();
        startForeground(SERVICE_NOTIFICATION_ID, notification);
        */
        return START_STICKY;
    }


    private void connectWebSocket(String websocketUrl, Service service) {
        URI uri;
        try {
            uri = new URI(websocketUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                Log.e("Websocket", message);
                Intent notificationIntent = new Intent(service, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(service, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                Notification notification = new NotificationCompat.Builder(service, CHANNEL_ID)
                        .setContentTitle("Websocket service")
                        .setContentText("Running..."+message)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(contentIntent)
                        .setOngoing(true)
                        .build();
                startForeground(SERVICE_NOTIFICATION_ID, notification);
                //handler.post(runnableCode);
                //runOnUiThread(new Runnable() {
                //    @Override
                //    public void run() {
                //        TextView textView = (TextView)findViewById(R.id.messages);
                //        textView.setText(textView.getText() + "\n" + message);
                //    }
                //});
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }
}
