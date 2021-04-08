package com.example.myfirebaseapp.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Class who manage user' time
 */
public class TimeUtils {

    public static int getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        Date currentLocalTime = calendar.getTime();

        @SuppressLint("SimpleDateFormat")
        DateFormat date = new SimpleDateFormat("HHmm");
        String localTime = date.format(currentLocalTime);

        return Integer.parseInt(localTime);
    }

}