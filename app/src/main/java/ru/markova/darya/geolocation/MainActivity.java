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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.markova.darya.geolocation.config.RetrofitBuilder;
import ru.markova.darya.geolocation.dto.InfoDTO;
import ru.markova.darya.geolocation.dto.ResponseEntityDTO;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import ru.markova.darya.geolocation.service.DateTimeService;
import ru.markova.darya.geolocation.service.LocalStorageService;
import ru.markova.darya.geolocation.service.RetrofitDataSendService;
import ru.markova.darya.geolocation.service.SendAccelerationToServerService;
import ru.markova.darya.geolocation.service.SendLocationToServerService;
import ru.markova.darya.geolocation.service.ShakeEventSensor;

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
    TextView tvUseFullInfoStatus;

    String   deviceIMEI;
    BroadcastReceiver broadcastReceiver;
    private LocationManager locationManager;
    private Location lastLocation;

    final String LOG_TAG = "MainActivity";
    private Intent serverLocationIntent;
    private Intent serverAccelerIntent;

    private SensorManager sensorManager;
    private ShakeEventSensor shakeEventListener;
    private LocalStorageService localStorageService;


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
        tvUseFullInfoStatus = (TextView)findViewById(R.id.tvUseFulInfoStatus);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        broadcastReceiver =  new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String status = intent.getExtras().get(MainActivity.STATUS_SENDING_PARAM).toString();
                txtStatusSending.setText(status);
            }
        };
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        shakeEventListener = new ShakeEventSensor();

        shakeEventListener.setOnShakeListener(
                new ShakeEventSensor.OnShakeListener() {

                    @Override
                    public void onShake() {
                        float[] accel = shakeEventListener.getAccellrations();
                        String info =  String.format("SHAKING: ax = %1$.4f, ay = %2$.4f, az=%3$.4f", accel[0],accel[1],accel[2]);
                        AccelerationTableEntity entity = new AccelerationTableEntity();
                        entity.setDeviceImei(deviceIMEI);
                        entity.setAccelX(accel[0]); entity.setAccelY(accel[1]);entity.setAccelZ(accel[2]);
                        entity.setDataTime(DateTimeService.getCurrentDateAndTime());
                        //локально сохраняем показание ускорений
                        localStorageService.insertAcceleration(entity);
                        System.out.println(info);
                    }
                }
        );
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = telephonyManager.getDeviceId();
        // создаем фильтр для BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(broadcastReceiver, intentFilter);
        serverLocationIntent = new Intent(this, SendLocationToServerService.class);
        serverAccelerIntent = new Intent(this, SendAccelerationToServerService.class);
        startService(serverLocationIntent);//запускаем службу отправки координат
        startService(serverAccelerIntent); //запускаем службу отправки ускорений
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("onResume(): приложение не имеет доступа к службе геолокации");
            return;
        }
        localStorageService = new LocalStorageService(MainActivity.this);
        //вешаем слушателя на два типа провайдеров определения местоположения - продумать параметры
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        sensorManager.registerListener(shakeEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //прослушиватель события смены местоположения
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location; //запоминаем данные о местоположении
            showLocation(location);
            GeoTableEntity entity = new GeoTableEntity();
            entity.setLat(location.getLatitude());
            entity.setLon(location.getLongitude());
            entity.setDataTime(DateTimeService.getDateAndTime(location));
            entity.setDeviceImei(deviceIMEI);
            localStorageService.insertLocation(entity);
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
        localStorageService.destroy(); //закрываем сессию для работы с базой
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "SENDING DATA UNTOUCHED....");
            return;
        }
        unregisterReceiver(broadcastReceiver);
        stopService(serverLocationIntent);
        stopService(serverAccelerIntent);
        //отключаем слушателя
        locationManager.removeUpdates(locationListener);
        shakeEventListener.setOnShakeListener(null);
    }
    //запись информации о яме
    public void onPitClick(View view){
        //сохранять в локальную базу данных
        InfoDTO info = new InfoDTO(DateTimeService.getCurrentDateAndTimeString(), "pit");
        Call<ResponseEntityDTO> call = RetrofitBuilder.getDataSendService().sendUseFulInfo(info);
        call.enqueue(new Callback<ResponseEntityDTO>() {
            @Override
            public void onResponse(Call<ResponseEntityDTO> call, Response<ResponseEntityDTO> response) {
                tvUseFullInfoStatus.setText("pit was saved");
            }

            @Override
            public void onFailure(Call<ResponseEntityDTO> call, Throwable t) {
                tvUseFullInfoStatus.setText("error saving pit");
            }
        });
    }

    //запись информации о неровности
    public void onRoughClick(View view){
        //тоже самое
        InfoDTO info = new InfoDTO(DateTimeService.getCurrentDateAndTimeString(), "rough");
        Call<ResponseEntityDTO> call = RetrofitBuilder.getDataSendService().sendUseFulInfo(info);
        call.enqueue(new Callback<ResponseEntityDTO>() {
            @Override
            public void onResponse(Call<ResponseEntityDTO> call, Response<ResponseEntityDTO> response) {
                tvUseFullInfoStatus.setText("rough was saved");
            }

            @Override
            public void onFailure(Call<ResponseEntityDTO> call, Throwable t) {
                tvUseFullInfoStatus.setText("error saving rough");
            }
        });
    }
}
