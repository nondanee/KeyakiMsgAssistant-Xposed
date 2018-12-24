package com.nondanee.keyakimsgassistant;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nzix on 2017/9/9.
 */

public class Notifier extends BroadcastReceiver {

    private static final String CLASS_NAME = Constant.PACKAGE_NAME + ".Notifier";
    private static final Integer COLORLESS_ICON_SET[] = {R.drawable.ic_photo, R.drawable.ic_voice, R.drawable.ic_movie};
    private static final Integer COLORED_ICON_SET[] = {R.drawable.ic_photo_colored, R.drawable.ic_voice_colored, R.drawable.ic_movie_colored};
    private static final Integer PROMPT_TEXT_SET[] = {R.string.photo_prompt, R.string.audio_prompt, R.string.video_prompt};
    private static final Integer COLOR_SET[] = {R.color.colorPhoto, R.color.colorVoice, R.color.colorMovie};

    private void requestPromptNotification(Context context, String permission) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constant.CHANNEL_PERMISSION);
        final int id = permission.equals("storage") ? 0 : 1;

        builder.setSmallIcon(R.drawable.ic_smile);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(context.getResources().getString(permission.equals("storage") ? R.string.need_storage : R.string.need_overlay));
        builder.setContentText(context.getResources().getString(R.string.please_authorize));

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

        Intent rejectIntent = new Intent(context, Worker.class);
        rejectIntent.setAction(Constant.ACTION_DISMISS);
        rejectIntent.putExtra("id", id);
        PendingIntent rejectPendingIntent = PendingIntent.getService(context, id, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_reject, context.getResources().getString(R.string.reject), rejectPendingIntent);

        Intent settingIntent = new Intent(context, Worker.class);
        settingIntent.setAction(Constant.ACTION_SETTING);
        settingIntent.putExtra("id", id);
        settingIntent.putExtra("permission", permission);
        PendingIntent settingPendingIntent = PendingIntent.getService(context, id, settingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(settingPendingIntent);
        builder.setFullScreenIntent(settingPendingIntent, true);
        builder.addAction(R.drawable.ic_sure, context.getResources().getString(R.string.authorize), settingPendingIntent);

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

    private void capturePromptNotification(Context context, String url, int mediaType, String fileName) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constant.CHANNEL_RESOURCE);
        final int id = 100;

        builder.setSmallIcon(R.drawable.ic_notify);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? COLORED_ICON_SET : COLORLESS_ICON_SET)[mediaType - 1]));
        builder.setContentTitle(context.getResources().getString(PROMPT_TEXT_SET[mediaType - 1]));
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
        final Intent dismissIntent = new Intent(context, Worker.class);
        dismissIntent.setAction(Constant.ACTION_DISMISS);
        dismissIntent.putExtra("id", id);
        PendingIntent dismissPendingIntent = PendingIntent.getService(context, id, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_ignore, context.getResources().getString(R.string.dismiss), dismissPendingIntent);

        //download
        final Intent downloadIntent = new Intent(context, Worker.class); // new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        downloadIntent.setAction(Constant.ACTION_DOWNLOAD);
        downloadIntent.putExtra("url", url);
        downloadIntent.putExtra("fileName", fileName);
        downloadIntent.putExtra("id", id);
        PendingIntent downloadPendingIntent = PendingIntent.getService(context, id, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_download, context.getResources().getString(R.string.download), downloadPendingIntent);

        //default
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, new Intent(), 0);
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

    private void copyPromptDialog(final Context context, final String text){
        final Intent intent = new Intent(context, Worker.class);
        intent.setAction(Constant.ACTION_COPY);
        intent.putExtra("text", text);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setMessage(text);
        builder.setNegativeButton(context.getResources().getString(R.string.copy), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startService(intent);
            }
        });
        builder.setPositiveButton(context.getResources().getString(R.string.dismiss), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

    private void distributor(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        Log.d(Constant.DEBUG_TAG, "onDistribute " + action);

        if (Constant.ACTION_CHECK.equals(action)) {
            if (!Checker.storageAccessible(context)) {
                requestPromptNotification(context, "storage");
            }
            if (!Checker.coverlayDrawable(context)) {
                requestPromptNotification(context, "overlay");
            }
        }
        else if (Constant.ACTION_POPUP.equals(action)) {
            if(!Checker.coverlayDrawable(context)) {
                requestPromptNotification(context, "overlay");
            }
            else {
                String text = intent.getStringExtra("text");
                copyPromptDialog(context, text);
            }
        }
        else if (Constant.ACTION_NOTIFY.equals(action)) {
            if (!Checker.storageAccessible(context)) {
                requestPromptNotification(context, "storage");
            }
            else {
                String url = intent.getStringExtra("url");
                int mediaType = intent.getIntExtra("mediaType", 1);
                String fileName = url.replaceAll("\\?\\S+$", "").replaceAll("^\\S+/", "");

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + Constant.ALBUM_NAME + "/" + fileName);
                if (!file.exists()) capturePromptNotification(context, url, mediaType, fileName);
            }
        }
    }

    public static void onLaunch(Context context) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_CHECK);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        Log.d(Constant.DEBUG_TAG, "onLaunch");
        context.sendBroadcast(intent);
    }

    public static void onText(Context context, String text) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_POPUP);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        intent.putExtra("text", text);
        Log.d(Constant.DEBUG_TAG, "onText " + text);
        context.sendBroadcast(intent);
    }

    public static void onMedia(Context context, String id, String url, int mediaType) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_NOTIFY);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        intent.putExtra("id", id);
        intent.putExtra("url", url);
        intent.putExtra("mediaType", mediaType);
        Log.d(Constant.DEBUG_TAG, "onMedia " + mediaType + " " + url);
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(Constant.DEBUG_TAG, "onReceive");
        distributor(context, intent);
    }
}
