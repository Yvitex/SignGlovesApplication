package utils;

import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.activity.result.ActivityResultLauncher;

import com.example.signglovesapplication.CaptureAct;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QrCodeHandler {
    public QrCodeHandler() {

    }

    public void scanOptions(ActivityResultLauncher<ScanOptions> barLauncher) {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(scanOptions);
    }


}
