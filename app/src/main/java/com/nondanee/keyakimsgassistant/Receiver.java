package com.nondanee.keyakimsgassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

    private static final String CLASS_NAME = Constant.PACKAGE_NAME + ".Receiver";

    public static void onLaunch(Context context) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_CHECK);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        Log.d(Constant.DEBUG_TAG, "Receiver onLaunch");
        context.sendBroadcast(intent);
//        enqueueWork(context, intent);
    }

    public static void onText(Context context, String text) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_POPUP);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("text", text);
        Log.d(Constant.DEBUG_TAG, "Receiver onText " + text);
        context.sendBroadcast(intent);
//        enqueueWork(context, intent);
    }

    public static void onMedia(Context context, String id, String url, int mediaType) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_NOTIFY);
        intent.setClassName(Constant.PACKAGE_NAME, CLASS_NAME);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("id", id);
        intent.putExtra("url", url);
        intent.putExtra("mediaType", mediaType);
        Log.d(Constant.DEBUG_TAG, "Receiver onMedia " + mediaType + " " + url);
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(Constant.DEBUG_TAG, "onReceive: " + action);
        if(action.startsWith(Constant.EXTERNAL_MARK)){
            intent.setClass(context, Notifier.class);
            Notifier.enqueueWork(context, intent);
        }
        else if(action.startsWith(Constant.INTERNAL_MARK)){
            intent.setClass(context, Worker.class);
            Worker.enqueueWork(context, intent);
        }
    }
}
