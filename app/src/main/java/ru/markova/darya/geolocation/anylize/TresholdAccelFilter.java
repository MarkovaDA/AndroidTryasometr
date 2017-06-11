package ru.markova.darya.geolocation.anylize;

import java.util.List;

import ru.markova.darya.geolocation.entity.AccelerationTableEntity;

/**
 * Created by Vitaliy Kholuenko on 11.06.2017.
 */
public class TresholdAccelFilter {
    private static final double G = 9.8;  // G value
    private static final double FOURTH_CLASS_DIFF_TRESHOLD = 14.0;  // treshold for difference between accels ang G value for detecting 4th class path parts
    private static final double THIRD_CLASS_DIFF_TRESHOLD = 8.0;  // treshold for difference between accels ang G value for detecting 3th class path parts

    public static int classifyPathPart(List<AccelerationTableEntity> accels){
        final int currentAxis = 2;  // Axis which we analyse
        for (AccelerationTableEntity accel : accels) {
            if (Math.abs(accel.getAccelZ() - G) > FOURTH_CLASS_DIFF_TRESHOLD) {
                return 4;
            }
            else if (Math.abs(accel.getAccelZ() - G) > THIRD_CLASS_DIFF_TRESHOLD) {
                return 3;
            }
        }
        return 0;
    }
}
