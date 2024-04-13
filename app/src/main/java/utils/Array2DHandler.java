package utils;

import android.util.Log;

public class Array2DHandler {

    private float[][] incomingData = new float[10][11];
    public Array2DHandler() {

    }

    public Array2DHandler(float[][] data) {
        this.incomingData = data;
    }

    public float[][] getIncomingData() {
        return this.incomingData;
    }

    public void setIncomingData(float[][] data) {
        this.incomingData = data;
    }

    public float[][] normalizeData(float[][] sample) {
        return normalizer(sample);
    }

    public float[][] normalizeData() {
        if (this.incomingData != null) {
            this.incomingData = normalizer(this.incomingData);
            return this.incomingData;
        } else {
            return null;
        }
    }

    public float[] flattenData(float[][] data) {
        return flatten(data);
    }

    public float[] flattenData() {
        if (this.incomingData != null) {
            return flatten(this.incomingData);
        }
        return null;
    }

    public void display2d(float[][] data) {
        for (int i = 0; i < data.length; i++) {
            for(int j = 0; j < data[i].length; j++) {
                Log.d("Normalizer", "Actual Data: " + String.valueOf(data[i][j]));
            }
        }
    }

    private float[] flatten(float[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        float[] flattenedArray = new float[rows * cols];
        int index = 0;

        // Iterate through each element of the 2D array and copy it into the flattened array
        for (float[] floats : data) {
            for (int j = 0; j < cols; j++) {
                flattenedArray[index++] = floats[j];
            }
        }
        return flattenedArray;
    }

    private float[][] normalizer(float[][] sample){
        float[][] resultContainer = sample;

        float[] norms = new float[sample.length];
        for (int i = 0; i < sample.length; i++) {
            float norm = 0.0f;
            for (int j = 0; j < sample[i].length; j++) {
                norm += (float) Math.pow(sample[i][j], 2);
            }
            norms[i] = (float) Math.sqrt(norm);
        }

        for (int i = 0; i < sample.length; i++) {
            for (int j = 0; j < sample[i].length; j++) {
                resultContainer[i][j] /= norms[i];
            }
        }

        display2d(resultContainer);

        return resultContainer;
    }




}
