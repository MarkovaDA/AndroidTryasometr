package ru.markova.darya.geolocation;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import android.widget.Toast;

import java.util.Date;
import ru.markova.darya.geolocation.config.GreenDaoBuilder;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.DaoSession;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import ru.markova.darya.geolocation.service.DateTimeService;
import ru.markova.darya.geolocation.service.SendDataFromDBService;
import ru.markova.darya.geolocation.service.ShakeEventListener;

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

    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;

    String   deviceIMEI;
    BroadcastReceiver brSending;
    private LocationManager locationManager;
    private Location lastLocation; //последнее запомненное местоположение

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
                txtStatusSending.setText(status);
            }
        };

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE); //менеджер для прослушивания датчиков
        mSensorListener = new ShakeEventListener();

        //событие тряски
        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

            public void onShake() {
                System.out.println("SHAKING....");
                Toast.makeText(MainActivity.this, "Shake!", Toast.LENGTH_SHORT).show();
                saveAcceleration(mSensorListener.getAx(), mSensorListener.getAy(), mSensorListener.getAz());
            }
        });

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = telephonyManager.getDeviceId();

        //получаем объект сессии для работы с базой данных
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
        //зарегистрировали слушателя события тряски
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //сохраняем данные о местоположении
    private void saveLocation(Location location){
        GeoTableEntity entity = new GeoTableEntity();
        entity.setLon(location.getLongitude());
        entity.setLat(location.getLatitude());
        entity.setDataTime(DateTimeService.getDateAndTime(location));
        entity.setDeviceImei(deviceIMEI);
        daoSession.insert(entity);
    }
    //сохраняем в базу данных ускорения
    private void saveAcceleration(float ax, float ay, float az){
        AccelerationTableEntity entity = new AccelerationTableEntity();
        entity.setAccelX(ax);
        entity.setAccelY(ay);
        entity.setAccelZ(az);
        entity.setDeviceImei(deviceIMEI);
        entity.setDataTime(DateTimeService.getCurrentDateAndTime());
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location; //запоминаем данные о местоположении
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
        return String.format("Coordinates: lat = %1$.4f, lon = %2$.4f", location.getLatitude(), location.getLongitude());
    }
    //проверка доступности провайдеров
    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    //вызов меню с настройками
    public void onClickLocationSettings(View view) {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };

    //запуск активити
    public void onClickShowMap(View view){
        Intent showMapActivityIntent = new Intent(this, ShowMapActivity.class);
        if (lastLocation != null) {
            showMapActivityIntent.putExtra("lastLocationLon", lastLocation.getLongitude());
            showMapActivityIntent.putExtra("lastLocationLat", lastLocation.getLatitude());
        }
        startActivity(showMapActivityIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GreenDaoBuilder.closeSession();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "SENDING DATA UNTOUCHED....");
            return;
        }
        unregisterReceiver(brSending);
        stopService(serverIntent);
        //отключаем слушателя
        locationManager.removeUpdates(locationListener);
        //отписываем слушателя события тряски
        mSensorManager.unregisterListener(mSensorListener);
    }
    //отобразить информацию о перегрузочном ускорении
    public void showAccelerationInfo(View view){
        Toast toast = Toast.makeText(getApplicationContext(),mSensorListener.getTextInfo(), Toast.LENGTH_LONG);
        toast.show();
    }

}
