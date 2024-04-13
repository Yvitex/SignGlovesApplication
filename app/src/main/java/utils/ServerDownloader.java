package utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerDownloader {
    public static void downloadFile(Context context, String fileUrl, String filename) {
        Log.d("Server Downloader", "Downloading File: " + fileUrl + " to " + Environment.getExternalStorageDirectory() + filename);
        Toast.makeText(context, "Connecting to " + fileUrl, Toast.LENGTH_LONG).show();
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String fileUrl = params[0];
                try {
                    URL url = new URL(fileUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    File destinationPath = new File(Environment.getExternalStorageDirectory(), filename);
                    FileOutputStream outputStream = new FileOutputStream(destinationPath);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();
                    connection.disconnect();
                } catch (Exception e) {
                    Log.e("Server Downloader", "downloadFile: " + e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // Update UI or perform any post-execution tasks here
            }
        }.execute(fileUrl);
    }
}
