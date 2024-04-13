package utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.example.signglovesapplication.BluetoothConnectionService;

import java.util.Set;
import java.util.UUID;
import android.os.Handler;

@SuppressLint({"NewApi", "MissingPermission"})
public class BluetoothHandler {
    private final String debugString = "BluetoothS1";
    private BluetoothAdapter bluetoothAdapter;
    private Context context;
    private BluetoothConnectionService connectionService;
    private boolean isEnabled;
    private boolean isConnected = false;
    private String hardwareAddress = "00:22:09:01:D1:4A";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String btNameToConnectTo = "HC-06";
    private BluetoothDevice btDevice;
    private Set<BluetoothDevice> pairedDevices;
    private static BluetoothHandler instance;

    public BluetoothHandler(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) {
            Toast.makeText(context, "ERROR: The Phone does not have Bluetooth Capability or is incompatible",
                    Toast.LENGTH_LONG).show();
        }
    }

    public static BluetoothHandler getInstance(Context context){
        if (instance == null) {
            instance = new BluetoothHandler(context);
        }
        return instance;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void switchDebugMode() {
        this.connectionService.write("s".getBytes());
    }
    public void lightsOn() {
        Log.d("BluetoothS1", "lightsOn: ");
        this.connectionService.write("o".getBytes());
    }
    public void lightsOff() {
        Log.d("BluetoothS1", "lightsOff: ");
        this.connectionService.write("f".getBytes());
    }
    public Boolean getIsEnabled() {
        this.isEnabled = bluetoothAdapter.isEnabled();
        return this.isEnabled;
    }

    public void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled())
            PermissionChecker.PermissionCheck(context);
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBTIntent);
            Log.d(debugString, "enableBluetooth: enabled");

            boolean isBonded = this.connectBonded();
            if (!isBonded) {
                IntentFilter btFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                this.context.registerReceiver(btEnabler, btFilter);
            }

            IntentFilter connectionFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            this.context.registerReceiver(mBroadcastReceiver4, connectionFilter);
        }

    public void disableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            PermissionChecker.PermissionCheck(context);
            bluetoothAdapter.disable();
        }
    }

    public void discoverBluetooth() {
        PermissionChecker.PermissionCheck(context);
        Log.d(debugString, "discoverBluetooth: started");
        while (!this.getIsEnabled()) {

        }
        if (!bluetoothAdapter.isDiscovering()) {
            try {
                if (bluetoothAdapter.isEnabled()) {
                    boolean state = bluetoothAdapter.startDiscovery();
                    Log.d(debugString, "discoverBluetooth: " + String.valueOf(state));
                    Log.d(debugString, "discoverBluetooth: discovering");
                } else {
                    Log.d(debugString, "onCreate: Not Enabled");
                }
            } catch (Exception e) {
                Log.d(debugString, "onCreate: " + e.getMessage());
            }

            IntentFilter discoverIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.context.registerReceiver(btConnector, discoverIntent);

        } else if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
            Log.d(debugString, "discoverBluetooth: discovering");
            IntentFilter discoverIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.context.registerReceiver(btConnector, discoverIntent);
        }
    }

    public void connectToHC(String name, String mac, BluetoothDevice device) {
        if (name.equals(this.btNameToConnectTo) && mac.equals(this.hardwareAddress)) {
            Log.d(debugString, "Connecting to " + this.btNameToConnectTo);
            Toast.makeText(context, "Connected with " + btNameToConnectTo, Toast.LENGTH_LONG).show();
            bluetoothAdapter.cancelDiscovery();
            device.createBond();
            isConnected = true;
            connectionService = new BluetoothConnectionService(context, context.getSystemService(BluetoothManager.class));
        } else {
            Log.d(debugString, "Error Occured on connectToHC");
        }
    }

    public boolean connectBonded() {
        try {
            pairedDevices = bluetoothAdapter.getBondedDevices();
            if(!pairedDevices.isEmpty()) {
                for (BluetoothDevice paired: pairedDevices) {
                    Log.d(debugString, "Already On Memory: " + paired.getName());
                    if (paired.getName().equals("HC-06")) {
                        bluetoothAdapter.cancelDiscovery();
                        this.btDevice = paired;
                        paired.createBond();
                        connectionService = new BluetoothConnectionService(context, context.getSystemService(BluetoothManager.class));
                        return true;
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e(debugString, "onCreate: " + e.getMessage());
            return false;
        }
        return false;
    }

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(debugString, "Bonded with " + device.getName());
                    btDevice = device;
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(debugString, "Bonding with " + device.getName());
                }
                if (device.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(debugString, "Bonding Error with " + device.getName());
                }
            }
        }
    };

    public void startConnection(Handler externalHandler) {
        startBtConnection(this.btDevice, this.MY_UUID, externalHandler);
    }

    public void startBtConnection(BluetoothDevice device, UUID uuid, Handler handler) {
        Log.d(debugString, "StartBtConnection: Initializing BT Connection");
        connectionService.startClient(device, uuid, handler);
    }

    public BroadcastReceiver btConnector = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDevice = device;
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                connectToHC(deviceName, deviceHardwareAddress, device);
            }
        }
    };

    public final BroadcastReceiver btEnabler = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        isEnabled = false;
                        break;
                    case BluetoothAdapter.STATE_ON:
                        isEnabled = true;
                        discoverBluetooth();
                        break;
                }
            }
        }
    };



    public void onDestroy() {
        this.context.unregisterReceiver(btConnector);
        this.context.unregisterReceiver(btEnabler);
    }


}
