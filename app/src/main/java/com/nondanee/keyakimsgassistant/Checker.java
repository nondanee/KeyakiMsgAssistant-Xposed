package com.nondanee.keyakimsgassistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Nzix on 2018/1/7.
 */

public class Checker {

    public static boolean storageAccessible(Context context) {
        if (Build.VERSION.SDK_INT < 23) return true;
        else if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) return true;
        else return false;
    }

    public static boolean overlayDrawable(Context context) {
        if (Build.VERSION.SDK_INT < 23) return true;
        else if (Settings.canDrawOverlays(context)) return true;
        else return false;
    }

    public static void storagePermissionRequest(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void overlayPermissionRequest(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static Notification serviceHolder(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(Constant.CHANNEL_DEFAULT_ID, Constant.CHANNEL_DEFAULT_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setVibrationPattern(new long[]{ 0 });
            notificationChannel.enableVibration(true);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Notification notification = new NotificationCompat.Builder(context, Constant.CHANNEL_PERMISSION_ID)
            .setSmallIcon(R.drawable.ic_notify)
            .setVibrate(new long[0])
            .setPriority(Notification.PRIORITY_MIN)
            .build();
        Log.d(Constant.DEBUG_TAG, "serviceHolder: should be no vibration");
        return notification;
    }

}
