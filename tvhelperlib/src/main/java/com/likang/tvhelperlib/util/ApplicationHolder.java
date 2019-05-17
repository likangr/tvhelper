package com.likang.tvhelperlib.util;

import android.app.Application;

/**
 * @author likangren
 */
public class ApplicationHolder {
    private static final String TAG = "ApplicationHolder";

    private static Application APPLICATION;

    public static void init(Application application) {
        APPLICATION = application;
    }

    public static Application getApplication() {
        return APPLICATION;
    }
}
