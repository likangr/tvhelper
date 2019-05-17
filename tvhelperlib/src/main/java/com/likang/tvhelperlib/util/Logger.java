
package com.likang.tvhelperlib.util;

import android.util.Log;

import com.likang.tvhelperlib.BuildConfig;


/**
 * Logger
 */
public class Logger {

    public static boolean DEBUG = BuildConfig.DEBUG;

    public static void v(String tag, String message) {
        if (DEBUG) {
            Log.v(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUG) {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable tr) {
        if (DEBUG) {
            Log.e(tag, message, tr);
        }
    }
}
