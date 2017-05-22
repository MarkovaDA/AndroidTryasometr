package ru.markova.darya.geolocation.anylize;

import java.util.ArrayList;
import java.util.List;

import ru.markova.darya.geolocation.dto.PitDTO;
import ru.markova.darya.geolocation.entity.AccelerationTableEntity;

/**
 * Детектор ям на отрезке по Фурье спектру
 */
public class PitDetectror {

    private static final int DEFAULT_FOURIET_SPECTRE_LENGTH = 100; // 1 секунда при интервале измерений в 10 мс

    //преобразователь Фурье
    private static FourierTransform fourier;

    //отсчеты
    private static List<AccelerationTableEntity> timeCounts;
    private PitDetectror(){
        fourier = new FourierTransform();
    }

    private static class PitDetectorHolder{
        private final static PitDetectror instance =
                new PitDetectror();
    }
    public static PitDetectror getDetector(List<AccelerationTableEntity> times){
        timeCounts = times;
        return PitDetectorHolder.instance;
    }

    public static PitDTO isTherePit(){
        //ввести пороговое значение, выше которого должна быть амплитуда
        int i=0;
        int baseAxis = 0;//номер базовой оси
        AccelerationTableEntity startPoint, endPoint;
        double avg; //средний показатель ыеличины гармоник на отрезке
        double[] initValues;
        if (timeCounts.isEmpty()) {
            return new PitDTO(0d, 0d, 0d, 0d, 0d);
        }
        else {
            initValues = new double[]
                    {
                            timeCounts.get(0).getAccelX(),
                            timeCounts.get(0).getAccelY(),
                            timeCounts.get(0).getAccelZ()
                    };
        }
        //выбиреам ось,модуль ускорения которого наиболее приближен к нормальному (9.8)
        baseAxis = getBaseAxis(initValues);
        startPoint = timeCounts.get(0);
        endPoint = timeCounts.get(timeCounts.size()-1);
        avg = fourier.getAvgOfAmplitudes(timeCounts, baseAxis);
        return new PitDTO(startPoint.getLon(), startPoint.getLat(), endPoint.getLon(),endPoint.getLat(), avg);
    }
    public static double[] getGarmonics(){
        if (timeCounts.isEmpty()) {
            return new double[DEFAULT_FOURIET_SPECTRE_LENGTH];
        }
        double[] initValues = new double[]
                {
                        timeCounts.get(0).getAccelX(),
                        timeCounts.get(0).getAccelY(),
                        timeCounts.get(0).getAccelZ()
                };
        //выбиреам ось,модуль ускорения которого наиболее приближен к нормальному (9.8)
        //int baseAxis = getBaseAxis(initValues);
        return fourier.getGarmonics(timeCounts, /*baseAxis*/2);
    }
    //определение оси телефона, коллинеарной вектору g
    private static int getBaseAxis(double[] values){
        double normal = 9.8;
        double delta = Math.abs(normal - values[0]);
        double _delta;
        int axisNumber = 0;
        for(int i=1; i < values.length;i++){
            _delta = Math.abs(normal - values[i]);
            if (_delta < delta) {
                delta = _delta;
                axisNumber = i;
            }
        }
        return axisNumber;
    }
}
