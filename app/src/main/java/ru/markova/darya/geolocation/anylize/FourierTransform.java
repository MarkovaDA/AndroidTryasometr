package ru.markova.darya.geolocation.anylize;

import java.util.List;

import ru.markova.darya.geolocation.entity.AccelerationTableEntity;

/**
 * Дискретное Фурье преобразование
 */
public class FourierTransform {

    //вычислить коэффициенты дискретного преобразования Фурье
    private double[][] computeDFT(List<AccelerationTableEntity> times, int axis){
        int n = times.size();
        double[][] koeff = new double[n][2];//матрица коэффициентов
        for (int k = 0; k < n; k++) {
            double sumreal = 0;
            double sumimag = 0;
            for (int t = 0; t < n; t++) {
                double angle = 2 * Math.PI * t * k / n;
                //в вычислении участвуют значения ускорений по оси axis
                sumreal += times.get(t).accels[axis] * Math.cos(angle);
                sumimag += -times.get(t).accels[axis] * Math.sin(angle);
            }
            koeff[k][0] = sumreal;
            koeff[k][1] = sumimag;
        }
        return koeff;
    }

    //квадраты коэффцициентов Фурье - гармоники спектра
    public double[] getAmplitudes(List<AccelerationTableEntity> times, int axis){
        double[][] koeff = computeDFT(times,axis);
        int length = times.size();
        double[] amplitudes = new double[length];
        for(int i=0; i < length; i++){
            amplitudes[i] = koeff[i][0]*koeff[i][0] + koeff[i][1]*koeff[i][1];
        }
        return amplitudes;
    }

}
