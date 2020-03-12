package com.shubham.dell.parenttracker1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

import io.paperdb.Paper;

public class Student_Profile extends AppCompatActivity {


    private Uri filePath;
    private ImageView imageView;
    private final int PICK_IMAGE_REQUEST = 71;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference databaseReference, mDatabaseReference;
    FirebaseStorage storage;
    StorageReference storageReference;
    Button logout_button, addGeo;
    Switch notiSwitch;
    TextView stu_name,roll_no1,contact_no1,bus_no1;
    CurrentUser mCurrentUser=new CurrentUser();
    private String token;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent=new Intent(Student_Profile.this,MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_attendance:
                  Intent intent1 = new Intent(Student_Profile.this , Stu_Att_Status.class);
                    startActivity(intent1);
                    return true;
                case R.id.navigation_profile:
                    return true;
            }
            return false;
        }
    };
    private DatabaseReference mDatabaseReference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student__profile);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_profile);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Paper.init(this);
        imageView = (ImageView) findViewById(R.id.stu_profileImage);
        notiSwitch=findViewById(R.id.noti);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        logout_button = findViewById(R.id.logout);
        stu_name = findViewById(R.id.name);
        roll_no1 = findViewById(R.id.rollno);
        contact_no1 = findViewById(R.id.conact_number);
        bus_no1 = findViewById(R.id.bus_number);
        addGeo = findViewById(R.id.addGeo);
        if(mCurrentUser.getNoti()==true)
        {
            notiSwitch.setChecked(true);
        }
        StuDetails();

        notiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true)
                {
                    FirebaseMessaging.getInstance().subscribeToTopic(mCurrentUser.getRoute())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(Student_Profile.this,"Subscribed to topic",Toast.LENGTH_SHORT).show();
                                        mCurrentUser.setNoti(true);
                                    }
                                }
                            });
                }
                else if(isChecked == false)
                {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(mCurrentUser.getRoute())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(Student_Profile.this,"Unsubscribed to topic",Toast.LENGTH_SHORT).show();
                                        mCurrentUser.setNoti(false);
                                    }
                                }
                            });
                }
            }
        });
        notiSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().subscribeToTopic(mCurrentUser.getRoute())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(Student_Profile.this,"Subscribed to topic",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().setAutoInitEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                           // SendTokenToServer s=new SendTokenToServer("ddd");
                            Log.d("Status","thread under");
                        }
                        catch (IOException e){
                            Log.d("Status",""+e);
                        }
                    }
                }).start();
                Log.d("DeviceTokenBhadwa", "1");

                mCurrentUser.remove();
                Log.d("DeviceTokenBhadwa", "2");
                stopService(new Intent(Student_Profile.this, GeoFenceTransitionService.class));
                stopService(new Intent(Student_Profile.this, FirebaseMessagingService.class));
                Log.d("DeviceTokenBhadwa", "3");
                Intent intent=new Intent(Student_Profile.this,login_page.class);
                finish();
                startActivity(intent);



                Log.d("DeviceTokenBhadwa", "4");

            }
        });

        addGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Student_Profile.this, AddGeoFence.class);
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                StuProfileImageUpload();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    public void SelectImage(View view)
    {Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
   public void StuProfileImageUpload()
    {
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            UploadTask uploadTask=ref.putFile(filePath);
            Task<Uri> uriTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri=task.getResult();
                        Toast.makeText(Student_Profile.this,"Uploaded",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                       DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("STUDENTS").child(mCurrentUser.getUsername());
                        databaseReference.child("img").setValue(downloadUri.toString());
                    }
                    else
                    {
                        Toast.makeText(Student_Profile.this,"Image Uploading failed",Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }
    public void StuDetails() {
        databaseReference = FirebaseDatabase.getInstance().getReference("STUDENTS").child(mCurrentUser.getUsername());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String rollno = dataSnapshot.child("roll_number").getValue(String.class);
                String contact_no = dataSnapshot.child("phone1").getValue(String.class);
                String bus_no = dataSnapshot.child("bus").getValue(String.class);

                Picasso.get().load(dataSnapshot.child("img").getValue(String.class)).into(imageView);

                stu_name.setText(name);
                roll_no1.setText(rollno);
                contact_no1.setText(contact_no);
                bus_no1.setText(bus_no);
            }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
