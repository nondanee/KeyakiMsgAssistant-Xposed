package com.nondanee.keyakimsgassistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Nzix on 2018/1/7.
 */

public class Checker {

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    public static boolean isMiuiRom() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null ||
                prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null ||
                prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null ||
                false;
    }

    public static boolean storageAccessible(Context content) {
        if (Build.VERSION.SDK_INT < 23) return true;
        else if (ContextCompat.checkSelfPermission(content,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) return true;
        else return false;
    }

    public static boolean coverlayDrawable(Context context) {
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

    public static void OverlayPermissionRequest(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
