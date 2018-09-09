package com.nondanee.keyakimsgassistant;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Properties;

/**
 * Created by Nzix on 2017/9/9.
 */

public class service extends Service {

    private static final String PACKAGE_NAME = "com.nondanee.keyakimsgassistant";
    private static final String CLASS_NAME = PACKAGE_NAME + ".service";
    private static final String ACTION_NOTIFY = PACKAGE_NAME + ".action.notify";
    private static final String ACTION_CHECK = PACKAGE_NAME + ".action.check";
    private static final String ACTION_DISMISS = ".action.dismiss";
    private static final String ACTION_DOWNLOAD = ".action.download";
    private static final String ACTION_SETTING = ".action.setting";
    private static final String ACTION_COPY= ".action.copy";
    private static final String ALBUM_NAME = "KeyakiMsg";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void launchCheck(Context context) {

        Intent intent = new Intent();
        intent.setAction(ACTION_CHECK);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        context.startService(intent);

    }

    public static void getText(String text, Context context) {

        Intent intent = new Intent();
        intent.setAction(ACTION_COPY);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra("text", text);
        context.startService(intent);

    }

    public static void captureResource(Context context, String id, String url, int mediaType) {

        Intent intent = new Intent();
        intent.setAction(ACTION_NOTIFY);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra("id", id);
        intent.putExtra("url", url);
        intent.putExtra("mediaType", mediaType);
        context.startService(intent);

//        Log.d("xposed_nondanee",url + "---" + mediaType);

    }


    private void permissionPromptNotification(int type){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        final int id = 0;

        if(type == 0){
            builder.setContentTitle(getResources().getString(R.string.welcome));
            builder.setContentText(getResources().getString(R.string.need_storage));
            builder.setSmallIcon(R.drawable.ic_smile);
        }
        else if(type == 1){
            builder.setContentTitle(getResources().getString(R.string.no_storage));
            builder.setContentText(getResources().getString(R.string.please_authorize));
            builder.setSmallIcon(R.drawable.ic_upset);
        }

        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(false);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setOngoing(false);

        Intent rejectIntent = new Intent(this,intentService.class);
        rejectIntent.setAction(ACTION_DISMISS);
        rejectIntent.putExtra("id",id);
        PendingIntent rejectPendingIntent = PendingIntent.getService(this,
                id,
                rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_reject, getResources().getString(R.string.reject), rejectPendingIntent);


        Intent settingIntent = new Intent(this,intentService.class);
        settingIntent.setAction(ACTION_SETTING);
        settingIntent.putExtra("id",id);
        PendingIntent settingPendingIntent = PendingIntent.getService(this,
                id,
                settingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(settingPendingIntent);
        builder.setFullScreenIntent(settingPendingIntent, true);
        builder.addAction(R.drawable.ic_sure, getResources().getString(R.string.authorize), settingPendingIntent);

        final NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.notify(id, builder.build());

//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                notificationmanager.cancel(id);
//            }
//        };
//        Timer timer = new Timer();
//        timer.schedule( task , 10*1000 );//10 seconds

    }


    private void permissionPromptDialog(int type){
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setIcon(R.mipmap.ic_launcher);
        if(type == 0){
            builder.setTitle(getResources().getString(R.string.welcome));
            builder.setMessage(getResources().getString(R.string.need_storage));
        }
        else if(type == 1){
            builder.setTitle(getResources().getString(R.string.no_storage));
            builder.setMessage(getResources().getString(R.string.please_authorize));
        }
        builder.setNegativeButton(getResources().getString(R.string.authorize), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            Intent settingIntent = new Intent(service.this,intentService.class);
            settingIntent.setAction(ACTION_SETTING);
            startService(settingIntent);
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.reject), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();

    }

    public void copyPrompt(final String text){
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setMessage(text);
        builder.setNegativeButton(getResources().getString(R.string.copy), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                copyText(text);
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.dismiss), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

    public void copyText(String text)
    {
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(this.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("message", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, getResources().getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
    }


    private void downloadPromptNotification(String url, int mediaType, String fileName){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

        String title = null;
        if(mediaType == 1){
            title = getResources().getString(R.string.photo_prompt);
            if (Build.VERSION.SDK_INT >= 21) {
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_colored));
            }
            else{
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo));
            }
//            builder.setColor(ContextCompat.getColor(this, R.color.colorPhoto));
        }
        else if(mediaType == 2){
            title = getResources().getString(R.string.audio_prompt);
            if (Build.VERSION.SDK_INT >= 21) {
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_voice_colored));
            }
            else{
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_voice));
            }
//            builder.setColor(ContextCompat.getColor(this, R.color.colorVoice));
        }
        else if(mediaType == 3){
            title = getResources().getString(R.string.video_prompt);
            if (Build.VERSION.SDK_INT >= 21) {
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_movie_colored));
            }
            else{
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_movie));
            }
//            builder.setColor(ContextCompat.getColor(this, R.color.colorMovie));
        }

        final int id = 1;

        builder.setSmallIcon(R.drawable.ic_notify);
        builder.setContentTitle(title);
        builder.setContentText(url);
        builder.setPriority(Notification.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= 21) builder.setVibrate(new long[0]);
        builder.setAutoCancel(true);
//        builder.setOngoing(false);
//        builder.setOnlyAlertOnce(false);

        //nop
        PendingIntent nopPendingIntent = PendingIntent.getActivity(this,
                id,
                new Intent(),
                0);

        builder.setContentIntent(nopPendingIntent);
//        builder.setFullScreenIntent(nopPendingIntent,true);


        //dismiss
        Intent dismissIntent = new Intent(this,intentService.class);
        dismissIntent.setAction(ACTION_DISMISS);
        dismissIntent.putExtra("id",id);
        PendingIntent dismissPendingIntent = PendingIntent.getService(this,
                id,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_ignore, getResources().getString(R.string.dismiss), dismissPendingIntent);

        //open
//        Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        PendingIntent openPendingIntent = PendingIntent.getActivity(this,
//                id,
//                openIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.addAction(R.drawable.ic_browser, getResources().getString(R.string.open), openPendingIntent);

        //download
        Intent downloadIntent = new Intent(this,intentService.class);
        downloadIntent.setAction(ACTION_DOWNLOAD);
        downloadIntent.putExtra("url",url);
        downloadIntent.putExtra("fileName",fileName);
        downloadIntent.putExtra("id",id);
        PendingIntent downloadPendingIntent = PendingIntent.getService(this,
                id,
                downloadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_download, getResources().getString(R.string.download), downloadPendingIntent);


        final NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.notify(id, builder.build());

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                notificationmanager.cancel(id);
            }
        };
        Timer timer = new Timer();
        timer.schedule( task , 5*1000 );//5 seconds

    }

    public void downloadPromptDialog(final String url, int mediaType, final String fileName){
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        if(mediaType == 1){
            builder.setTitle(getResources().getString(R.string.photo_prompt));
            builder.setIcon(R.drawable.ic_photo_colored);
        }
        else if(mediaType == 2){
            builder.setTitle(getResources().getString(R.string.audio_prompt));
            builder.setIcon(R.drawable.ic_voice_colored);
        }
        else if(mediaType == 3){
            builder.setTitle(getResources().getString(R.string.video_prompt));
            builder.setIcon(R.drawable.ic_movie_colored);
        }
//        builder.setMessage(url);
        builder.setNegativeButton(getResources().getString(R.string.download), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent downloadIntent = new Intent(service.this,intentService.class);
                downloadIntent.setAction(ACTION_DOWNLOAD);
                downloadIntent.putExtra("url",url);
                downloadIntent.putExtra("fileName",fileName);
                startService(downloadIntent);
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.dismiss), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean isMiuiRom = util.isMiuiRom();

        if (intent != null) {

            String action = intent.getAction();

            if (ACTION_CHECK.equals(action)) {

                if(!util.isStoragePermissionGranted(this)) {
                    if (isMiuiRom) {
                        permissionPromptDialog(0);
                    }
                    else {
                        permissionPromptNotification(0);
                    }
                }
            }

            else if (ACTION_COPY.equals(action)) {
                String text = intent.getStringExtra("text");
                copyPrompt(text);
            }

            else if (ACTION_NOTIFY.equals(action)) {

                if(!util.isStoragePermissionGranted(this)) {
                    if (isMiuiRom) {
                        permissionPromptDialog(1);
                    }
                    else {
                        permissionPromptNotification(1);
                    }
                }
                else {
                    String url = intent.getStringExtra("url");
                    int mediaType = intent.getIntExtra("mediaType", 1);

                    String fileName = url.replaceAll("\\?\\S+$", "");
                    fileName = fileName.replaceAll("^\\S+/", "");

                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + ALBUM_NAME + "/" + fileName);
                    if (!file.exists()) {
                        if(isMiuiRom){
                            downloadPromptDialog(url, mediaType, fileName);
                        }
                        else {
                            downloadPromptNotification(url, mediaType, fileName);
                        }
                    }
                }
            }

        }
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
