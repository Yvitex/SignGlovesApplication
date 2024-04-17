package utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageHandler {
    private Context context;
    private String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private String TAG = "StorageHandler";
    private Activity activity;

    public StorageHandler(Context context, Activity activity, ActivityResultLauncher<Intent> activityResultLauncher) {
        this.context = context;
        this.activity = activity;
        if (!checkPermission()) {
            requestPermission();
        }
        this.activityResultLauncher = activityResultLauncher;

    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            new AlertDialog.Builder(context)
                    .setTitle("Permission")
                    .setMessage("Please give the Storage permission")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which ) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                intent.addCategory("android.intent.category.DEFAULT");
                                intent.setData(Uri.parse(String.format("package:%s", new Object[]{context.getPackageName()})));
                                activityResultLauncher.launch(intent);
                            } catch (Exception e) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                activityResultLauncher.launch(intent);
                            }
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            ActivityCompat.requestPermissions(this.activity, permissions, 30);
        }
    }

    public void save(String filename, String message) {
        File file = new File(Environment.getExternalStorageDirectory(), filename);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(message.getBytes());
            Toast.makeText(context, "File Saved: " + filename, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Error Saving File: " + filename, Toast.LENGTH_LONG).show();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Toast.makeText(context, "Error Saving File: " + filename, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void createDirectory(String directoryPath) {
        File directory = new File(Environment.getExternalStorageDirectory(), directoryPath);
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (success) {
                Toast.makeText(context, "Storage on " + directory.toString(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Failed to create directory, make sure you allow storage permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean hasThisDirectory(String directoryPath) {
        File directory = new File(Environment.getExternalStorageDirectory(), directoryPath);
        return directory.exists();
    }
}
