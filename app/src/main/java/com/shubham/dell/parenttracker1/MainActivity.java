package com.shubham.dell.parenttracker1;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;

import io.paperdb.Paper;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ResultCallback<Status>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference, mDatabaseReference2;
    public String route_value;
    private GeofencingClient mGeofencingClient;
    public String stu_stop;
    Marker marker;
    GeofencingRequest geoRequest;
    LatLng current_stop;
    Circle geofencelimits;
    PendingIntent geoFencePendingIntent;
    GoogleApiClient googleApiClient;
    private static final int MY_PERMISSIONS = 1;
    public static Marker geoFenceMarker;
    public static LatLng bb;
    String date;
    int r;
    private String token;
    private static final String Tag = "MainActivity";

    public TextView bus_num, route, driver_name, driver_phone;
    CurrentUser mCurrentUser = new CurrentUser();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_attendance:
                    Intent intent = new Intent(MainActivity.this, Stu_Att_Status.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_profile:
                    Intent intent2 = new Intent(MainActivity.this, Student_Profile.class);
                    startActivity(intent2);
                    return true;
            }
            return false;
        }
    };

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MainActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Paper.init(this);
        bus_num = findViewById(R.id.bus_no);
        route = findViewById(R.id.route);
        driver_name = findViewById(R.id.driver_name);
        driver_phone = findViewById(R.id.driver_phone);

        //Getting the date
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Calendar cal=Calendar.getInstance();
        final String temp_date=sdf.format(cal.getTime());
        date=temp_date.substring(0,10);

        startService(new Intent(this, GeoFenceTransitionService.class));
        startService(new Intent(this, FirebaseMessagingService.class));
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance();

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("BUS_ROUTE").child(mCurrentUser.getRoute());
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("status").getValue(String.class);
                if (status.equals("stop")) {
                    Intent i = new Intent(MainActivity.this, BusStatus.class);
                    mapFragment.startActivity(i);
                } else {

                    mapFragment.getMapAsync(MainActivity.this);
                    busLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("BUSROUTE").child(mCurrentUser.getRoute());
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String stat = dataSnapshot.child("Master_status").getValue(String.class);
                String bus_change = dataSnapshot.child("bus_change_status").getValue(String.class);
                Log.d("Status", "Master Status " + stat);
                DriverDetails(stat, bus_change);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getThikana();

        Log.d("Status", "qwertyuiopojhgfdfghg "+stu_stop);

        Display_Geo();
        create_Marker();
        createGoogleApi();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Status", "onrestart");
        Display_Geo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Status", "onResume");
        Display_Geo();
    }

    public void Display_Geo(){
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS")
                .child(mCurrentUser.getUsername());
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("GeoFence"))
                {
                    //dataSnapshot.child("GeoFence");
                    for(DataSnapshot dataSnapshot1: dataSnapshot.child("GeoFence").getChildren())
                    {
                        LatLng l = new LatLng(dataSnapshot1.child("LAT").getValue(Double.class),
                                dataSnapshot1.child("Lon").getValue(Double.class));
                        int radius = dataSnapshot1.child("Radius").getValue(Integer.class);
                        //startGeofencing(radius, l);
                        //Log.d("Status", "????????????????????????????????");
                        drawGeoFence(radius, l);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void getThikana() {
        //String hh;
        mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("STUDENTS").child(mCurrentUser.getUsername()).child("stop_name");
        mDatabaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hh = dataSnapshot.getValue(String.class);
                Log.d("Status", "kjnkdsjsduh "+hh);
                getLatLon(hh);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d("Status", "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        Display_Geo();
    }

    public void getLatLon(String stop) {
        Log.d("Status", "getLatLon called "+stop);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("BUSROUTE")
                .child(mCurrentUser.getRoute())
                .child("Stops")
                .child(stop);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double stop_lat = Double.parseDouble(dataSnapshot.child("Lat").getValue(String.class));
                Double stop_long = Double.parseDouble(dataSnapshot.child("Long").getValue(String.class));
                LatLng stop = new LatLng(stop_lat, stop_long);
                //makingLatLngGlobal(stop);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*removeGeoFence();*/
    }

    public void DriverDetails(String master_driver, String bus_change_status) {

        String roll_no = mCurrentUser.getUsername();
        String route_no = mCurrentUser.getRoute();
        String tmp_route_no = "  " + route_no;
        route.setText(tmp_route_no);

        String timeZone = MorningorEvening();

        mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("BUSROUTE").child(route_no).child("Bus_number");
        mDatabaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String tmp_bus_no = dataSnapshot.getValue(String.class);
                String bus_no = "  " + tmp_bus_no;
                bus_num.setText(bus_no);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.d("Status", "Had come 1");

        if ((master_driver.equals("0") && bus_change_status.equals("unchanged"))) {
            Log.d("Status", "Had come 2");
            if (roll_no != null && route_no != null) {
                mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("BUSROUTE").child(route_no);
                mDatabaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String tmp_phone = dataSnapshot.child("Driver_Phone").getValue(String.class);
                        String phone = "  " + tmp_phone;

                        String tmp_name = dataSnapshot.child("Driver_name").getValue(String.class);
                        String name = "  " + tmp_name;

                        driver_phone.setText(phone);
                        driver_name.setText(name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

        else if (bus_change_status.equals("unchanged") && master_driver.equals("1")) {
            mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("BUSROUTE").child(route_no).child("Master_drivers").child(date).child(timeZone);
            mDatabaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String name = "  " + dataSnapshot1.getKey();
                        String phone = "  " + dataSnapshot1.getValue(String.class);

                        driver_name.setText(name);
                        driver_phone.setText(phone);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        else if(bus_change_status.equals("changed") && master_driver.equals("0"))
        {
            mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("BUSROUTE").child(route_no).child("Bus_Change").child(date).child(timeZone);
            mDatabaseReference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String bus_no = "  " + dataSnapshot.child("Bus_Number").getValue(String.class);
                    String dr_name = "  " + dataSnapshot.child("Driver_Name").getValue(String.class);
                    String dr_contact = "  " + dataSnapshot.child("Driver_Contact").getValue(String.class);

                    bus_num.setText(bus_no);
                    driver_name.setText(dr_name);
                    driver_phone.setText(dr_contact);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        else if(bus_change_status.equals("changed") && master_driver.equals("1"))
        {
            //Bus number
            mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("BUSROUTE")
                    .child(route_no)
                    .child("Bus_Change")
                    .child(date)
                    .child(timeZone);
            mDatabaseReference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String bus_no = "  " + dataSnapshot.child("Bus_Number").getValue(String.class);
                    bus_num.setText(bus_no);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("BUSROUTE")
                    .child(route_no)
                    .child("Master_drivers")
                    .child(date)
                    .child(timeZone);
            mDatabaseReference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        String name = "  " + dataSnapshot1.getKey();
                        String phone = "  " + dataSnapshot1.getValue(String.class);

                        driver_name.setText(name);
                        driver_phone.setText(phone);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Display_Geo();
        //mMap.setOnMapClickListener(this);
        //mMap.setOnMarkerClickListener(this);
    }

    private void startGeofencing(int radius, LatLng latLng) {
        bb = latLng;
        r = radius;
        Log.d("Status", "Inside startGeoFencing");
        Geofence geofence = new Geofence.Builder()
                .setRequestId("MY FENCE")
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        geoRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
        addGeoFence(geoRequest);
    }

    /*private Geofence createGeofence(LatLng current_stop, float v) {
        Log.d("Status", "Inside createGeoFence "+String.valueOf(current_stop.latitude)+" "+String.valueOf(current_stop.longitude));
        return new Geofence.Builder()
                .setRequestId("MY FENCE")
                .setCircularRegion(current_stop.latitude, current_stop.longitude, v)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }*/

    /*private GeofencingRequest createGeoRequest(Geofence geofence) {
        Log.d("Status", "Inside createGeoRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }*/

    private void addGeoFence(GeofencingRequest georequest) {
        Log.d("Status", "Inside addGeoFence");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS);
            Log.d("Status", "Got some error");
            return;
        }
        mGeofencingClient.addGeofences(georequest, createGeoFencingPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Status", "addGeoFence Success");
                        //saveGeoFence();
                        //drawGeoFence(r, bb);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Status", "addGeoFence Failure");
                    }
                });
    }



    private PendingIntent createGeoFencingPendingIntent() {
        Log.d("Status", "Inside createGeoFencingPendingIntent");
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        }
        Intent i = new Intent(this, GeoFenceTransitionService.class);
        Log.d("Status", "Returning intent");
        return PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Status", "Access Granted");
                } else {
                    Log.d("Status", "Acccess Denied");
                }
                return;
            }
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        if(status.isSuccess()) {
            Log.d("Status", "onResult() : Drawing GeoFence");
            //drawGeoFence();
        }
        else {
            Log.d("Status", "GeoFence Cancelled");
        }
    }

    private void drawGeoFence(int rad, LatLng bb) {
        Log.d("Status", "Drawing GeoFence");
        //Log.d("Status", "Drawing GeoFence "+geoFenceMarker.getPosition().latitude+ " "+geoFenceMarker.getPosition().longitude);
        /*if(geofencelimits!=null)
        {
            Log.d("Status", "geoFenceLimits is null");
            geofencelimits.remove();
        }*/

        if(mMap != null)
        {
            Log.d("Status", "GeoFenceMarker not null");
            CircleOptions circleOptions = new CircleOptions()
                    .center(bb)
                    .strokeColor(Color.argb(50,70,70,70))
                    .fillColor(Color.argb(100,150,150,150))
                    .radius(rad);
            geofencelimits = mMap.addCircle(circleOptions);
        }
        Log.d("Status", "-------------------------------geoFenceLimits added------------------------------");
    }

    public void busLocation() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS")
                .child(mCurrentUser.getUsername())
                .child("route");
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                route_value=dataSnapshot.getValue(String.class);
                Log.d("route","Value"+route_value);
                if (route_value!=null)
                {
                    mDatabaseReference=FirebaseDatabase.getInstance().getReference("BUS_ROUTE").child(route_value);
                    mDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Double lat=dataSnapshot.child("lat").getValue(Double.class);
                            Double lon=dataSnapshot.child("lon").getValue(Double.class);

                            Log.d("Location","Lat"+lat+"lon"+lon);
                            LatLng location = new LatLng(lat,lon);

                            if (marker != null)
                            {
                                marker.remove();
                            }
                            marker = mMap.addMarker(new MarkerOptions().position(location)
                                    .title("Bus")
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.mapmarker)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.latitude, location.longitude)).zoom(17).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            Toast.makeText(MainActivity.this,"error in getting location",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,"error in getting location1",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void create_Marker() {
        Log.d("Status","kdfd "+mCurrentUser.getUsername());
        String bachhe_rollnum = mCurrentUser.getUsername();
        mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("STUDENTS").child(bachhe_rollnum);
        mDatabaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hh = dataSnapshot.child("stop_name").getValue(String.class);
                Log.d("Status", "jfjfbjfv "+hh);
                addStopMarker(hh);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addStopMarker(String myStop) {
        Log.d("Status", "myStop "+myStop);
        mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("BUSROUTE").child(mCurrentUser.getRoute()).child("Stops").child(myStop);
        mDatabaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double stop_lat = Double.parseDouble(dataSnapshot.child("Lat").getValue(String.class));
                Double stop_long = Double.parseDouble(dataSnapshot.child("Long").getValue(String.class));
                LatLng stop = new LatLng(stop_lat, stop_long);
                if(stop != null && mMap!=null) {
                    mMap.addMarker(new MarkerOptions().position(stop).title("Stop"));
                    Log.d("Status", "Added "+stop_lat+" "+stop_long);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Status", "onConnected()");
        /*recoverGeoFence();*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Status", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*@Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }*/

    /*@Override
    public void onMapClick(LatLng latLng) {
        Log.d("Status", "Map Clicked");

        current_stop = latLng;
        centerPoint = latLng;
        markerForGeoFence(latLng);
        startGeofencing(200f);
    }*/

    /*public void markerForGeoFence(LatLng latLng)
    {
        Log.d("Status", "markerForGeofence("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if ( mMap!=null ) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = mMap.addMarker(markerOptions);
        }
    }*/


    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";
    // Saving GeoFence marker with prefs mng
    /*private void saveGeoFence() {
        Log.d("Status", "saveGeofence()");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong( KEY_GEOFENCE_LAT, Double.doubleToRawLongBits( current_stop.latitude ));
        editor.putLong( KEY_GEOFENCE_LON, Double.doubleToRawLongBits( current_stop.longitude ));
        editor.apply();
    }*/

    /*private void recoverGeoFence()
    {
        Log.d("Status", "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );

        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
            Log.d("Status", " "+lat+" "+lon);
            LatLng latLng = new LatLng( lat, lon );
            markerForGeoFence(latLng);
            drawGeoFence();
        }
    }*/

    /*public void removeGeoFence()
    {
        Log.d("Status", "removeGeofenceDraw()");
        if ( geoFenceMarker != null)
            geoFenceMarker.remove();
        if ( geofencelimits != null )
            geofencelimits.remove();
    }*/

    public String MorningorEvening()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
        Calendar c = Calendar.getInstance();
        String tmp = sdf.format(c.getTime());
        String tmp2 = tmp.split("\\s")[1];
        String tmp3 = tmp2.substring(0,2) + "." + tmp2.substring(3,5);
        final double time = Double.parseDouble(tmp3);

        if(time < 12)
            return "morning";
        else
            return "evening";
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && geoFenceMarker!= null)
        {
            counter = counter + 1000;
            if(counter < 15000)
                startGeofencing(counter);
            Toast.makeText(MainActivity.this, "Radius is "+counter, Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }*/
}
