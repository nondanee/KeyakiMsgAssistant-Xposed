package com.nondanee.keyakimsgassistant;

public class Constant {
    public static final String PACKAGE_NAME = "com.nondanee.keyakimsgassistant";

    public static final String EXTERNAL_PREFIX = ".call";
    public static final String ACTION_NOTIFY = EXTERNAL_PREFIX + ".notify";
    public static final String ACTION_CHECK = EXTERNAL_PREFIX + ".check";
    public static final String ACTION_POPUP = EXTERNAL_PREFIX + ".popup";

    public static final String INTERNAL_PREFIX = ".command";
    public static final String ACTION_DISMISS = INTERNAL_PREFIX + ".dismiss";
    public static final String ACTION_DOWNLOAD = INTERNAL_PREFIX + ".download";
    public static final String ACTION_SETTING = INTERNAL_PREFIX + ".setting";
    public static final String ACTION_COPY = INTERNAL_PREFIX + ".copy";

    public static final String ALBUM_NAME = "KeyakiMsg";

    public static final String CHANNEL_DEFAULT_ID = "default";
    public static final String CHANNEL_DEFAULT_NAME = "Default channel";
    public static final String CHANNEL_PERMISSION_ID = "permission";
    public static final String CHANNEL_PERMISSION_NAME = "Permission request";
    public static final String CHANNEL_RESOURCE_ID = "resource";
    public static final String CHANNEL_RESOURCE_NAME = "Resource notify";

    public static final String DEBUG_TAG = "xposed_nondanee";
}
