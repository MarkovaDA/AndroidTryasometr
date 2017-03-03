package ru.markova.darya.geolocation.service;

import android.location.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * сервис для получения текущей даты и времени
 */
public class DateTimeService {

    public static Date getCurrentDateAndTime(){
        return new Date();
    }

    public static String getCurrentDateAndTimeString(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static Date getDateAndTime(Location location){
        return new Date(location.getTime());
    }
}
