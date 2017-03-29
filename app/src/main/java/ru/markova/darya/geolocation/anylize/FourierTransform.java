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
        double sumreal, sumimag;
        double[][] arr = new double[n][3];
        for (int i = 0; i < n; i++)
        {
             arr[i][0] = times.get(i).getAccelX();
             arr[i][1] = times.get(i).getAccelY();
             arr[i][2] = times.get(i).getAccelZ();
        };
        System.out.println("ИСХОДНЫЕ КОЭФФИЦИЕНТЫ РАЗЛОЖЕНИЯ");
        for (int k = 0; k < n; k++) {
            System.out.print(arr[k][axis] + " ");
            sumreal = 0.0;
            sumimag = 0.0;
            //значения ускорений в данной точке k
            //у ускорения мнимая часть равно нулю
            for (int t = 0; t < n; t++) {
                double angle = 2 * Math.PI * t * k / n;
                sumreal += arr[t][axis] * Math.cos(angle);
                sumimag += -1*arr[t][axis] * Math.sin(angle);
            }
            koeff[k][0] = sumreal;
            koeff[k][1] = sumimag;
        }
        return koeff;
    }

    //квадраты коэффцициентов Фурье - гармоники спектра
    public double getAvgOfAmplitudes(List<AccelerationTableEntity> times, int axis){
        double[][] koeff = computeDFT(times,axis);
        int length = times.size();
        double avg = 0.0;
        double[] amplitudes = new double[length];
        System.out.println("РЕАЛЬНЫЕ КОЭФФИЦИЕНТЫ РАЗЛОЖЕНИЯ");
        for(int i=0; i < length; i++){
            System.out.print(koeff[i][0] + " ");
            amplitudes[i] = koeff[i][0]*koeff[i][0] + koeff[i][1]*koeff[i][1];
            if (i>0)//первую ведущую гармонику не берем
            avg += amplitudes[i];
        }
        return avg/(length - 1);
    }

}
