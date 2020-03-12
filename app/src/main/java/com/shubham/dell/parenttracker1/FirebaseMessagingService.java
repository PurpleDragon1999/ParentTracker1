package com.shubham.dell.parenttracker1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    int notificationId=1;
    CurrentUser mCurrentUser=new CurrentUser();
    String token;
    private static final int NOTIFICATION_ID = 0;
   @Override
    public void onNewToken(String s) {
       /* FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.d("refreshedToken", "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                token = task.getResult().getToken();

                // Log and toast

                Log.d("refreshedToken", "Token "+token);
                SendTokenToServer tokenToServer=new SendTokenToServer(token);
            }
        });*/

       //SendTokenToServer tokenToServer=new SendTokenToServer(s);

        super.onNewToken(s);
       DatabaseReference ref= FirebaseDatabase.getInstance().getReference("STUDENTS")
               .child(mCurrentUser.getUsername())
               .child("deviceToken");
       ref.setValue(s);
       Log.d("DeviceTokenBhadwa", "onNewToken");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String messageTitle=remoteMessage.getNotification().getTitle();
        String messageBody=remoteMessage.getNotification().getBody();
//        Toast.makeText(this, "Notify", Toast.LENGTH_SHORT).show();
        sendNotification(messageTitle,messageBody);
    }
    private Notification createNotification(String messageTitle,String messageBody,PendingIntent notificationPendingIntent) {
        Log.d("Status", "Inside createNotification");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "101");
        notificationBuilder
                .setSmallIcon(R.drawable.baseline_notifications_active_black_18dp)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setContentIntent(notificationPendingIntent)
                .setDefaults(android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.DEFAULT_VIBRATE | android.app.Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }
    private void sendNotification(String messageTitle,String messageBody) {
        Log.d("Status", "sendNotification: " + messageBody);

        // Intent to start the main Activity
        Intent notificationIntent = MainActivity.makeNotificationIntent(
                getApplicationContext(), messageBody);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        createMyNotificationChannel();

        Log.d("Status", "Calling createNotification");
        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificatioMng.notify(
                NOTIFICATION_ID,
                createNotification(messageTitle,messageBody, notificationPendingIntent));

    }
    private void createMyNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Attendance Channel";
            String description = "Testing";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("101", name, importance);
            channel.setDescription(description);
            channel.enableLights(true);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}


