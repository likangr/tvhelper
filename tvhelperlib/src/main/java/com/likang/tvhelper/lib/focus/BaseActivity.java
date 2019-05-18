package com.likang.tvhelper.lib.focus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.likang.tvhelper.lib.util.ScreenUtil;


/**
 * @author Likang
 * @date 2017/6/6 21:06
 * email：15034671952@163.com
 */
public abstract class BaseActivity extends Activity {
    private ViewFocusHandler mViewFocusHandler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewFocusHandler = new ViewFocusHandler(getContentView());
    }


    @Override
    public void setContentView(int layoutResID) {
        FrameLayout fv = new FocusBehaviourHandlerView(this);
        View.inflate(this, layoutResID, fv);
        super.setContentView(fv);

    }

    @Override
    public void setContentView(View view) {
        this.setContentView(view, null);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        FrameLayout fv = new FocusBehaviourHandlerView(this);
        if (params != null) {
            fv.addView(view, params);
        } else {
            fv.addView(view);
        }
        super.setContentView(fv);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewFocusHandler.stopFocusHandle();
    }


    public View getContentView() {
        return getWindow().getDecorView().findViewById(android.R.id.content);
    }

    public ViewFocusHandler getViewFocusHandler() {
        return mViewFocusHandler;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mViewFocusHandler.onWindowFocusChanged(hasFocus);
        ScreenUtil.updateConfig(this, getResources());
    }
}
