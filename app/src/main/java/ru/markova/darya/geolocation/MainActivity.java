package ru.markova.darya.geolocation;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.Date;
import ru.markova.darya.geolocation.config.GreenDaoBuilder;
import ru.markova.darya.geolocation.config.RetrofitBuilder;
import ru.markova.darya.geolocation.entity.DaoSession;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import ru.markova.darya.geolocation.service.RetrofitDataSendService;
import ru.markova.darya.geolocation.service.SendDataFromDBService;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AppCompatActivity {

    public final static String BROADCAST_ACTION = "ru.markova.darya.geolocation";
    public final static String STATUS_SENDING_PARAM = "sending_status";
    TextView tvEnabledGPS;
    TextView tvStatusGPS;
    TextView tvLocationGPS;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;
    TextView txtStatusSending;
    String   deviceIMEI;
    BroadcastReceiver brSending;
    private LocationManager locationManager;

    private static DaoSession daoSession;

    final String LOG_TAG = "MainActivity";

    private Intent serverIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS =   (TextView)  findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView)findViewById(R.id.tvLocationGPS);
        tvEnabledNet =  (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet =   (TextView)  findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView)findViewById(R.id.tvLocationNet);
        txtStatusSending = (TextView)findViewById(R.id.txtStatusSending);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        brSending =  new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String status = intent.getExtras().get(MainActivity.STATUS_SENDING_PARAM).toString();
                System.out.println(status);
                txtStatusSending.setText(status);
            }
        };
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = telephonyManager.getDeviceId();
        daoSession = GreenDaoBuilder.getDaoSession(MainActivity.this);
        // создаем фильтр для BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(brSending, intentFilter);
        serverIntent = new Intent(this, SendDataFromDBService.class);
        startService(serverIntent);//запускаем службу отправки координат
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("onResume(): приложение не имеет доступа к службе геолокации");
            return;
        }
        //вешаем слушателя на два типа провайдеров
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void saveLocation(Location location){
        GeoTableEntity entity = new GeoTableEntity();
        entity.setLon(location.getLongitude());
        entity.setLat(location.getLatitude());
        entity.setDataTime(new Date(location.getTime()));
        entity.setDeviceImei(deviceIMEI);
        daoSession.insert(entity);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
            saveLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("onProviderEnabled: приложение не имеет доступа к службе геолокации");
                return;
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    };
    //отображение координат
    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
    }
    private String formatLocation(Location location) {
        if (location == null)
            return "location is unknown";
        return String.format("Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT", location.getLatitude(), location.getLongitude(), new Date(location.getTime()));
    }
    //проверка доступности провайдеров
    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    //вызов меню
    public void onClickLocationSettings(View view) {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };

    //запуск активити
    public void onClickShowMap(View view){
        Intent showMapActivityIntent = new Intent(this, ShowMapActivity.class);
        startActivity(showMapActivityIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (checkAndSendHandler != null) {
            checkAndSendHandler.removeCallbacksAndMessages(null);
        }*/
        GreenDaoBuilder.closeSession();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "SENDING DATA UNTOUCHED....");
            return;
        }
        unregisterReceiver(brSending);
        stopService(serverIntent);
        //отключаем слушателя
        locationManager.removeUpdates(locationListener);
    }
}
