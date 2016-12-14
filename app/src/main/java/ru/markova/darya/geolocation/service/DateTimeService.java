package ru.markova.darya.geolocation.service;

import android.location.Location;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by darya on 04.12.16.
 */
//возвращает текущую дату и время
public class DateTimeService {

    public static Date getCurrentDateAndTime(){
        /*DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(new Date());*/

        return new Date();
    }

    public static Date getDateAndTime(Location location){
        /*DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(new Date(location.getTime()));*/
        return new Date(location.getTime());
    }
}
