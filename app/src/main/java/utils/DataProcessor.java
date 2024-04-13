package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataProcessor {
    private String plainData;

    private final float[][] sample = {
            {1.7761e+02f, -9.2900e+00f, -6.7400e+00f, -2.0260e+03f, 3.7540e+03f,
                    4.8580e+03f, 7.2400e+02f, 8.8100e+02f, 7.3900e+02f, 7.5600e+02f,
                    8.0700e+02f},
            {-1.5597e+02f, -3.2700e+01f, -2.3560e+01f, -2.6890e+03f, 4.6250e+03f,
                    6.8790e+03f, 7.9300e+02f, 7.9300e+02f, 7.3300e+02f, 7.7100e+02f,
                    8.0000e+02f},
            {-1.3091e+02f, -4.3650e+01f, -4.6280e+01f, -5.2810e+03f, 3.1380e+03f,
                    8.4720e+03f, 7.9300e+02f, 7.9300e+02f, 7.3400e+02f, 7.7600e+02f,
                    8.0000e+02f},
            {-8.4730e+01f, -5.3710e+01f, -7.8640e+01f, -5.8230e+03f, 2.0440e+03f,
                    8.0290e+03f, 7.9200e+02f, 7.9300e+02f, 7.3000e+02f, 7.6900e+02f,
                    8.0000e+02f},
            {-6.1830e+01f, -1.1738e+02f, -9.0360e+01f, -5.8310e+03f, 9.8200e+02f,
                    7.1920e+03f, 7.2600e+02f, 8.6900e+02f, 7.2900e+02f, 7.7700e+02f,
                    7.9900e+02f},
            {-1.1484e+02f, -6.3960e+01f, -6.3350e+01f, -5.8950e+03f, 1.0420e+03f,
                    5.5960e+03f, 7.2000e+02f, 8.7200e+02f, 7.2600e+02f, 7.7100e+02f,
                    7.9800e+02f},
            {-1.3714e+02f, -4.9300e+01f, -1.4710e+01f, -5.4240e+03f, -7.7300e+02f,
                    3.9230e+03f, 7.8800e+02f, 7.8800e+02f, 7.2100e+02f, 7.6100e+02f,
                    7.9700e+02f},
            {-1.5978e+02f, -1.3840e+01f, 2.1230e+01f, -6.4240e+03f, -2.7380e+03f,
                    4.0560e+03f, 7.2500e+02f, 8.1200e+02f, 6.5900e+02f, 7.5500e+02f,
                    7.7600e+02f},
            {1.5964e+02f, 2.3590e+01f, 3.6850e+01f, -5.1310e+03f, -2.0040e+03f,
                    3.6130e+03f, 7.5000e+02f, 7.5100e+02f, 5.9300e+02f, 7.5400e+02f,
                    7.6700e+02f},
            {1.3264e+02f, 3.5300e+01f, 1.8590e+01f, -4.4670e+03f, 1.6500e+02f,
                    2.1630e+03f, 7.4200e+02f, 7.4000e+02f, 5.7300e+02f, 7.5200e+02f,
                    7.6200e+02f}, {1.3264e+02f, 3.5300e+01f, 1.8590e+01f, -4.4670e+03f, 1.6500e+02f,
            2.1630e+03f, 7.4200e+02f, 7.4000e+02f, 5.7300e+02f, 7.5200e+02f,
            7.6200e+02f}};

    public static float[] stringToFloatArray(String data) {
        String[] splitString = data.split(",\\s*");
        float[] floatArray = new float[splitString.length];

        for (int i = 0; i < splitString.length; i++) {
            floatArray[i] = Float.parseFloat(splitString[i]);
        }

        return floatArray;
    }

    public static float[][] getSampler() {
        float[][] something = {
                {1.7761e+02f, -9.2900e+00f, -6.7400e+00f, -2.0260e+03f, 3.7540e+03f,
                        4.8580e+03f, 7.2400e+02f, 8.8100e+02f, 7.3900e+02f, 7.5600e+02f,
                        8.0700e+02f},
                {-1.5597e+02f, -3.2700e+01f, -2.3560e+01f, -2.6890e+03f, 4.6250e+03f,
                        6.8790e+03f, 7.9300e+02f, 7.9300e+02f, 7.3300e+02f, 7.7100e+02f,
                        8.0000e+02f},
                {-1.3091e+02f, -4.3650e+01f, -4.6280e+01f, -5.2810e+03f, 3.1380e+03f,
                        8.4720e+03f, 7.9300e+02f, 7.9300e+02f, 7.3400e+02f, 7.7600e+02f,
                        8.0000e+02f},
                {-8.4730e+01f, -5.3710e+01f, -7.8640e+01f, -5.8230e+03f, 2.0440e+03f,
                        8.0290e+03f, 7.9200e+02f, 7.9300e+02f, 7.3000e+02f, 7.6900e+02f,
                        8.0000e+02f},
                {-6.1830e+01f, -1.1738e+02f, -9.0360e+01f, -5.8310e+03f, 9.8200e+02f,
                        7.1920e+03f, 7.2600e+02f, 8.6900e+02f, 7.2900e+02f, 7.7700e+02f,
                        7.9900e+02f},
                {-1.1484e+02f, -6.3960e+01f, -6.3350e+01f, -5.8950e+03f, 1.0420e+03f,
                        5.5960e+03f, 7.2000e+02f, 8.7200e+02f, 7.2600e+02f, 7.7100e+02f,
                        7.9800e+02f},
                {-1.3714e+02f, -4.9300e+01f, -1.4710e+01f, -5.4240e+03f, -7.7300e+02f,
                        3.9230e+03f, 7.8800e+02f, 7.8800e+02f, 7.2100e+02f, 7.6100e+02f,
                        7.9700e+02f},
                {-1.5978e+02f, -1.3840e+01f, 2.1230e+01f, -6.4240e+03f, -2.7380e+03f,
                        4.0560e+03f, 7.2500e+02f, 8.1200e+02f, 6.5900e+02f, 7.5500e+02f,
                        7.7600e+02f},
                {1.5964e+02f, 2.3590e+01f, 3.6850e+01f, -5.1310e+03f, -2.0040e+03f,
                        3.6130e+03f, 7.5000e+02f, 7.5100e+02f, 5.9300e+02f, 7.5400e+02f,
                        7.6700e+02f},
                {1.3264e+02f, 3.5300e+01f, 1.8590e+01f, -4.4670e+03f, 1.6500e+02f,
                        2.1630e+03f, 7.4200e+02f, 7.4000e+02f, 5.7300e+02f, 7.5200e+02f,
                        7.6200e+02f}, {1.3264e+02f, 3.5300e+01f, 1.8590e+01f, -4.4670e+03f, 1.6500e+02f,
                2.1630e+03f, 7.4200e+02f, 7.4000e+02f, 5.7300e+02f, 7.5200e+02f,
                7.6200e+02f}};
        return something;
    }

    public float[][] getSample() {
        return this.sample;
    }

    public static float[][] limitToTen(float[][] data) {

        if (data.length == 10) {
            return data;
        }

        int columnLength = 10;
        float[][] limitedArray = new float[10][11];
        int sizeLength = data.length;
        int indexRef = sizeLength / columnLength;
        int counter = 0;
        float indexToTake = 0;

        while (counter < columnLength) {
            indexToTake += indexRef;
            int roundedIndex =  Math.round(indexToTake - 1);
            push(limitedArray, counter, data[roundedIndex]);
            counter += 1;
        }

        return limitedArray;
    }

    public static float[][] limitTo(float[][] data, int limit) {
        if (data.length == limit) {
            return data;
        }

        int columnLength = limit;
        float[][] limitedArray = new float[limit][11];
        int sizeLength = data.length;
        int indexRef = sizeLength / columnLength;
        int counter = 0;
        float indexToTake = 0;

        while (counter < columnLength) {
            indexToTake += indexRef;
            int roundedIndex =  Math.round(indexToTake - 1);
            push(limitedArray, counter, data[roundedIndex]);
            counter += 1;
        }

        return limitedArray;
    }

    public static float[][] listToFloat(List<float[]> data) {
        float[][] collector = new float[data.size()][];

        for (int i = 0; i < data.size(); i++) {
            collector[i] = data.get(i);
        }

        return collector;
    }

    public static List<float[]> floatToList(float[][] data) {
        List<float[]> collector = new ArrayList<>();

        for (float[] datum : data) {
            collector.addAll(Collections.singleton(datum));
        }

        return collector;
    }
    public static void push(float[][] array, int row, float[] newArray) {
        if (row >= 0 && row < array.length) {
            if (newArray.length <= array[row].length) {
                System.arraycopy(newArray, 0, array[row], 0, newArray.length);
            } else {
                System.out.println("New array length exceeds row length");
            }
        } else {
            System.out.println("Index out of bounds");
        }
    }
}
