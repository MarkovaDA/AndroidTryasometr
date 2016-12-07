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
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));
        Date date = new Date();
        try {
            date = dateFormat.parse(dateFormat.format(date));
        } catch (ParseException e) {
            System.out.println("PARSE ERROR  EXCEPTION IN DATETIMESERVICE");
            e.printStackTrace();
        }
        return date;
    }

    public static Date getDateAndTime(Location location){
        Date d = new Date(location.getTime());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));
        Date date = null;
        try {
            date = dateFormat.parse(dateFormat.format(d));
        } catch (ParseException e) {
            System.out.println("PARSE ERROR  EXCEPTION IN DATETIMESERVICE");
            e.printStackTrace();
        }
        return date;
    }
}
