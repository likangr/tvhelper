package com.likang.tvhelperlib.core;

import android.app.Application;

import com.likang.tvhelperlib.util.ApplicationHolder;
import com.likang.tvhelperlib.util.ScreenUtil;

/**
 * @author likangren
 */
public class TvHelper {
    private static final String TAG = "TvHelper";
    private static final Object sInitialiseLock = new Object();
    private static boolean sIsInitialised = false;

    /*****open aip****/

    /**
     * @param application
     */
    public static void initCore(Application application) {
        synchronized (sInitialiseLock) {
            if (sIsInitialised) {
                return;
            }
            ApplicationHolder.init(application);
            ScreenUtil.adaptDensity(application, 720, 1280, true, ScreenUtil.MODE_ADAPT_TWO_SIDE);
            sIsInitialised = true;
        }
    }
}
