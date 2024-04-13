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
import android.widget.Button;
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

@SuppressLint({"MissingPermission", "NewApi"})
public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private Button learnButton;
//    private TextView debuggingText;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch bluetoothSwitch;
    private int REQUEST_ENABLE_BT = 0;
    private final String debugString = "Bluetooth";
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothHandler btHandler;
    private Button qrCodeButton;
    private Intent intent;
    private String path;
    private TensorFlowHandler tensorman;
    private Array2DHandler handler;
    private StorageHandler storageHandler;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<ScanOptions> barLauncher;
    private QrCodeHandler qrCodeHandler;
    private String mainDirectory = "GlovesApp";


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
        bluetoothSwitch = findViewById(R.id.bluetoothSwitch);
        qrCodeButton = findViewById(R.id.qrCodeButton);
        handler = new Array2DHandler();

        qrCodeHandler = new QrCodeHandler();
        btHandler = BluetoothHandler.getInstance(this);
        storageHandler = new StorageHandler(this, MainActivity.this, activityResultLauncher);
        storageHandler.createDirectory(mainDirectory);

        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, StartMode.class);
            intent.putExtra("isCustom", false);
            startActivity(intent);
//            if (btHandler.isConnected()) {
//                Intent intent = new Intent(this, StartMode.class);
//                intent.putExtra("isCustom", false);
//                startActivity(intent);
//            } else {
//                Toast.makeText(this, "Not Connected to Gloves", Toast.LENGTH_LONG).show();
//            }
        });
        
//        try {
//            bluetoothSwitch.setChecked(btHandler.getIsEnabled());
//        } catch (NullPointerException e) {
//            Toast.makeText(this, "Encountered Error: BT Non Supported " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//
//        bluetoothSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
//            if (bluetoothSwitch.isChecked()) {
//                btHandler.enableBluetooth();
//            } else {
//                btHandler.disableBluetooth();
//            }
//        });

        learnButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, StartMode.class);
            intent.putExtra("isCustom", true);
            startActivity(intent);
        });

        qrCodeButton.setOnClickListener(view -> {
            qrCodeHandler.scanOptions(this.barLauncher);
        });

        WordProcessor wordProcessor = new WordProcessor(this, Locale.ITALIAN);
        JSONObject wordes = wordProcessor.getCustomWords();
        Log.d("External Model", "onCreate: " + wordes.toString());

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

}