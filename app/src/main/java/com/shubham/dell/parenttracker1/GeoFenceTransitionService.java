package com.shubham.dell.parenttracker1;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class GeoFenceTransitionService extends IntentService {

    private static final int GEOFENCE_NOTIFICATION_ID = 0;
    private DatabaseReference mDatabaseReference;
    CurrentUser mCurrentUser = new CurrentUser();

    public GeoFenceTransitionService() {
        super("GeofenceTransitionsIntentService");
        Log.d("Status", "GeoFenceTransitionService Class");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Status", "Handling Intent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.d("Status", errorMsg);
            return;
        }

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        if(geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences);
            sendNotification(geofenceTransitionDetails);
            Log.d("Status", geofenceTransitionDetails);

        }
        else{
            Log.d("Status", "kfksjnsvdkjkjdnvjdjbvkdjskjvb");
        }

        //Getting Bus Location
        /*mDatabaseReference = FirebaseDatabase.getInstance().getReference("BUS_ROUTE")
                .child(mCurrentUser.getRoute());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double lat = dataSnapshot.child("lat").getValue(Double.class);
                Double lon = dataSnapshot.child("lon").getValue(Double.class);

                Log.d("Status", "GeoFenceTransitionService :  Lat" + lat + "lon" + lon);
                //LatLng bus_loc = new LatLng(lat, lon);

                Location bus = new Location(LocationManager.GPS_PROVIDER);
                bus.setLatitude(lat);
                bus.setLongitude(lon);

                Location center = new Location(LocationManager.GPS_PROVIDER);
                center.setLatitude(28.646466646);
                center.setLongitude(72.66691641);

                float distance = center.distanceTo(bus);
                if (distance > 40)
                    sendNotification("Outside The GeoFence");
                else if (distance < 40)
                    sendNotification("Inside The GeoFence");
                else if (distance == 40)
                    sendNotification("Beware! You are at the Boundary.");

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(GeoFenceTransitionService.this, "error in getting location", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        String geofenceTransitionString = getTransitionString(geoFenceTransition);
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesList.add(geofence.getRequestId());
        }

        /*String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting ";*/
        return geofenceTransitionString + TextUtils.join(", ", triggeringGeofencesList);
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Inside the GeoFence";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Outside the GeoFence";
            default:
                return "Unknown";
        }
    }

    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

    // Create notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        Log.d("Status", "Inside createNotification");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "101");
        notificationBuilder
                .setSmallIcon(R.drawable.baseline_notifications_active_black_18dp)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }

    private void sendNotification(String msg) {
        Log.d("Status", "sendNotification: " + msg);

        // Intent to start the main Activity
        Intent notificationIntent = MainActivity.makeNotificationIntent(
                getApplicationContext(), msg);

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
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent));

    }

    private void createMyNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Sadda Haq";
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
