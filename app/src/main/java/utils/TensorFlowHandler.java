package utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.example.signglovesapplication.ml.Model;

import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.TensorFlowLite;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class TensorFlowHandler {

    private float[] dataToBeProcessed = new float[10*11];
    private int classNumber;
    private float[] processedData = new float[classNumber];
    private float highestValue;
    private int highestIndex;
    private Interpreter interpreter;
    private Context context;

    private String Err = "Error";
    private String mainDirectory = "GlovesApp";
    private File modelPath = new File(Environment.getExternalStorageDirectory(), mainDirectory + "/model.tflite");
    private int inputColumn = 10;

    public TensorFlowHandler(float[] data, int classNumber, Context context, boolean isExternal){
        this.dataToBeProcessed = data;
        this.classNumber = classNumber;
        this.context = context;
        if (isExternal) {
            externalModelResult(modelPath.toString());
        } else {
            getResult();
            processHighestIndex();
        }

    }

    public int getHighestIndex() {
        return this.highestIndex;
    }

    public float getHighestValue() {
        return highestValue;
    }

    public float[] getResult() {
        if (this.dataToBeProcessed != null) {
            this.processedData = processResult(this.dataToBeProcessed);
            return this.processedData;
        } else {
            Log.d(Err, "getResult: this.dataToBeProcessed is null");
            return null;
        }
    }

    public float[] getResult(float[] data) {
        return processResult(data);
    }

    public void externalModelResult(String modelPath) {
        Log.d("Model", "Executed External Model Result");
        try {
            File modelFile = new File(modelPath);
            FileInputStream fileInputStream = new FileInputStream(modelFile);
            FileChannel fileChannel = fileInputStream.getChannel();
            long startOffset = fileChannel.position();
            long declaredLength = fileChannel.size();
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            interpreter = new Interpreter(mappedByteBuffer);
        } catch (Exception e) {
            Log.d("Error", "externalModelResult: " + e.toString());
        }
    }

    public float[] doInference() {
        if (this.interpreter == null) {
            Log.d("Model", "doInference: notloaded ");
            return null; // Model not loaded
        }
        try {
            TensorBuffer inputTensor = TensorBuffer.createFixedSize(new int[]{1, this.inputColumn, 11}, DataType.FLOAT32);
            inputTensor.loadArray(this.dataToBeProcessed);

            TensorBuffer outputTensor = TensorBuffer.createFixedSize(new int[]{1, this.classNumber}, DataType.FLOAT32);

            this.interpreter.run(inputTensor.getBuffer(), outputTensor.getBuffer().rewind());
            Log.d("Model", "doInference: " + Arrays.toString(outputTensor.getFloatArray()));
            Log.d("Model", "Highest: " + getFinalIndex(outputTensor.getFloatArray()));
            return outputTensor.getFloatArray();
        } catch (Exception e) {
            Log.d("Error", "externalModelResult: " + e.getMessage());
            return null;
        }
    }



    private float[] processResult(float[] data) {
        try {
            Model model = Model.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 10, 11}, DataType.FLOAT32);
            inputFeature0.loadArray(data);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] outputValues = outputFeature0.getFloatArray();
            model.close();

            return outputValues;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int processHighestIndex(float[] data) {
        return getFinalIndex(data);
    }

    public void processHighestIndex() {
        if (this.processedData != null) {
            int highest = getFinalIndex(this.processedData);
        } else {
            Log.d(Err, "getHighestIndex: this.processData is null");
        }
    }

    public int getFinalIndex(float[] data){
        int highestIndex = 0;
        float highestValue = 0;
        for (int i = 0; i < data.length; i++) {
            if(data[i] > highestValue){
                highestValue = data[i];
                highestIndex = i;
            }
        }
        this.highestValue = highestValue;
        this.highestIndex = highestIndex;
        return highestIndex;
    }

    public static String getPathFromURI(Uri uri, Context context) {
        String filePath = null;
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = context.getContentResolver();
            try {
                InputStream inputStream = contentResolver.openInputStream(uri);
                if (inputStream != null) {
                    File tempFile = new File(context.getCacheDir(), "temp_model_file.tflite");
                    FileOutputStream outputStream = new FileOutputStream(tempFile);
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.flush();
                    outputStream.close();
                    filePath = tempFile.getAbsolutePath();
                    inputStream.close();
                }
            } catch (Exception e) {
                Log.e("Model", "getPathFromURI: " + e.getMessage());
            }
        } else if (scheme != null && scheme.equals(ContentResolver.SCHEME_FILE)) {
            filePath = uri.getPath();
        }
        return filePath;
    }


}
