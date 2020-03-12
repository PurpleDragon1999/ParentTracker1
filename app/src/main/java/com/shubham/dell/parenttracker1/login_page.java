package com.shubham.dell.parenttracker1;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import io.paperdb.Paper;

public class login_page extends AppCompatActivity {
    private static final String Tag = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public static String route, roll_number, bus_num;
    public EditText user, pass;
    public static String val;
    private DatabaseReference mDatabaseReference, mDatabaseReference2;
    private String token;
    CurrentUser mCurrentUser=new CurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        Log.d(Tag, "onCreate");
        Paper.init(this);
        if(mCurrentUser.getUsername()!=null)
        {
            Log.d("Status", "mCurrentUser not null");
            Intent intent=new Intent(login_page.this, MainActivity.class);
            finish();
            startActivity(intent);
        }
        FirebaseApp.initializeApp(this);
        user = findViewById(R.id.username);
        pass = findViewById(R.id.password);

        Log.d(Tag, "init is called " + user.getText().toString() + " " + pass.getText().toString());
        final Button lgn = findViewById(R.id.login);

        lgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseMessaging.getInstance().setAutoInitEnabled(true);

                Log.d(Tag, "else me aa gya "+user.getText().toString()+" "+pass.getText().toString());

                mDatabaseReference=FirebaseDatabase.getInstance().getReference("STUDENTS");
                //val = user.getText().toString();
                mDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                        {
                            String rollno=dataSnapshot1.child("roll_number").getValue(String.class);
                            String passwd=dataSnapshot1.child("u_password").getValue(String.class);
                            String log=dataSnapshot1.child("log").getValue(String.class);
                            if(rollno.equals(user.getText().toString()) && passwd.equals(pass.getText().toString()))
                            {


                                    Log.d("Status", "Succesful");
                                    route = dataSnapshot1.child("route").getValue(String.class);
                                    roll_number = user.getText().toString();
                                    bus_num = dataSnapshot1.child("bus").getValue(String.class);
                                    mCurrentUser.setUsername(roll_number);
                                    mCurrentUser.setPassword(passwd);
                                    mCurrentUser.setRoute(route);
                                    mCurrentUser.setNoti(false);
                                    mDatabaseReference2 = FirebaseDatabase.getInstance().getReference("STUDENTS")
                                            .child(mCurrentUser.getUsername())
                                            .child("log");
                                    mDatabaseReference2.setValue("1");
                                    //TOKEN generation

                                    Intent intent = new Intent(login_page.this, MainActivity.class);


                                    startActivity(intent);
                                    finish();


                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

}
