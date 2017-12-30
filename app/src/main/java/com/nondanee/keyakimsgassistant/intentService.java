package com.nondanee.keyakimsgassistant;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import java.io.File;

/**
 * Created by Nzix on 2017/9/8.
 */
public class intentService extends IntentService {

    private static final String PACKAGE_NAME = "com.nondanee.keyakimsgassistant";
    private static final String CLASS_NAME = PACKAGE_NAME + ".intentService";
    private static final String ACTION_DISMISS = ".action.dismiss";
    private static final String ACTION_DOWNLOAD = ".action.download";
    private static final String ACTION_SETTING = ".action.setting";
    private static final String ALBUM_NAME = "KeyakiMsg";

    public intentService() {
        // The super call is required. The background thread that IntentService
        // starts is labeled with the string argument you pass.
        super("intentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            String action = intent.getAction();

            if(ACTION_DOWNLOAD.equals(action)) {
                int id = intent.getIntExtra("id", 0);
                String url = intent.getStringExtra("url");
                String fileName = intent.getStringExtra("fileName");

                if (isStoragePermissionGranted() == false) {
                    return;
                }

                try {

                    File album = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + ALBUM_NAME + "/");
                    if (!album.exists()) {
                        album.mkdir();
                    }
                    else{
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + ALBUM_NAME + "/" + fileName);
                        if (file.exists()) {
                            return;
                        }
                    }

                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/" + ALBUM_NAME, fileName);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, ALBUM_NAME + File.separator + fileName);

                    request.setTitle(fileName);

                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setVisibleInDownloadsUi(true);
                    request.allowScanningByMediaScanner();

                    DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);

                } catch (Exception e) {
                    Log.e("xposed_nondanee", e.toString());
                }

                final NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationmanager.cancel(id);
            }
            else if(ACTION_DISMISS.equals(action)) {

                int id = intent.getIntExtra("id", 0);
                final NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationmanager.cancel(id);

            }
            else if(ACTION_SETTING.equals(action)){

                int id = intent.getIntExtra("id", 0);
                final NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationmanager.cancel(id);

                Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                settingIntent.setData(uri);
                startActivity(settingIntent);

            }
        }
    }


    private  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            else {
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }



}



