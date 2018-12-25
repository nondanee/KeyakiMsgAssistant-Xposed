package com.nondanee.keyakimsgassistant;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Nzix on 2017/9/8.
 */
public class Worker extends JobIntentService {

//    public Worker() {
//        super("Worker");
//    }

    public static final int JOB_ID = 1000;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, Worker.class, JOB_ID, work);
    }

    private void copyText(final Context context, final String text) {
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("message", text);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFile(Context context, String url, String fileName){
        if (!Checker.storageAccessible(context)) return;
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

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Constant.ACTION_DOWNLOAD.equals(action)) {
            int id = intent.getIntExtra("id", 0);
            String url = intent.getStringExtra("url");
            String fileName = intent.getStringExtra("fileName");
            notificationManager.cancel(id);
            downloadFile(this, url, fileName);
        }
        else if(Constant.ACTION_COPY.equals(action)) {
            String text = intent.getStringExtra("text");
            copyText(this, text);
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
            else if (permission.equals("overlay")) Checker.overlayPermissionRequest(this);
        }
    }

}



