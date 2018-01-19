package com.nondanee.keyakimsgassistant;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


/**
 * Created by Nzix on 2017/9/7.
 */

public class Module implements IXposedHookLoadPackage {

    private final TouchEventHandler touchEventHandler = new TouchEventHandler();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("jp.co.sonymusic.communication.keyakizaka")) {

            XposedBridge.log("KeyakiMsgAssistant Launched");

            // WELCOME AND CHECK
            XposedHelpers.findAndHookMethod("jp.co.sonymusic.communication.keyakizaka.activity.ActivitySplash",
                loadPackageParam.classLoader,
                "a",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        Context context = activity.getApplicationContext();
                        service.launchCheck(context);
                    }
                }
            );

            // PHOTO
//            XposedHelpers.findAndHookMethod("jp.co.sonymusic.communication.keyakizaka.activity.ActivityShowPhoto",
//                loadPackageParam.classLoader,
//                "a",
//                new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Activity activity = (Activity) param.thisObject;
//                        String id = activity.getIntent().getStringExtra("CONTENT_KEY");
//                        String url = activity.getIntent().getStringExtra("CONTENT_URL");
//                        Context context = activity.getApplicationContext();
//                        service.captureResource(context,id,url,1);
//                    }
//                }
//            );

            // PHOTO
            XposedHelpers.findAndHookMethod("jp.co.sonymusic.communication.keyakizaka.activity.ActivityShowPhoto",
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
                            service.captureResource(context,id,url,1);
                        }
                    }
            );

            // VOICE
            XposedHelpers.findAndHookMethod("jp.co.sonymusic.communication.keyakizaka.common.dp",
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
                        service.captureResource(context,id,url,2);
                    }
                }
            );

            // MOVIE
//            XposedHelpers.findAndHookMethod("jp.co.sonymusic.communication.keyakizaka.activity.ActivityPlayMovie",
//                loadPackageParam.classLoader,
//                "a",
//                new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Activity activity = (Activity) param.thisObject;
//                        String id = activity.getIntent().getStringExtra("CONTENT_ID");
//                        String url = activity.getIntent().getStringExtra("CONTENT_URL");
//                        Context context = activity.getApplicationContext();
//                        service.captureResource(context,id,url,3);
//                    }
//                }
//            );

            // MOVIE
            XposedHelpers.findAndHookMethod("jp.co.sonymusic.communication.keyakizaka.activity.ActivityPlayMovie",
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
                            service.captureResource(context,id,url,3);
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
                            if (view instanceof TextView) {
                                touchEventHandler.hookTouchEvent(view, event);
                            }
                        }
                    }
            );


            // PHOTO
//            XposedHelpers.findAndHookMethod("jp.co.sonymusic.communication.keyakizaka.e.c",
//                loadPackageParam.classLoader,
//                "d",
//                String.class,
//                new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        String url = (String) param.args[0];
//                        Context context = AndroidAppHelper.currentApplication().getApplicationContext();
//                        service.captureResource(context,url,1);
//                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    }
//                }
//            );

            // VOICE
//            XposedHelpers.findAndHookMethod("android.media.MediaPlayer",
//                loadPackageParam.classLoader,
//                "setDataSource",
//                String.class,
//                new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        String url = (String) param.args[0];
//                        Context context = AndroidAppHelper.currentApplication().getApplicationContext();
//                        service.captureResource(context,url,2);
//                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    }
//                }
//            );

            // VIDEO
//            XposedHelpers.findAndHookMethod("android.widget.VideoView",
//                loadPackageParam.classLoader,
//                "setVideoPath",
//                String.class,
//                new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        String url = (String) param.args[0];
//                        Context context = AndroidAppHelper.currentApplication().getApplicationContext();
//                        service.captureResource(context,url,3);
//                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    }
//                }
//            );
        }

    }

}

class TouchEventHandler {
    private View currentView;
    private GestureDetector gestureDetector;

    private void detectView(){
        TextView textView = (TextView) currentView;
        int resourceId = textView.getId();
        if (resourceId != View.NO_ID) {
            String viewName = currentView.getContext().getResources().getResourceEntryName(resourceId);
            if (viewName.equals("text") || viewName.equals("singleLineText")) {
                String text = textView.getText().toString();
                service.getText(text, currentView.getContext());
            }
        }
    }

    public void hookTouchEvent(View view, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN){
            currentView = view;
        }
        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
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
                    detectView();
                }
                public boolean onDoubleTap(MotionEvent e) {
                    // TODO Auto-generated method stub
                    detectView();
                    return true;
                }
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    // TODO Auto-generated method stub
                    return false;
                }
            });
        }
        gestureDetector.onTouchEvent(event);
    }
}
