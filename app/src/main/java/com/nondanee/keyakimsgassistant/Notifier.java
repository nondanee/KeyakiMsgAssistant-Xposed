package com.nondanee.keyakimsgassistant;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nzix on 2017/9/9.
 */

public class Notifier extends Service {

    private static final String CLASS_NAME = Constant.PACKAGE_NAME + ".Notifier";
    private static final Integer COLORLESS_ICON_SET[] = {R.drawable.ic_photo, R.drawable.ic_voice, R.drawable.ic_movie};
    private static final Integer COLORED_ICON_SET[] = {R.drawable.ic_photo_colored, R.drawable.ic_voice_colored, R.drawable.ic_movie_colored};
    private static final Integer PROMPT_TEXT_SET[] = {R.string.photo_prompt, R.string.audio_prompt, R.string.video_prompt};
    private static final Integer COLOR_SET[] = {R.color.colorPhoto, R.color.colorVoice, R.color.colorMovie};

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void onLaunch(Context context) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_CHECK);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            context.startForegroundService(intent);
//        else
//            context.startService(intent);
        Log.d(Constant.DEBUG_TAG, "onLaunch");
    }

    public static void onText(Context context, String text) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_COPY);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        intent.putExtra("text", text);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            context.startForegroundService(intent);
//        else
//            context.startService(intent);
        Log.d(Constant.DEBUG_TAG, "onText " + text);
    }

    public static void onMedia(Context context, String id, String url, int mediaType) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_NOTIFY);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        intent.putExtra("id", id);
        intent.putExtra("url", url);
        intent.putExtra("mediaType", mediaType);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            context.startForegroundService(intent);
//        else
//            context.startService(intent);
        Log.d(Constant.DEBUG_TAG, "onMedia " + mediaType + " " + url);
    }

    private void requestPromptNotification(String permission) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constant.CHANNEL_PERMISSION);
        final int id = permission.equals("storage") ? 0 : 1;

        builder.setSmallIcon(R.drawable.ic_smile);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(getResources().getString(permission.equals("storage") ? R.string.need_storage : R.string.need_overlay));
        builder.setContentText(getResources().getString(R.string.please_authorize));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constant.CHANNEL_PERMISSION, "Permission request", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setVibrationPattern(new long[0]);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setOngoing(false);
        builder.setOnlyAlertOnce(false);
        builder.setAutoCancel(true);

        Intent rejectIntent = new Intent(this, Worker.class);
        rejectIntent.setAction(Constant.ACTION_DISMISS);
        rejectIntent.putExtra("id", id);
        PendingIntent rejectPendingIntent = PendingIntent.getService(this, id, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_reject, getResources().getString(R.string.reject), rejectPendingIntent);

        Intent settingIntent = new Intent(this, Worker.class);
        settingIntent.setAction(Constant.ACTION_SETTING);
        settingIntent.putExtra("id", id);
        settingIntent.putExtra("permission", permission);
        PendingIntent settingPendingIntent = PendingIntent.getService(this, id, settingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(settingPendingIntent);
        builder.setFullScreenIntent(settingPendingIntent, true);
        builder.addAction(R.drawable.ic_sure, getResources().getString(R.string.authorize), settingPendingIntent);

        notificationManager.notify(id, builder.build());

//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                notificationManager.cancel(id);
//            }
//        };
//        Timer timer = new Timer();
//        timer.schedule( task , 10*1000 );
    }

    private void capturePromptNotification(String url, int mediaType, String fileName) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constant.CHANNEL_RESOURCE);
        final int id = 100;

        builder.setSmallIcon(R.drawable.ic_notify);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? COLORED_ICON_SET : COLORLESS_ICON_SET)[mediaType - 1]));
        builder.setContentTitle(getResources().getString(PROMPT_TEXT_SET[mediaType - 1]));
        builder.setContentText(url);
//        builder.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
//        builder.setColor(ContextCompat.getColor(this, COLOR_SET[mediaType - 1]));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constant.CHANNEL_RESOURCE, "Resource notify", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setVibrationPattern(new long[0]);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder.setVibrate(new long[0]);
        }

        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
//        builder.setOngoing(false);
//        builder.setOnlyAlertOnce(false);

        //dismiss
        final Intent dismissIntent = new Intent(this, Worker.class);
        dismissIntent.setAction(Constant.ACTION_DISMISS);
        dismissIntent.putExtra("id", id);
        PendingIntent dismissPendingIntent = PendingIntent.getService(this, id, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_ignore, getResources().getString(R.string.dismiss), dismissPendingIntent);

        //download
        final Intent downloadIntent = new Intent(this, Worker.class); // new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        downloadIntent.setAction(Constant.ACTION_DOWNLOAD);
        downloadIntent.putExtra("url", url);
        downloadIntent.putExtra("fileName", fileName);
        downloadIntent.putExtra("id", id);
        PendingIntent downloadPendingIntent = PendingIntent.getService(this, id, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_download, getResources().getString(R.string.download), downloadPendingIntent);

        //default
        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, new Intent(), 0);
        builder.setContentIntent(pendingIntent);
//        builder.setFullScreenIntent(pendingIntent, true);

        notificationManager.notify(id, builder.build());

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                notificationManager.cancel(id);
            }
        };
        Timer timer = new Timer();
        timer.schedule( task , 5*1000 );
    }

//    public void downloadPromptDialog(final String url, int mediaType, final String fileName){
//        if(!Checker.coverlayDrawable(this)) {
//            requestPromptNotification("overlay");
//            return;
//        }
//
//        final Intent intent = new Intent(this, Worker.class);
//        intent.setAction(Constant.ACTION_DOWNLOAD);
//        intent.putExtra("url", url);
//        intent.putExtra("fileName", fileName);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
//        builder.setTitle(getResources().getString(PROMPT_TEXT_SET[mediaType - 1]));
//        builder.setIcon(COLORED_ICON_SET[mediaType - 1]);
////        builder.setMessage(url);
//        builder.setNegativeButton(getResources().getString(R.string.download), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startService(intent);
//            }
//        });
//        builder.setPositiveButton(getResources().getString(R.string.dismiss), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        final AlertDialog alertDialog = builder.create();
//        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        alertDialog.show();
//    }

    public void copyPromptDialog(final String text){
        if(!Checker.coverlayDrawable(this)) {
            requestPromptNotification("overlay");
            return;
        }

        final Intent intent = new Intent(this, Worker.class);
        intent.setAction(Constant.ACTION_COPY);
        intent.putExtra("text", text);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setMessage(text);
        builder.setNegativeButton(getResources().getString(R.string.copy), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startService(intent);
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

        if (intent != null) {
            String action = intent.getAction();
            if (Constant.ACTION_CHECK.equals(action)) {
                if (!Checker.storageAccessible(this)) {
                    requestPromptNotification("storage");
                }
                else if (!Checker.coverlayDrawable((this))) {
                    requestPromptNotification("overlay");
                }
            }
            else if (Constant.ACTION_COPY.equals(action)) {
                String text = intent.getStringExtra("text");
                copyPromptDialog(text);
            }
            else if (Constant.ACTION_NOTIFY.equals(action)) {
                if (!Checker.storageAccessible(this)) {
                    requestPromptNotification("storage");
                }
                else {
                    String url = intent.getStringExtra("url");
                    int mediaType = intent.getIntExtra("mediaType", 1);
                    String fileName = url.replaceAll("\\?\\S+$", "").replaceAll("^\\S+/", "");

                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + Constant.ALBUM_NAME + "/" + fileName);
                    if (!file.exists()) capturePromptNotification(url, mediaType, fileName);
                }
            }
        }

        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
