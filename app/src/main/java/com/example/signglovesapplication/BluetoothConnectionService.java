package com.example.signglovesapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import utils.DataProcessor;

public class BluetoothConnectionService {
    private final String TAG = "Bluetoothm1";
    private final String appName = "BluetoothConnectionServ";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    //    private AcceptThread inseccureAcceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private BluetoothDevice bluetoothDevice;
    private UUID deviceUUID;
    ProgressDialog progressDialog;
    Context context;

    public BluetoothConnectionService(Context context, BluetoothManager bluetoothManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionCheck(context);
        }
        this.context = context;
        this.bluetoothManager = bluetoothManager;
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket;
        private final Handler handler;
        public ConnectThread(UUID uuid, BluetoothDevice device, Handler handler) {
            bluetoothDevice = device;
            deviceUUID = uuid;
            this.handler = handler;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            BluetoothSocket tmp = null;
            Log.d(TAG, "run: ConnectThread");

            try {
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "run: IOException" + e.getMessage());
            }

            bluetoothSocket = tmp;
            bluetoothAdapter.cancelDiscovery();

            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    bluetoothSocket.close();
                    Log.e(TAG, "run: BluetoothSocet close");
                } catch (IOException ex) {
                    Log.e(TAG, "run: BluetoothSocket close failed " + ex.getMessage());
                }
                Log.e(TAG, "run: Couldn't connect to UUID" + MY_UUID);
            }

            connected(bluetoothSocket, bluetoothDevice, handler);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: BluetoothSocket Cancel");
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: IOException" + e.getMessage());
            }
        }
    }

    public class ConnectedThread extends Thread {
        private BluetoothSocket bluetoothSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private String readValue;
        private Handler handler;

        public ConnectedThread(BluetoothSocket socket, Handler handler) {
            Log.d(TAG, "ConnectedThread: Started");
            this.bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            this.handler = handler;


            try {
                progressDialog.dismiss();
            } catch (NullPointerException e) {
                Log.e(TAG, "ConnectedThread: NullPointer " + e.getMessage());
            }


            try {
                tmpIn = this.bluetoothSocket.getInputStream();
                tmpOut = this.bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: IOException " + e.getMessage());
            }

            this.inputStream = tmpIn;
            this.outputStream = tmpOut;
        }


        public void run() {
            byte[] bufferD = new byte[1024];
            int bytes = 0;
            String message = new String();
            StringBuilder messageBuilder = new StringBuilder();
            String completeMessage = new String();
            int indexSpace;
            int counter = 0;
            float[][] cornStorage = new float[10][11];
            DataProcessor processor = new DataProcessor();

            while(true) {
                try {
                    bytes = this.inputStream.read(bufferD);
                    boolean isFound = false;
                    message = new String(bufferD, 0, bytes);
                    for (int i = 0; i < message.length(); i++) {
                        if (message.charAt(i) != '\n') {
                            messageBuilder.append(message.charAt(i));
                            counter++;
                        } else {
                            isFound = true;
                            indexSpace = i;
                        }
                    }
                    if (isFound) {
                        isFound = false;
                        completeMessage = messageBuilder.substring(0, counter);
                        messageBuilder.delete(0, counter);
                        counter = 0;
                        Log.d(TAG, "run: MEssage " + completeMessage);
                        Message messageToSet = handler.obtainMessage(1, completeMessage);
                        messageToSet.sendToTarget();
                    }

                } catch (IOException e) {
                    Log.d(TAG, "run: IOException " + e.getMessage());
                    break;
                }
            }
        }

        public void cancel() {
            try {
                this.bluetoothSocket.close();
                Log.d(TAG, "cancel: Close");
            } catch (IOException e) {
                Log.e(TAG, "cancel: IOException " + e.getMessage());
            }
        }

        public void write(byte[] bytes) {
            Log.d(TAG, "write: Writting");
            try {
                this.outputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: IOException" + e.getMessage());
            }
        }
    }

    public synchronized void start() {
        Log.d(TAG, "start: Start");

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid, Handler handler) {
        Log.d(TAG, "startClient: Start");
        progressDialog = ProgressDialog.show(context,"Connecting Bluetooth", "Please wait...", true);
        connectThread = new ConnectThread(uuid, device, handler);
        connectThread.start();
    }

    public void connected(BluetoothSocket socket, BluetoothDevice device, Handler handler) {
        Log.d(TAG, "connected: Starting");
        connectedThread = new ConnectedThread(socket, handler);
        connectedThread.start();
    }

    public void write(byte[] bytes) {
        Log.d(TAG, "write: Write Called");
        connectedThread.write(bytes);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void PermissionCheck(Context context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 0);
        }
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 0);
        }
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }


}
