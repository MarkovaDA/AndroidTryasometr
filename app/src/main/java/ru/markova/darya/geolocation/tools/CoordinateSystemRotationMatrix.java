package ru.markova.darya.geolocation.tools;

import java.util.List;

/**
 * Created by Vitaliy on 20.05.2017.
 */
public class CoordinateSystemRotationMatrix {

    private static Double[][] rotationMatrix = new Double[][] {
        {1d, 0d, 0d},
        {0d, 1d, 0d},
        {0d, 0d, 1d}
    };

    public static void initMatrix(Double[] gx, Double[] gy, Double[] gz){
        initMatrix(avgMatrixValue(gx), avgMatrixValue(gy), avgMatrixValue(gz));
    }

    public static void initMatrix(Double gx, Double gy, Double gz){

        float[] vector = new float[] {
                gx.floatValue(), gy.floatValue(), gz.floatValue()
        };

        double gravity = Math.sqrt(gx * gx + gy * gy + gz * gz);
        double cos1 = Math.cos(Math.atan(gy / gx));
        double sin1 = Math.sin(Math.atan(gy / gx));

        double cos2 = gz / gravity;
        double sin2 = Math.sin(Math.acos(cos2));

        Double[][] rotation1 = new Double[3][3];
        Double[][] rotation2 = new Double[3][3];
        Double[][] rotation3 = new Double[3][3];
        Double[][] rotation4 = new Double[3][3];

        rotation1[0][0] = cos1 * cos2;
        rotation1[0][1] = cos2 * sin1;
        rotation1[0][2] = -sin2;

        rotation1[1][0] = -sin1;
        rotation1[1][1] = cos1;
        rotation1[1][2] = 0d;

        rotation1[2][0] = cos1*sin2;
        rotation1[2][1] = sin1*sin2;
        rotation1[2][2] = cos2;

        rotation2[0][0] = cos1 * cos2;
        rotation2[0][1] = cos2 * -1d * sin1;
        rotation2[0][2] = -sin2;

        rotation2[1][0] = sin1;
        rotation2[1][1] = cos1;
        rotation2[1][2] = 0d;

        rotation2[2][0] = cos1*sin2;
        rotation2[2][1] = -sin1*sin2;
        rotation2[2][2] = cos2;

        rotation3[0][0] = cos1 * cos2;
        rotation3[0][1] = cos2 * sin1;
        rotation3[0][2] = sin2;

        rotation3[1][0] = -sin1;
        rotation3[1][1] = cos1;
        rotation3[1][2] = 0d;

        rotation3[2][0] = -cos1*sin2;
        rotation3[2][1] = -sin1*sin2;
        rotation3[2][2] = cos2;

        rotation4[0][0] = cos1 * cos2;
        rotation4[0][1] = cos2 * -1d * sin1;
        rotation4[0][2] = sin2;

        rotation4[1][0] = sin1;
        rotation4[1][1] = cos1;
        rotation4[1][2] = 0d;

        rotation4[2][0] = -cos1*sin2;
        rotation4[2][1] = sin1*sin2;
        rotation4[2][2] = cos2;

        rotationMatrix = rotation1;
        float[] vector1 = Multiply(vector);
        double dim1 = euclideDimentionToG(vector1);

        rotationMatrix = rotation2;
        float[] vector2 = Multiply(vector);
        double dim2 = euclideDimentionToG(vector2);

        rotationMatrix = rotation3;
        float[] vector3 = Multiply(vector);
        double dim3 = euclideDimentionToG(vector3);

        rotationMatrix = rotation4;
        float[] vector4 = Multiply(vector);
        double dim4 = euclideDimentionToG(vector4);

        if (dim1 < dim2 && dim1 < dim3 && dim1 < dim4) {
            rotationMatrix = rotation1;
        }
        else if (dim2 < dim3 && dim2 < dim4 && dim2 < dim1) {
            rotationMatrix = rotation2;
        }
        else if (dim3 < dim2 && dim3 < dim1 && dim3 < dim4) {
            rotationMatrix = rotation3;
        }
        else {
            rotationMatrix = rotation4;
        }

        /*rotationMatrix[0][0] = cos1 * cos2;
        rotationMatrix[0][1] = cos2 * sin1;
        rotationMatrix[0][2] = -sin2;

        rotationMatrix[1][0] = -sin1;
        rotationMatrix[1][1] = cos1;
        rotationMatrix[1][2] = 0d;

        rotationMatrix[2][0] = cos1*sin2;
        rotationMatrix[2][1] = sin1*sin2;
        rotationMatrix[2][2] = cos2;*/
    }

    public static float[] Multiply(float[] vector){
        float[] result = new float[3];
        result[0] = rotationMatrix[0][0].floatValue() * vector[0] +
                rotationMatrix[0][1].floatValue() * vector[1] +
                rotationMatrix[0][2].floatValue() * vector[2];
        result[1] = rotationMatrix[1][0].floatValue() * vector[0] +
                rotationMatrix[1][1].floatValue() * vector[1] +
                rotationMatrix[1][2].floatValue() * vector[2];
        result[2] = rotationMatrix[2][0].floatValue() * vector[0] +
                rotationMatrix[2][1].floatValue() * vector[1] +
                rotationMatrix[2][2].floatValue() * vector[2];
        return result;
    }

    private static Double avgMatrixValue(Double[] matrix) {
        Double sum = 0d;
        for (Double aMatrix : matrix) {
            sum += aMatrix;
        }
        return sum/matrix.length;
    }

    private static double euclideDimentionToG(float[] vector) {
        return Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + (vector[2] - 9.8) * (vector[2] - 9.8));
    }

}
