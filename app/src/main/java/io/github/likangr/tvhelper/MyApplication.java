package io.github.likangr.tvhelper;

import android.app.Application;

/**
 * Created by Likang on 2017/6/6 21:06.
 * email：15034671952@163.com
 */

public class MyApplication extends Application {

    private static MyApplication instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }


    public static Application getContext() {
        return instance;
    }

}
