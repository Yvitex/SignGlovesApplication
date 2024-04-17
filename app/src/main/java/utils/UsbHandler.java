package utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class UsbHandler {
    private UsbManager usbManager;
    private Context context;
    private final String TAG = "USB Handler";
    private boolean state = false;
    private UsbSerialDriver driver;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private PendingIntent permissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbSerialPort port;
    private boolean approvedPermission = false;

    public UsbHandler(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (!availableDrivers.isEmpty()) {
            this.state = true;
        }
        if (this.state) {
            this.driver = availableDrivers.get(0);
            this.device = this.driver.getDevice();
            if (this.device == null) {
                this.state = false;
            }
        }
        if (this.state) {
            this.connection = this.usbManager.openDevice(this.device);
        }
    }

    public boolean getConnection() {
        return this.connection != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public boolean requestPermission() {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this.context, 0,
                new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        try {
            if (this.connection == null) {
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                this.context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
                this.usbManager.requestPermission(this.device, permissionIntent);
                return false;
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            Toast.makeText(this.context, "Make sure that USB is connected", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    public UsbSerialPort getPort() {
        this.port = this.driver.getPorts().get(0);
        try {
            this.port.open(this.connection);
            this.port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.port;
    }

    public void closePort() {
        try {
            this.port.close();
        } catch (IOException e) {
            Log.e(TAG, "closePort: Failed to close port " + e.toString());
        }
    }

    public boolean getApprovedPermission() {
        return this.approvedPermission;
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d(TAG, "onReceive: Permitted");
                            approvedPermission = true;
                        }
                    }
                }
            } else {
                Log.d(TAG, "onReceive: Denied");
                approvedPermission = false;
            }
        }
    };
}
