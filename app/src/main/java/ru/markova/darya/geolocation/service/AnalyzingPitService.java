package ru.markova.darya.geolocation.service;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.markova.darya.geolocation.MainActivity;
import ru.markova.darya.geolocation.anylize.PitDetectror;
import ru.markova.darya.geolocation.config.RetrofitBuilder;
import ru.markova.darya.geolocation.dto.PitDTO;
import ru.markova.darya.geolocation.dto.ResponseEntityDTO;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;
import ru.markova.darya.geolocation.entity.InfoTableEntity;

/*
 * сервис для отправки ускорений на сервер
 *  в последующем будет просто сервисом для интервального анализа и отправки точек на сервер, только если яма
 *  http://stackoverflow.com/questions/35202541/this-version-of-android-studio-is-incompatible-with-the-gradle-version-used-try
 */
public class AnalyzingPitService extends Service{

    final String LOG_TAG = "SendDataFromDBService";
    private  final static  Long CHECK_INTERVAL =  2500L; //интервал отправки ускорений на сервер

    private Handler checkAndSendHandler = null;

    //сервис для отправки запросов на сервер
    private  RetrofitDataSendService dataSendService;
    //сервис для работы с локальной базой данных
    private LocalStorageService localStorageService;
    private Intent intent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate(){
        super.onCreate();
        intent = new Intent(MainActivity.BROADCAST_ACTION);
        localStorageService = new LocalStorageService(this);
        checkAndSendHandler = new Handler();
        dataSendService = RetrofitBuilder.getDataSendService();
    }

    public void onDestroy(){
        super.onDestroy();
        if (checkAndSendHandler != null) {
            checkAndSendHandler.removeCallbacksAndMessages(null);
            checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
        }
        localStorageService.destroy();
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Служба отправки ускорений запущена", Toast.LENGTH_SHORT).show();
        checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
        return super.onStartCommand(intent, flags, startId);
    }


    private Runnable dataSendRunnable = new Runnable() {
        @Override
        public void run() {
            checkAndSendHandler.removeCallbacksAndMessages(null);
            final Date currentDate = DateTimeService.getCurrentDateAndTime();
            final List<AccelerationTableEntity> accelerations =
                    localStorageService.getSavedAccelerations(currentDate);
            double[] amplitudes = PitDetectror.getDetector(accelerations).getGarmonics();
            intent.putExtra(MainActivity.DRAW_GARMONICS_ACTION, amplitudes);
            sendBroadcast(intent);
            PitDTO currentSurface = PitDetectror.getDetector(accelerations).isTherePit();
            intent.putExtra(MainActivity.AVERAGE_INTERVAL_VALUE, currentSurface.getValue());
            sendBroadcast(intent);
            //отправляем, если распознали, что в этом месте яма
            Call<ResponseEntityDTO> call = dataSendService.markPitInterval(currentSurface);
            call.enqueue(new Callback<ResponseEntityDTO>() {
                @Override
                public void onResponse(Call<ResponseEntityDTO> call, Response<ResponseEntityDTO> response) {
                    //успешная отправка
                    Log.d(LOG_TAG, "SENDING PIT SUCCESS...");
                    //удаляем обработанные ускорения
                    localStorageService.deleteAccelerations(currentDate);
                    intent.putExtra(MainActivity.STATUS_SENDING_PARAM, "pit success:" + DateTimeService.getCurrentDateAndTimeString());
                    sendBroadcast(intent);
                    checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
                }
                @Override
                public void onFailure(Call<ResponseEntityDTO> call, Throwable t) {
                    //врменно удаляем и старые ускорения
                    localStorageService.deleteAccelerations(currentDate);
                    //неуспешная отправка
                    Log.d(LOG_TAG, "SENDING PIT FAILURE....");
                    intent.putExtra(MainActivity.STATUS_SENDING_PARAM, "pit fail:" + DateTimeService.getCurrentDateAndTimeString());
                    sendBroadcast(intent);
                    checkAndSendHandler.postDelayed(dataSendRunnable, CHECK_INTERVAL);
                }
            });
        }
    };
}
