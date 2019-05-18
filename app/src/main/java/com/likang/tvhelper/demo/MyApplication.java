package com.likang.tvhelper.demo;

import android.app.Application;

import com.likang.tvhelper.lib.core.TvHelper;

/**
 * @author Likang
 * @date 2017/6/6 21:06
 * email：15034671952@163.com
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TvHelper.initCore(this);
    }

}
