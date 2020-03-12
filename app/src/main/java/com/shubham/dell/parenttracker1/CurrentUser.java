package com.shubham.dell.parenttracker1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import io.paperdb.Paper;

public class CurrentUser {

    //getting username and password
    public static final String USER_KEY = "saveuser";
    public static final String PASS_KEY = "savepass";
    public static final String ROUTE = "saveroute";
    public static final String NOTI="saveNoti";

    public String getUsername() {
        username = Paper.book().read(USER_KEY);
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        Paper.book().write(USER_KEY, username);
    }
    public void setNoti(Boolean status)
    {
        this.status=status;
        Paper.book().write(NOTI,status);
    }
    public Boolean getNoti(){
        status=Paper.book().read(NOTI);
        return status;
    }

    public String getRoute() {
        route = Paper.book().read(ROUTE);
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
        Paper.book().write(ROUTE, route);

    }

    public String getPassword() {
        password = Paper.book().read(PASS_KEY);
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        Paper.book().write(PASS_KEY, password);
    }

    private String username, password, route;
    Boolean status;
    private Context context;
    SharedPreferences sharedPreferences;

    public CurrentUser() {

        //sharedPreferences = context.getSharedPreferences("CurrentInfo", Context.MODE_PRIVATE);
    }

    public void remove() {
        Paper.book().destroy();
        Log.d("Status","removed");
    }
}
