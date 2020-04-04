package com.nondanee.keyakimsgassistant;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nzix on 2017/9/9.
 */

public class Notifier extends JobIntentService {

    private static final String CLASS_NAME = Constant.PACKAGE_NAME + ".Notifier";
    private static final Integer COLORLESS_ICON_SET[] = { R.drawable.ic_photo, R.drawable.ic_voice, R.drawable.ic_movie };
    private static final Integer COLORED_ICON_SET[] = { R.drawable.ic_photo_colored, R.drawable.ic_voice_colored, R.drawable.ic_movie_colored };
    private static final Integer PROMPT_TEXT_SET[] = { R.string.photo_prompt, R.string.audio_prompt, R.string.video_prompt };
    private static final Integer COLOR_SET[] = { R.color.colorPhoto, R.color.colorVoice, R.color.colorMovie };

    private void bindNotificationChannel(NotificationManager notificationManager, String id, CharSequence name, int importance) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
        notificationChannel.setVibrationPattern(new long[]{ 0 });
        notificationChannel.enableVibration(true);
        notificationChannel.setSound(null, null);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private void requestPromptNotification(Context context, String permission) {
        final int id = permission.equals("storage") ? 100 : 200;
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        bindNotificationChannel(notificationManager, Constant.CHANNEL_PERMISSION_ID, Constant.CHANNEL_PERMISSION_NAME, NotificationManager.IMPORTANCE_HIGH);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constant.CHANNEL_PERMISSION_ID)
            .setSmallIcon(R.drawable.ic_smile)
//            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
            .setContentTitle(context.getResources().getString(permission.equals("storage") ? R.string.need_storage : R.string.need_overlay))
            .setContentText(context.getResources().getString(R.string.please_authorize))

            .setPriority(Notification.PRIORITY_HIGH)
            .setOngoing(false)
            .setOnlyAlertOnce(false)
            .setAutoCancel(true);

        Intent rejectIntent = new Intent(context, Receiver.class)
            .setAction(Constant.ACTION_DISMISS)
            .putExtra("id", id);
        PendingIntent rejectPendingIntent = PendingIntent.getBroadcast(context, id, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent settingIntent = new Intent(context, Receiver.class)
            .setAction(Constant.ACTION_SETTING)
            .putExtra("id", id)
            .putExtra("permission", permission);
        PendingIntent settingPendingIntent = PendingIntent.getBroadcast(context, id, settingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder
            .setContentIntent(settingPendingIntent)
            .setFullScreenIntent(settingPendingIntent, true)
            .addAction(R.drawable.ic_reject, context.getResources().getString(R.string.reject), rejectPendingIntent)
            .addAction(R.drawable.ic_sure, context.getResources().getString(R.string.authorize), settingPendingIntent);

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
        final int id = 10;
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        bindNotificationChannel(notificationManager, Constant.CHANNEL_RESOURCE_ID, Constant.CHANNEL_RESOURCE_NAME, NotificationManager.IMPORTANCE_HIGH);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constant.CHANNEL_RESOURCE_ID)
            .setSmallIcon(R.drawable.ic_notify)
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? COLORED_ICON_SET : COLORLESS_ICON_SET)[mediaType - 1]))
            .setContentTitle(context.getResources().getString(PROMPT_TEXT_SET[mediaType - 1]))
            .setContentText(url)
//            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
//            .setColor(ContextCompat.getColor(context, COLOR_SET[mediaType - 1]))

            .setVibrate(new long[0])
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true);
//            .setOngoing(false)
//            .setOnlyAlertOnce(false)

        final Intent dismissIntent = new Intent(context, Receiver.class);
        dismissIntent.setAction(Constant.ACTION_DISMISS)
            .putExtra("id", id);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, id, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Intent downloadIntent = new Intent(context, Receiver.class) // new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            .setAction(Constant.ACTION_DOWNLOAD)
            .putExtra("url", url)
            .putExtra("fileName", fileName)
            .putExtra("id", id);
        PendingIntent downloadPendingIntent = PendingIntent.getBroadcast(context, id, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, new Intent(), 0);

        builder
            .setContentIntent(pendingIntent)
//            .setFullScreenIntent(pendingIntent, true)
            .addAction(R.drawable.ic_ignore, context.getResources().getString(R.string.dismiss), dismissPendingIntent)
            .addAction(R.drawable.ic_download, context.getResources().getString(R.string.download), downloadPendingIntent);

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

//    public void downloadPromptDialog(final String url, int mediaType, final String fileName) {
//        if (!Checker.overlayDrawable(this)) {
//            requestPromptNotification(this, "overlay");
//            return;
//        }
//
//        final Intent intent = new Intent(this, Worker.class)
//            .setAction(Constant.ACTION_DOWNLOAD)
//            .putExtra("url", url)
//            .putExtra("fileName", fileName);
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

    private void copyPromptDialog(final Context context, final String text) {
        final Intent intent = new Intent(context, Worker.class)
            .setAction(Constant.ACTION_COPY)
            .putExtra("text", text);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setMessage(text);
        builder.setNegativeButton(context.getResources().getString(R.string.copy), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Worker.enqueueWork(context, intent);
            }
        });
        builder.setPositiveButton(context.getResources().getString(R.string.dismiss), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }
        });
    }

    private void distributor(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        Log.d(Constant.DEBUG_TAG, "onDistribute " + action);

        if (Constant.ACTION_CHECK.equals(action)) {
            if (!Checker.storageAccessible(context)) {
                requestPromptNotification(context, "storage");
            }
            if (!Checker.overlayDrawable(context)) {
                requestPromptNotification(context, "overlay");
            }
        }
        else if (Constant.ACTION_POPUP.equals(action)) {
            if (!Checker.overlayDrawable(context)) {
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

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constant.ALBUM_NAME + File.separator + fileName);
                if (!file.exists()) capturePromptNotification(context, url, mediaType, fileName);
            }
        }
    }

    public static void onLaunch(Context context) {
        Intent intent = new Intent()
            .setAction(Constant.ACTION_CHECK)
            .setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        Log.d(Constant.DEBUG_TAG, "onLaunch");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            context.startForegroundService(intent);
//        else
//            context.startService(intent);
        enqueueWork(context, intent);
    }

    public static void onText(Context context, String text) {
        Intent intent = new Intent()
            .setAction(Constant.ACTION_POPUP)
            .setClassName(Constant.PACKAGE_NAME, CLASS_NAME)
            .putExtra("text", text);
        Log.d(Constant.DEBUG_TAG, "onText " + text);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            context.startForegroundService(intent);
//        else
//            context.startService(intent);
        enqueueWork(context, intent);
    }

    public static void onMedia(Context context, String id, String url, int mediaType) {
        Intent intent = new Intent()
            .setAction(Constant.ACTION_NOTIFY)
            .setClassName(Constant.PACKAGE_NAME, CLASS_NAME)
            .putExtra("id", id)
            .putExtra("url", url)
            .putExtra("mediaType", mediaType);
        Log.d(Constant.DEBUG_TAG, "onMedia " + mediaType + " " + url);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            context.startForegroundService(intent);
//        else
//            context.startService(intent);
        enqueueWork(context, intent);
    }

    public static final int JOB_ID = 100;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, Notifier.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        distributor(this, intent);
    }

//    public Notifier() {
//        super("Notifier");
//    }
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        distributor(this, intent);
//    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        distributor(this, intent);
//        stopSelf(startId);
//        return START_NOT_STICKY;
//    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Notification notification = Checker.serviceHolder(this);
//        startForeground(1, notification);
//    }

}
