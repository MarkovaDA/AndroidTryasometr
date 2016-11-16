package ru.markova.darya.geolocation;

import android.location.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/*класс, реализующий функционал отправки координат на сервер*/
public class SendGeoLocation {

    public static String sendLocation(Location location, String deviceIMEI)  {
        URL geoSendUrl = null;
        try {
            geoSendUrl = new URL("https://vps1.vistar.su/tryasometr/geo");
        }
        catch (MalformedURLException me) {
            return "MalformedURLException";
        }
        Map<String,String> params = new LinkedHashMap<>();
        params.put("longitude", Double.toString(location.getLongitude()));
        params.put("latitude", Double.toString(location.getLatitude()));
        params.put("imei", deviceIMEI);
        //вместе с координатами будем отсылать id-утсройства и положение ускорения
        StringBuilder postData = new StringBuilder();
        for(Map.Entry<String, String> param: params.entrySet()) {
            try {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            catch(UnsupportedEncodingException ue){
                return "UnsupportedEncodingException in sending data";
            }
        }
        try {
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            HttpURLConnection conn = (HttpURLConnection)geoSendUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            //считываем ответ от сервера
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            String response = sb.toString();//ответ от сервера
            //состояние отправки
            return response;
        }
        catch (IOException ie) {
            return "IOException in sending data";
        }
    }

}
