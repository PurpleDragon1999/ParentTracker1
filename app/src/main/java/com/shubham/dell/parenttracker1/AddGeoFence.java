package com.shubham.dell.parenttracker1;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddGeoFence extends AppCompatActivity implements OnMapReadyCallback, ResultCallback<Status>, GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    SeekBar range;
    private DatabaseReference mDatabaseReference ,mDatabaseReference2, refDelete;
    CurrentUser mCurrentUser = new CurrentUser();
    private GoogleMap mGoogleMap;
    GoogleApiClient googleApiClient;
    Marker mMarker;
    int hh;
    GeofencingRequest geoRequest;
    private GeofencingClient mGeofencingClient;
    private Marker geoFenceMarker;
    Circle geofencelimits;
    public static final int MY_PERMISSIONS = 1;
    CardView edit, display;
    TextView g1, g2, g3;
    ImageButton d1, d2, d3;
    ImageButton add;
    ImageView ad, min;

    TextView geoName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geo_fence);

        mGeofencingClient = LocationServices.getGeofencingClient(this);
        //mTextView = findViewById(R.id.rad);
        //edit = findViewById(R.id.setter);
        display = findViewById(R.id.displayer);
        g1 = findViewById(R.id.no1);
        g2 = findViewById(R.id.no2);
        g3 = findViewById(R.id.no3);
        d1 = findViewById(R.id.del1);
        d2 = findViewById(R.id.del2);
        d3 = findViewById(R.id.del3);
        ad = findViewById(R.id.p);
        min = findViewById(R.id.m);
        ad.setVisibility(View.INVISIBLE);
        min.setVisibility(View.INVISIBLE);
        add = findViewById(R.id.addGeoButton);
        geoName = findViewById(R.id.geoName);
        geoName.setVisibility(View.INVISIBLE);

        refDelete = FirebaseDatabase.getInstance().getReference("STUDENTS")
                .child(mCurrentUser.getUsername())
                .child("GeoFence");

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS")
                .child(mCurrentUser.getUsername())
                .child("GeoFence_Status");
        //For Checking GeoFence Status
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int stat = Integer.parseInt(dataSnapshot.getValue(String.class));
                if(stat == 3)
                {
                    add.setEnabled(false);
                }
                if(stat <= 3)
                {
                    DisplayGeo(stat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(AddGeoFence.this);

        range = findViewById(R.id.range);
        range.setVisibility(View.INVISIBLE);
        range.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(geoFenceMarker != null) {
                    Toast.makeText(AddGeoFence.this, "Radius " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                    startGeofencing(seekBar.getProgress());
                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Status", "Button Clicked "+add.getDrawable().getConstantState());
                mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS")
                        .child(mCurrentUser.getUsername());

                if(add.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.plus2).getConstantState()))
                {
                    //Log.d("Status", "Plus2 in bg");
                    range.setVisibility(View.VISIBLE);
                    add.setImageResource(R.drawable.tick);
                    geoName.setVisibility(View.VISIBLE);
                    ad.setVisibility(View.VISIBLE);
                    min.setVisibility(View.VISIBLE);
                    //Log.d("Status", "Bg set successfull");
                    mGoogleMap.setOnMapClickListener(AddGeoFence.this);
                }

                else if(add.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.tick).getConstantState()))
                {
                    Log.d("Status", "TextField Null");
                    if(!geoName.getText().toString().equals("") && geoFenceMarker != null) //Also to check for invalid names
                    {
                        Log.d("Status", "TextField Not Null");
                        checker(geoName.getText().toString());
                    }
                    else if(geoName.getText().toString().equals(""))
                    {
                        Log.d("Status", "TextField Null");
                        dialog("Please Enter The Required Field");
                    }
                    else if(geoFenceMarker == null)
                    {
                        Log.d("Status", "No GeoFence Added");
                        dialog("Please add a geoFence");
                    }
                }
            }
        });
        d1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = g1.getText().toString();
                refDelete.child(t).removeValue();
                mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS").child(mCurrentUser.getUsername())
                        .child("GeoFence");
                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int count = 0;
                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                        {
                            count = count + 1;
                        }
                        Log.d("Status", "Count "+count);
                        DatabaseReference v = FirebaseDatabase.getInstance().getReference("STUDENTS")
                                .child(mCurrentUser.getUsername())
                                .child("GeoFence_Status");
                        v.setValue(String.valueOf(count));
                        DisplayGeo(count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        d2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = g2.getText().toString();
                refDelete.child(t).removeValue();
                mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS").child(mCurrentUser.getUsername())
                        .child("GeoFence");
                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int count = 0;
                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                        {
                            count = count + 1;
                        }
                        Log.d("Status", "Count "+count);
                        DatabaseReference v = FirebaseDatabase.getInstance().getReference("STUDENTS")
                                .child(mCurrentUser.getUsername())
                                .child("GeoFence_Status");
                        v.setValue(String.valueOf(count));
                        DisplayGeo(count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        d3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = g3.getText().toString();
                refDelete.child(t).removeValue();
                mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS").child(mCurrentUser.getUsername())
                        .child("GeoFence");
                mDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int count = 0;
                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                        {
                            count = count + 1;
                        }
                        Log.d("Status", "Count "+count);
                        DatabaseReference v = FirebaseDatabase.getInstance().getReference("STUDENTS")
                                .child(mCurrentUser.getUsername())
                                .child("GeoFence_Status");
                        v.setValue(String.valueOf(count));
                        DisplayGeo(count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        createGoogleApi();
    }

    public void checker(String name)
    {
        Log.d("Status", "inside checker");
        final String s = name;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS")
                .child(mCurrentUser.getUsername());

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("GeoFence"))
                {
                    Log.d("Status", "Child Exists");
                    mDatabaseReference.removeEventListener(this);
                    mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("STUDENTS")
                            .child(mCurrentUser.getUsername())
                            .child("GeoFence");
                    mDatabaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean x = true;
                            for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                            {
                                Log.d("Status", "Key is "+dataSnapshot1.getKey());
                                if(dataSnapshot1.getKey().equals(s))
                                {
                                    Log.d("Status", "Name Already Exists");
                                    dialog("Name Already Exists");
                                    x = false;
                                }
                                else if(!dataSnapshot1.getKey().toString().equals(s))
                                {
                                    Log.d("Status", "Name does not exists");
                                    //updater();
                                }
                            }
                            if(x)
                                updater();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    Log.d("Status", "Child not exists");
                    updater();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updater()
    {
        String key = geoName.getText().toString();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS")
                .child(mCurrentUser.getUsername())
                .child("GeoFence")
                .child(key);

        Map<String, Object> updte = new HashMap<>();
        updte.put("LAT", geoFenceMarker.getPosition().latitude);
        updte.put("Lon", geoFenceMarker.getPosition().longitude);
        updte.put("Radius", range.getProgress());
        if(mDatabaseReference.updateChildren(updte) != null)
        {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS")
                    .child(mCurrentUser.getUsername())
                    .child("GeoFence_Status");
            hh = hh + 1;
            mDatabaseReference.setValue(String.valueOf(hh));
            Log.d("Status", "Status Updated");
            Toast.makeText(AddGeoFence.this, "Saved", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(getIntent());
            add.setImageResource(R.drawable.plus2);
            range.setVisibility(View.INVISIBLE);
        }
    }

    public void dialog(String msg)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(msg).setTitle("Warning");

        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alert = dialog.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(AddGeoFence.this, MainActivity.class);
        startActivity(i);
    }

    public void DisplayGeo(int status) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS")
                .child(mCurrentUser.getUsername())
                .child("GeoFence");
        hh = status;
        Log.d("Status", "value added in hh");
        if(status == 0)
        {
            g1.setVisibility(View.GONE);
            g2.setVisibility(View.GONE);
            g3.setVisibility(View.GONE);
            d1.setVisibility(View.GONE);
            d2.setVisibility(View.GONE);
            d3.setVisibility(View.GONE);
        }
        else if(status == 1)
        {
            mDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        g1.setText(dataSnapshot1.getKey().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            g2.setVisibility(View.GONE);
            g3.setVisibility(View.GONE);
            d2.setVisibility(View.GONE);
            d3.setVisibility(View.GONE);
        }
        else if(status == 2)
        {
            mDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count = 0;
                    for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        count = count + 1;
                        if(count == 1)
                            g1.setText(dataSnapshot1.getKey().toString());
                        if(count == 2)
                            g2.setText(dataSnapshot1.getKey().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            g3.setVisibility(View.GONE);
            d3.setVisibility(View.GONE);
        }
        else if(status == 3)
        {
            geoName.setVisibility(View.INVISIBLE);
            mDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count = 0;
                    for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        count = count + 1;
                        if(count == 1)
                            g1.setText(dataSnapshot1.getKey().toString());
                        if(count == 2)
                            g2.setText(dataSnapshot1.getKey().toString());
                        if(count == 3)
                            g3.setText(dataSnapshot1.getKey().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        Log.d("Status", "In onMapReady");
        //mGoogleMap.setOnMapClickListener(this);
        mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("STUDENTS")
                .child(mCurrentUser.getUsername());
        mDatabaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hh = dataSnapshot.child("stop_name").getValue(String.class);
                Log.d("Status", "jfjfbjfv "+hh);

                mDatabaseReference = FirebaseDatabase.getInstance().getReference("BUSROUTE")
                        .child(mCurrentUser.getRoute())
                        .child("Stops")
                        .child(hh);
                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Double lat = Double.parseDouble(dataSnapshot.child("Lat").getValue(String.class));
                        Double lon = Double.parseDouble(dataSnapshot.child("Long").getValue(String.class));
                        LatLng stop = new LatLng(lat,lon);
                        Log.d("Status", stop.latitude +" "+stop.longitude);
                        if(stop != null && mGoogleMap!=null) {
                            mGoogleMap.addMarker(new MarkerOptions().position(stop).title("Stop").
                                    icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.mapmarker)));
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(stop));
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(stop.latitude, stop.longitude))
                                    .zoom(17)
                                    .build();
                            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            Log.d("Status", "Added "+stop.latitude+" "+stop.longitude);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        markerForGeoFence(latLng);
        Log.d("Status", "onMapClick------------> "+range.getProgress());
        startGeofencing(100);
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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void markerForGeoFence(LatLng latLng)
    {
        Log.d("Status", "markerForGeofence("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if ( mGoogleMap!=null ) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = mGoogleMap.addMarker(markerOptions);
        }
    }

    private void drawGeoFence(float radius) {
        Log.d("Status", "Drawing GeoFence");
        //Log.d("Status", "Drawing GeoFence "+geoFenceMarker.getPosition().latitude+ " "+geoFenceMarker.getPosition().longitude);
        if(geofencelimits!=null)
        {
            Log.d("Status", "geoFenceLimits is null");
            geofencelimits.remove();
        }

        if(geoFenceMarker != null)
        {
            Log.d("Status", "GeoFenceMarker not null");
            CircleOptions circleOptions = new CircleOptions()
                    .center(geoFenceMarker.getPosition())
                    .strokeColor(Color.argb(50,70,70,70))
                    .fillColor(Color.argb(100,150,150,150))
                    .radius(radius);
            geofencelimits = mGoogleMap.addCircle(circleOptions);
        }
        Log.d("Status", "geoFenceLimits added");
    }

    private void addGeoFence(GeofencingRequest georequest, float radius) {
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
        drawGeoFence(radius);
        /*mGeofencingClient.addGeofences(georequest, createGeoFencingPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Status", "addGeoFence Success");
                        drawGeoFence();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Status", "addGeoFence Failure");
                    }
                });*/
    }

    private Geofence createGeofence(LatLng current_stop, float v) {
        Log.d("Status", "Inside createGeoFence "+String.valueOf(current_stop.latitude)+" "+String.valueOf(current_stop.longitude));
        return new Geofence.Builder()
                .setRequestId("MY FENCE")
                .setCircularRegion(current_stop.latitude, current_stop.longitude, v)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private GeofencingRequest createGeoRequest(Geofence geofence) {
        Log.d("Status", "Inside createGeoRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private void startGeofencing(float radius) {
        Log.d("Status", "Inside startGeoFencing");
        Geofence geofence = createGeofence(geoFenceMarker.getPosition(), radius);
        geoRequest = createGeoRequest(geofence);
        addGeoFence(geoRequest, radius);
    }
}
