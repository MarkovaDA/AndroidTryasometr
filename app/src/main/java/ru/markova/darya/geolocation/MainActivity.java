package ru.markova.darya.geolocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Date;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.markova.darya.geolocation.config.GreenDaoBuilder;
import ru.markova.darya.geolocation.config.RetrofitBuilder;
import ru.markova.darya.geolocation.dto.LocationDTO;
import ru.markova.darya.geolocation.entity.DaoSession;
import ru.markova.darya.geolocation.entity.GeoTableEntity;
import ru.markova.darya.geolocation.entity.GeoTableEntityDao;

public class MainActivity extends AppCompatActivity {
    TextView tvEnabledGPS;
    TextView tvStatusGPS;
    TextView tvLocationGPS;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;
    TextView txtDbTest;
    String   deviceIMEI;

    private LocationManager locationManager;

    //https://habrahabr.ru/post/143431/
    //кешировать данные в базу SQLLite и отправлять через каждый определенный интервал
    //файл отдельной конфигурации - синглтон
    //ORM Lite - фреймворк для работы с базой
    //ctrl  Alt  L - форматирование всего текста
    //!сделать кнопку,по её нажатию генерировать рандомные координаты и поверять,кладутся ли они в базу
    //настроить часовой поис gps, координаты помещать в базу сразу после фиксации их изменения!
    //создать сервис, похожий на джобраннер который будет координаты забирать из базы и послыать на сервер
    private static DataSendService dataSendService = RetrofitBuilder.getDataSendService();

    private static DaoSession daoSession;


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
        txtDbTest =     (TextView)findViewById(R.id.textTestDB);
        //получаем LocationManager, через который будем работать
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //идентификатор устройства
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = telephonyManager.getDeviceId();

        daoSession = GreenDaoBuilder.getDaoSession(MainActivity.this);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("onPause: приложение не имеет доступа к службе геолокации");
            return;
        }
        //отключаем слушателя
        locationManager.removeUpdates(locationListener);
    }

    //тестируемый метод при клике на кнопку
    public void onTestAddToDB(View view){
        //добавляем в базу,
        GeoTableEntity entity = new GeoTableEntity();
        entity.setDeviceImei(deviceIMEI);
        Random rnd = new Random();
        entity.setLat(rnd.nextDouble());
        entity.setLon(rnd.nextDouble());
        long curTime = System.currentTimeMillis();
        Date curDate = new Date(curTime);
        entity.setDataTime(curDate);
        daoSession.insert(entity);
        //тут же извлекаем  последнее значение и помещаем на форму
        //Query<GeoTableEntity> qb = daoSession.queryBuilder(GeoTableEntity.class).build();
        //qb.unique();
        GeoTableEntity last = daoSession.queryBuilder(GeoTableEntity.class)
            .where(GeoTableEntityDao.Properties.DeviceImei.eq(deviceIMEI))
            .orderDesc(GeoTableEntityDao.Properties.DataTime)
            .limit(1).unique();
        txtDbTest.setText(last.getId().toString());
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
            //отправка координат на сервер - вытащить скоп объектов и потом отправлять
            //базу подчищать
            Call<Object> call = dataSendService.sendCoordinate(
                new LocationDTO(location.getLongitude(), location.getLatitude(), deviceIMEI));
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    //успешная отправка
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    //неуспешная отправка
                }
            });
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

}
