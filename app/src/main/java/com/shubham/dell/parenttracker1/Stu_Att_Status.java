package com.shubham.dell.parenttracker1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.paperdb.Paper;

public class Stu_Att_Status extends AppCompatActivity {
    DatabaseReference mDatabaseReference;
    TextView status2,time, cdate;
    Button cal_date;
    int mYear, mMonth, mDay;
    TextView stat, tym, stat2, tym2, stat3, tym3, stat4, tym4;

    CurrentUser mCurrentUser=new CurrentUser();
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent=new Intent(Stu_Att_Status.this,MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_attendance:
                    return true;
                case R.id.navigation_profile:
                    Intent intent2 = new Intent(Stu_Att_Status.this , Student_Profile.class);
                    startActivity(intent2);
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu__att__status);
        Paper.init(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_attendance);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        stat = findViewById(R.id.stu_status);
        tym = findViewById(R.id.att_time);
        stat2 = findViewById(R.id.stu_status2);
        tym2 = findViewById(R.id.att_time2);
        stat3 = findViewById(R.id.stu_status3);
        tym3 = findViewById(R.id.att_time3);
        stat4 = findViewById(R.id.stu_status4);
        tym4 = findViewById(R.id.att_time4);

        status2 = findViewById(R.id.stu_status);
        time = findViewById(R.id.att_time);
        cal_date = findViewById(R.id.button);
        cdate = findViewById(R.id.current_date);

        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Calendar cal=Calendar.getInstance();
        final String temp_date=sdf.format(cal.getTime());
        final String date=temp_date.substring(0,10);
        cdate.setText(date);
        Log.d("Status","Date "+date);

        cal_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                final DatePickerDialog datePickerDialog = new DatePickerDialog(Stu_Att_Status.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                stat.setText(R.string.empty);
                                stat2.setText(R.string.empty);
                                stat3.setText(R.string.empty);
                                stat4.setText(R.string.empty);
                                tym.setText(R.string.empty);
                                tym2.setText(R.string.empty);
                                tym3.setText(R.string.empty);
                                tym4.setText(R.string.empty);

                                if(monthOfYear+1 >=10)
                                    cdate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                else
                                    cdate.setText(dayOfMonth + "-" + "0" + (monthOfYear + 1) + "-" + year);
                                AttStatus();
                            }
                        }, mYear, mMonth, mDay);
                c.add(Calendar.DATE, 0);
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
                c.add(Calendar.DATE, -15);
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                datePickerDialog.show();
            }
        });
        AttStatus();
    }

    public void AttStatus()
    {
        Log.d("Status","gfxgfgcgvhbjnbhvgcfxcghbjnbhgvcfvgbh "+cdate.getText().toString());
        try {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("Attendance").child(mCurrentUser.getRoute()).child(cdate.getText().toString()).child(mCurrentUser.getUsername());
            mDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                    String tmp_M_in, tmp_M_out, tmp_E_in, tmp_E_out;

                    if(dataSnapshot1.hasChild("MorningIn")) {
                        tmp_M_in = dataSnapshot1.child("MorningIn").getValue(String.class);
                        stat.setText(tmp_M_in.substring(0,7));
                        tym.setText(tmp_M_in.substring(7,15));
                    }

                    if(dataSnapshot1.hasChild("MorningOut")) {
                        tmp_M_out = dataSnapshot1.child("MorningOut").getValue(String.class);
                        stat2.setText(tmp_M_out.substring(0,7));
                        tym2.setText(tmp_M_out.substring(7,15));
                    }

                    if(dataSnapshot1.hasChild("EveningIn")) {
                        tmp_E_in = dataSnapshot1.child("EveningIn").getValue(String.class);
                        stat3.setText(tmp_E_in.substring(0,7));
                        tym3.setText(tmp_E_in.substring(7,15));
                    }

                    if(dataSnapshot1.hasChild("EveningOut")) {
                        tmp_E_out = dataSnapshot1.child("EveningOut").getValue(String.class);
                        stat4.setText(tmp_E_out.substring(0,7));
                        tym4.setText(tmp_E_out.substring(7,15));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch(Exception e)
        {
            Toast.makeText(Stu_Att_Status.this, e.toString(), Toast.LENGTH_SHORT);
        }
    }
}
