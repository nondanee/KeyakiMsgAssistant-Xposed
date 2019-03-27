package com.nondanee.keyakimsgassistant;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


/**
 * Created by Nzix on 2017/9/7.
 */

public class Sniffer implements IXposedHookLoadPackage {

    private final GestureListener gestureListener = new GestureListener();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("jp.co.sonymusic.communication.keyakizaka")) return;

        XposedBridge.log("KeyakiMsgAssistant Launched");

        XposedHelpers.findAndHookMethod(
            Application.class,
            "onCreate",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                    gestureListener.bind(context);
                    Receiver.onLaunch(context);
                }
            }
        );

//        PHOTO
//        XposedHelpers.findAndHookMethod(
//            "jp.co.sonymusic.communication.keyakizaka.activity.ActivityShowPhoto",
//            loadPackageParam.classLoader,
//            "a",
//            new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    Activity activity = (Activity) param.thisObject;
//                    String id = activity.getIntent().getStringExtra("CONTENT_KEY");
//                    String url = activity.getIntent().getStringExtra("CONTENT_URL");
//                    Context context = activity.getApplicationContext();
//                    Notifier.onMedia(context,id,url,1);
//                }
//            }
//        );

        // PHOTO
        XposedHelpers.findAndHookMethod(
            "jp.co.sonymusic.communication.keyakizaka.activity.ActivityShowPhoto",
            loadPackageParam.classLoader,
            "a",
            Activity.class,
            String.class,
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String id = (String) param.args[1];
                    String url = (String) param.args[2];
                    Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                    Receiver.onMedia(context,id,url,1);
                }
            }
        );

        // MOVIE
//        XposedHelpers.findAndHookMethod(
//            "jp.co.sonymusic.communication.keyakizaka.activity.ActivityPlayMovie",
//            loadPackageParam.classLoader,
//            "a",
//            new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    Activity activity = (Activity) param.thisObject;
//                    String id = activity.getIntent().getStringExtra("CONTENT_ID");
//                    String url = activity.getIntent().getStringExtra("CONTENT_URL");
//                    Context context = activity.getApplicationContext();
//                    Notifier.onMedia(context,id,url,3);
//                }
//            }
//        );

        // MOVIE
        XposedHelpers.findAndHookMethod(
            "jp.co.sonymusic.communication.keyakizaka.activity.ActivityPlayMovie",
            loadPackageParam.classLoader,
            "a",
            Activity.class,
            String.class,
            String.class,
            MediaPlayer.OnPreparedListener.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String id = (String) param.args[1];
                    String url = (String) param.args[2];
                    Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                    Receiver.onMedia(context,id,url,3);
                }
            }
        );

        // VOICE
        XposedHelpers.findAndHookMethod(
            "jp.co.sonymusic.communication.keyakizaka.common.de",
            loadPackageParam.classLoader,
            "a",
            loadPackageParam.classLoader.loadClass("jp.co.sonymusic.communication.keyakizaka.db.dto.TalkInfo"),
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String id = (String) XposedHelpers.callMethod(param.args[0],"getTalkId");
                    String url = (String) param.args[1];
                    Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                    Receiver.onMedia(context,id,url,2);
                }
            }
        );

        //TEXT
        XposedHelpers.findAndHookMethod(
            View.class,
            "dispatchTouchEvent",
            MotionEvent.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    View view = (View) param.thisObject;
                    MotionEvent event = (MotionEvent) param.args[0];
                    gestureListener.onTouchEvent(view, event);
                }
            }
        );

        //SCREENSHOT
        XposedHelpers.findAndHookMethod(
            Window.class,
            "addFlags",
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    int flags = (int) param.args[0];
                    flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                    param.args[0] = flags;
                }
            }
        );

//        PHOTO
//        XposedHelpers.findAndHookMethod(
//            "jp.co.sonymusic.communication.keyakizaka.e.c",
//            loadPackageParam.classLoader,
//            "d",
//            String.class,
//            new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    String url = (String) param.args[0];
//                    Context context = AndroidAppHelper.currentApplication().getApplicationContext();
//                    Notifier.onMedia(context,url,1);
//                }
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                }
//            }
//        );
//
//        VOICE
//        XposedHelpers.findAndHookMethod(
//            "android.media.MediaPlayer",
//            loadPackageParam.classLoader,
//            "setDataSource",
//            String.class,
//            new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    String url = (String) param.args[0];
//                    Context context = AndroidAppHelper.currentApplication().getApplicationContext();
//                    Notifier.onMedia(context,url,2);
//                }
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                }
//            }
//        );
//
//        VIDEO
//        XposedHelpers.findAndHookMethod(
//            "android.widget.VideoView",
//            loadPackageParam.classLoader,
//            "setVideoPath",
//            String.class,
//            new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    String url = (String) param.args[0];
//                    Context context = AndroidAppHelper.currentApplication().getApplicationContext();
//                    Notifier.onMedia(context,url,3);
//                }
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                }
//            }
//        );
    }

    private class GestureListener {

        private GestureDetector detector;
        private View target;

        public void bind(Context context) {
            detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                public boolean onDown(MotionEvent e) {
                    // TODO Auto-generated method stub
                    return false;
                }
                public void onShowPress(MotionEvent e) {
                    // TODO Auto-generated method stub
                }
                public boolean onSingleTapUp(MotionEvent e) {
                    // TODO Auto-generated method stub
                    return false;
                }
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    // TODO Auto-generated method stub
                    return false;
                }
                public void onLongPress(MotionEvent e) {
                    // TODO Auto-generated method stub
                    return;
                }
                public boolean onDoubleTap(MotionEvent e) {
                    // TODO Auto-generated method stub
                    trigger();
                    return true;
                }
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    // TODO Auto-generated method stub
                    return false;
                }
            });
        }

        private void trigger() {
            if (!(target instanceof TextView)) return;
            TextView textView = (TextView) target;
            int resourceId = textView.getId();
            if (resourceId == View.NO_ID) return;
            String viewName = target.getContext().getResources().getResourceEntryName(resourceId);
            if (viewName.equals("text") || viewName.equals("singleLineText")) {
                String text = textView.getText().toString();
                Receiver.onText(target.getContext(), text);
            }
        }

        public void onTouchEvent(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) target = view;
            detector.onTouchEvent(event);
        }

    }

}