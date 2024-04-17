package com.example.signglovesapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

import data.WordProcessor;
import utils.Array2DHandler;
import utils.BluetoothHandler;
import utils.QrCodeHandler;
import utils.ServerDownloader;
import utils.StorageHandler;
import utils.TensorFlowHandler;
import utils.UsbHandler;

@SuppressLint({"MissingPermission", "NewApi"})
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button startButton;
    private Button learnButton;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch bluetoothSwitch;
    private int REQUEST_ENABLE_BT = 0;
    private BluetoothHandler btHandler;
    private Button qrCodeButton;
    private String path;
    private StorageHandler storageHandler;
    private String language = "English";

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<ScanOptions> barLauncher;
    private QrCodeHandler qrCodeHandler;
    private String mainDirectory = "GlovesApp";
    private Spinner dropDown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult( ActivityResult result ) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager())
                        Toast.makeText(MainActivity.this,"We Have Permission",Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity.this, "Please, Enable Storage Permission", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please, Enable Storage Permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

        barLauncher = registerForActivityResult(new ScanContract(), result-> {
            String model = "model.tflite";
            String word = "WordsDictionary.txt";
            if (result.getContents() != null) {
                String baseLink = result.getContents();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Prepare to Download the Following:");
                builder.setMessage(baseLink + model + "\n" + baseLink + word + "\n");
                builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ServerDownloader.downloadFile(MainActivity.this, baseLink + model, mainDirectory + "/" + model);
                        ServerDownloader.downloadFile(MainActivity.this, baseLink + word, mainDirectory + "/" + word);
                        dialogInterface.dismiss();
                    }
                }).show();
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
            }
        });

        startButton = findViewById(R.id.startPatternMode);
        learnButton = findViewById(R.id.learningMode);
        qrCodeButton = findViewById(R.id.qrCodeButton);
        dropDown = findViewById(R.id.languageSelection);

        qrCodeHandler = new QrCodeHandler();
        btHandler = BluetoothHandler.getInstance(this);
        storageHandler = new StorageHandler(this, MainActivity.this, activityResultLauncher);
        storageHandler.createDirectory(mainDirectory);

        String[] languages = dropDown.getResources().getStringArray(R.array.languages);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
                languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropDown.setAdapter(adapter);
        dropDown.setOnItemSelectedListener(this);


        startButton.setOnClickListener(view -> {
            UsbHandler usbHandler = new UsbHandler(this);
            boolean state = usbHandler.requestPermission();
                Intent intent = new Intent(this, StartMode.class);
                intent.putExtra("isCustom", false);
                intent.putExtra("locale", this.language);
                startActivity(intent);

        });

        learnButton.setOnClickListener(view -> {
            UsbHandler usbHandler = new UsbHandler(this);
            boolean state = usbHandler.requestPermission();
            if (storageHandler.hasThisDirectory(mainDirectory + "/model.tflite")) {
                Intent intent = new Intent(this, StartMode.class);
                intent.putExtra("isCustom", true);
                intent.putExtra("locale", "English");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Can't find directory, make sure you " +
                        "downloaded the model", Toast.LENGTH_LONG).show();
            }

        });

        qrCodeButton.setOnClickListener(view -> {
            qrCodeHandler.scanOptions(this.barLauncher);
        });

    }

    // If Custom Model? We get the path, then send the path to Start Mode Context
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        if (uri != null) {
            String path = TensorFlowHandler.getPathFromURI(uri, this);
            if (path != null) {
                this.path = path;
                Intent intentCustomModel = new Intent(this, StartMode.class);
                intentCustomModel.putExtra("path", this.path);
                startActivity(intentCustomModel);

                Log.d("Model", "onActivityResult: " + path);
            } else {
                Log.e("Model", "Failed to retrieve file path from URI");
            }
        } else {
            Log.e("Model", "URI is null");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("Languagexx", "onItemSelected: " + adapterView.getId());
        Log.d("Languagexx", "onItemSelected: On Field");
        if (adapterView.getId() == R.id.languageSelection) {
            Log.d("Languagexx", "onItemSelected: Click");
            this.language = adapterView.getItemAtPosition(i).toString();
            Log.d("Languagexx", "onItemSelected: " + this.language);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}