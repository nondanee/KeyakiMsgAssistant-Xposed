package com.nondanee.keyakimsgassistant;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Nzix on 2017/9/8.
 */
public class Worker extends IntentService {

    public Worker() {
        // The super call is required. The background thread that IntentService
        // starts is labeled with the string argument you pass.
        super("Worker");
    }

    private void copyText(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(this.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("message", text);
        clipboardManager.setPrimaryClip(clipData);
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFile(String url, String fileName){
        if (!Checker.storageAccessible(this)) return;
        try {
            File album = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + Constant.ALBUM_NAME + "/");
            if (!album.exists()) album.mkdir();

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + Constant.ALBUM_NAME + "/" + fileName);
            if (file.exists()) return;

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, Constant.ALBUM_NAME + "/" + fileName);

            request.setTitle(fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(true);
            request.allowScanningByMediaScanner();

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Constant.ACTION_DOWNLOAD.equals(action)) {
            int id = intent.getIntExtra("id", 0);
            String url = intent.getStringExtra("url");
            String fileName = intent.getStringExtra("fileName");
            notificationManager.cancel(id);
            downloadFile(url, fileName);
        }
        else if(Constant.ACTION_COPY.equals(action)) {
            String text = intent.getStringExtra("text");
            copyText(text);
        }
        else if(Constant.ACTION_DISMISS.equals(action)) {
            int id = intent.getIntExtra("id", 0);
            notificationManager.cancel(id);
        }
        else if(Constant.ACTION_SETTING.equals(action)){
            int id = intent.getIntExtra("id", 0);
            String permission = intent.getStringExtra("permission");
            notificationManager.cancel(id);
            if (permission.equals("storage")) Checker.storagePermissionRequest(this);
            else if (permission.equals("overlay")) Checker.OverlayPermissionRequest(this);
        }
    }
}



