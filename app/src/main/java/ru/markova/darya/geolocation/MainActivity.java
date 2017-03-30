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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Random;

import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import ru.markova.darya.geolocation.service.DateTimeService;
import ru.markova.darya.geolocation.service.LocalStorageService;
import ru.markova.darya.geolocation.service.AnalyzingPitService;
//import ru.markova.darya.geolocation.service.SendLocationToServerService;
import ru.markova.darya.geolocation.service.ShakeEventSensor;

public class MainActivity extends AppCompatActivity {

    public final static String BROADCAST_ACTION = "ru.markova.darya.geolocation";
    public final static String STATUS_SENDING_PARAM = "sending_status";
    public final static String AVERAGE_INTERVAL_VALUE = "avg_value";
    public final static String DRAW_GARMONICS_ACTION = "draw_garmonics";
    TextView tvEnabledGPS;
    TextView tvEnabledNet;
    TextView txtStatusSending;
    TextView txtValue;

    String   deviceIMEI;
    BroadcastReceiver broadcastReceiver;
    private LocationManager locationManager;

    //менеджер для обработки мгновенного смены координат
    private Location lastLocation;

    final String LOG_TAG = "MainActivity";
    //private Intent serverLocationIntent;
    private Intent serverAccelerIntent;

    private SensorManager sensorManager;
    private ShakeEventSensor shakeEventListener;
    private LocalStorageService localStorageService;

    private BarChart barChart;
    private ArrayList<BarEntry> barEntries;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvEnabledNet =  (TextView) findViewById(R.id.tvEnabledNet);
        txtStatusSending = (TextView)findViewById(R.id.txtStatusSending);
        txtValue = (TextView)findViewById(R.id.txtValue);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //обратная связь с сервером отсылки ускорений
        broadcastReceiver =  new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {

                Object status = intent.getExtras().get(MainActivity.STATUS_SENDING_PARAM);
                if (status !=null)
                    txtStatusSending.setText(status.toString());
                //среднее значение оценки
                Double avgValue =  intent.getDoubleExtra(MainActivity.AVERAGE_INTERVAL_VALUE, 5);
                if (avgValue !=null) {
                    String value = String.format("%.2f", avgValue);
                    txtValue.setText(value);
                }
                //гармоники спектра
                double[] garmonics =  intent.getDoubleArrayExtra(MainActivity.DRAW_GARMONICS_ACTION);
                if (garmonics.length > 0){
                    //прорисовка гармоник
                    drawGarmonics(garmonics);
                }
            }
        };
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        shakeEventListener = new ShakeEventSensor();
        //регистратор события тряски устройства
        shakeEventListener.setOnShakeListener(
                new ShakeEventSensor.OnShakeListener() {

                    @Override
                    public void onShake() {
                        float[] accel = shakeEventListener.getAccellrations();
                        String info =  String.format("SHAKING: ax = %1$.4f, ay = %2$.4f, az=%3$.4f", accel[0],accel[1],accel[2]);
                        AccelerationTableEntity entity = new AccelerationTableEntity();
                        entity.setDeviceImei(deviceIMEI);
                        entity.setAccelX(accel[0]); entity.setAccelY(accel[1]);entity.setAccelZ(accel[2]);
                        if (lastLocation !=null) {
                            entity.setLat(lastLocation.getLatitude());
                            entity.setLon(lastLocation.getLongitude());
                        }
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
        //serverLocationIntent = new Intent(this, SendLocationToServerService.class);
        serverAccelerIntent = new Intent(this, AnalyzingPitService.class);
        Random random = new Random();
        //инициализация гистограммы
        barChart = (BarChart)findViewById(R.id.bargraph);
        barEntries = new ArrayList<>();
        for(int i=1; i <=30; i++){
            barEntries.add(new BarEntry(i,  random.nextInt(50)));
        }
        barEntries.add(new BarEntry(1, 44f));
        BarDataSet barDataSet = new BarDataSet(barEntries, "garmonics");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);//подписи к столбцам убираем
        barChart.setData(barData);
        barChart.setDrawBorders(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.setDrawGridBackground(false);
        //startService(serverLocationIntent);//запускаем службу отправки координат
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
            /*GeoTableEntity entity = new GeoTableEntity();
            entity.setLat(location.getLatitude());
            entity.setLon(location.getLongitude());
            entity.setDataTime(DateTimeService.getDateAndTime(location));
            entity.setDeviceImei(deviceIMEI);
            localStorageService.insertLocation(entity);*/
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
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
           /*if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }*/
        }
    };

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
        //stopService(serverLocationIntent);
        stopService(serverAccelerIntent);
        //отключаем слушателя
        locationManager.removeUpdates(locationListener);
        shakeEventListener.setOnShakeListener(null);
    }
    //прорисовка гармоник
    private void drawGarmonics(double[] amplitudes){
        barEntries = new ArrayList<>();
        for(int i=0; i < amplitudes.length; i++){
            barEntries.add(new BarEntry(i+1, (float)amplitudes[i]));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "garmonics");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);//подписи к столбцам убираем
        barChart.setData(barData);
        barChart.invalidate();
    }
    /*//запись информации о яме
    public void onPitClick(View view){
        InfoTableEntity infoEntity = new InfoTableEntity();
        infoEntity.setDataTime(DateTimeService.getCurrentDateAndTime());
        infoEntity.setType("pit");
        localStorageService.insertInfo(infoEntity);
    }
    //запись информации о неровности
    public void onRoughClick(View view){
        InfoTableEntity infoEntity = new InfoTableEntity();
        infoEntity.setDataTime(DateTimeService.getCurrentDateAndTime());
        infoEntity.setType("rough");
        localStorageService.insertInfo(infoEntity);
    }
    */
}
