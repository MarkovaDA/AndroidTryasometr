package ru.markova.darya.geolocation.service;

import android.location.Location;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static Date getDateAndTime(Location location){
        //DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return new Date(location.getTime());
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(location.getTime());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return new Date(year,month,day,hour,minute,second);*/
    }
}
