package com.example.signglovesapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.hoho.android.usbserial.driver.UsbSerialPort;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import data.WordProcessor;
import utils.Array2DHandler;
import utils.BluetoothHandler;
import utils.DataProcessor;
import utils.TensorFlowHandler;
import utils.UsbHandler;

public class StartMode extends AppCompatActivity {

    private Button debugButton;
    private Button lights;
    private TextView percentage;
    private TextView dataInput;
    private WordProcessor wordProcessor;
    private ProgressBar progressBar;
    private UsbSerialPort port;
    int counter = 0;
    private boolean willWork = true;

    private boolean isCustom = false;
    private Locale selectedLocale = Locale.ENGLISH;
    private String TAG = "StartMode";
    private UsbHandler usbHandler;


    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_mode);

        Bundle data = getIntent().getExtras();
        if (data != null) {
            this.isCustom = data.getBoolean("isCustom");
            String getLocale = data.getString("locale");
            if (getLocale != null) {
                switch (getLocale) {
                    case "Japanese":
                        this.selectedLocale = Locale.JAPANESE;
                        break;
                    case "Tagalog":
                        this.selectedLocale = new Locale("filipino");
                        break;
                    case "Bisaya":
                        this.selectedLocale = new Locale("visaya");
                        break;
                    case "Italian":
                        this.selectedLocale = Locale.ITALIAN;
                        break;
                    case "Chinese":
                        this.selectedLocale = Locale.CHINESE;
                        break;
                    default:
                        this.selectedLocale = Locale.ENGLISH;
                }
            }

        }

        // Temporary Main Container of Data
        List<float[]> collector = new ArrayList<>();

        // Instantiating Word Processor
        wordProcessor = new WordProcessor(this, this.selectedLocale);

        // something else
        dataInput = findViewById(R.id.processedWord);
        percentage = findViewById(R.id.percentageShow);
        progressBar = findViewById(R.id.progressBar);

        // Should press s in arduino
        debugButton = findViewById(R.id.debugSwitch);
        // Press o in arduino
        lights = findViewById(R.id.lightsButton);

        try {
            usbHandler = new UsbHandler(this);
            usbHandler.requestPermission();
            Log.d(TAG, "onCreate: Approved Permission");
            port = usbHandler.getPort();
            Log.d(TAG, "onCreate: got port" + port.toString());
        } catch (NullPointerException e) {
            Toast.makeText(this, "No USB Connected", Toast.LENGTH_LONG).show();
            this.willWork = false;
        } catch (IllegalArgumentException f) {
            usbHandler.closePort();
            usbHandler = new UsbHandler(this);
            usbHandler.requestPermission();
            Log.d(TAG, "onCreate: Approved Permission");
            port = usbHandler.getPort();
            Log.d(TAG, "onCreate: got port" + port.toString());
        }


        if (willWork) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024]; // Buffer size as needed
                    StringBuilder messageBuilder = new StringBuilder(); // To accumulate characters until a delimiter is encountered

                    try {
                        // Continuously read data
                        while (true) {
                            int len = port.read(buffer, 1000);
                            if (len > 0) {
                                String data = new String(buffer, 0, len);
                                // Append the read data to the StringBuilder
                                messageBuilder.append(data);
                                // Process complete messages
                                while (true) {
                                    int delimiterIndex = messageBuilder.indexOf("\n");
                                    if (delimiterIndex == -1) {
                                        // No complete message found, break the loop
                                        break;
                                    }
                                    // Extract the complete message
                                    String completeMessage = messageBuilder.substring(0, delimiterIndex);
                                    // Remove the processed part (including the delimiter) from the StringBuilder
                                    messageBuilder.delete(0, delimiterIndex + 1);
                                    // Process the complete message
                                    processData(completeMessage);
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "run: " + e.toString());
                    }
                }

                // Method to process each complete message
                private void processData(String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int limit = 10;
                            int size = collector.size();
                            if (!message.isEmpty()) {
                                String incomingMessage = message;
                                if (incomingMessage.charAt(0) == 'S') {
                                    Log.d("Result", "handleMessage: stopped ");
                                    if (size > 0 && size < limit) {
                                        Log.d("Result", "handleMessage: Collector Cleared ");
                                        progressBar.setProgress(0, true);
                                        collector.clear();
                                    } else if (size >= limit) {
                                        Log.d("Result", "handleMessage: Collector Conversion ");
                                        // Convert to float before limiting to ten
                                        float[][] convertedToFloat = DataProcessor.listToFloat(collector);
                                        float[][] limitedData = DataProcessor.limitTo(convertedToFloat, 10);
                                        convertToWord(limitedData);

                                        progressBar.setProgress(0, true);
                                        collector.clear();
                                    } else {
                                        Log.d("Result", "handleMessage: System Encountered Unexpected Result ");
                                    }

                                } else {
                                    // Collect data
                                    try {
                                        float[] result = DataProcessor.stringToFloatArray(incomingMessage);
                                        progressBar.setProgress(size * 20);
                                        collector.add(result);
                                    } catch (NumberFormatException e) {
                                        Log.e("Model", "handleMessage: " + e.toString() + e.getMessage());
                                        Log.e("Model", "handleMessage: " + incomingMessage);
                                        Log.e("Model", "handleMessage: " + data);
                                        Toast.makeText(getApplicationContext(), "S", Toast.LENGTH_LONG).show();
                                        collector.clear();
                                    }
                                }
                            }
                        }
                    });
                }
            }).start();
        } else {
            dataInput.setText("No USB Connected");
        }

//        debugButton.setOnClickListener(view -> {
//            Log.d("Result", "onCreate: pressed debug");
//            btHandler.switchDebugMode();
//
//        });

//        lights.setOnClickListener(view -> {
//            Log.d("Result", "onCreate: pressed off");
//            increaseBar();
//        });
    }

    public void increaseBar() {
        counter++;
        if (counter > 10) {
            counter = 0;
        }
        progressBar.setProgress(counter * 10, true);
    }

    public void convertToWord(float[][] sample) {
        // Allow text to speech
        // Processed Incoming Data through Normalization then Flattening to 1d array
        float[][] data = DataProcessor.limitToTen(sample);
        Array2DHandler handle = new Array2DHandler(data);
        handle.normalizeData();
        float[] flatData = handle.flattenData();

        //This process the flat data into index of the word
        TensorFlowHandler tensor;
        String percentageText;

        if (!isCustom) {
            int classNumber = wordProcessor.getLengthEnglish();
            tensor = new TensorFlowHandler(flatData, classNumber, this, false);
            int num = tensor.getHighestIndex();
            float value = tensor.getHighestValue() * 100;
            dataInput.setText(wordProcessor.speak(num));
            percentageText = value + "%";
            percentage.setText(percentageText);
        } else {
            JSONObject words = wordProcessor.getCustomWords();
            Log.d("ExternalModel", "JSON: " + words.toString());
            int classNumber = wordProcessor.getLengthCustom();
            Log.d("ExternalModel", "classNumber: " + classNumber);
            tensor = new TensorFlowHandler(flatData, classNumber, this, true);
            float[] result = tensor.doInference();
            Log.d("ExternalModel", "inference: " + Arrays.toString(result));
            int value = tensor.getFinalIndex(result);
            try {
                String word = words.getString(String.valueOf(value));
                dataInput.setText(wordProcessor.speak(word));
            } catch (JSONException e) {
                Log.e("Error", "convertToWord: " + e.toString());
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown TextToSpeech engine
        wordProcessor.onDestroy();
        try {
            if (port != null) {
                port.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}