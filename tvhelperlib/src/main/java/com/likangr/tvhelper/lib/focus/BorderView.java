package com.likangr.tvhelper.lib.focus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.likangr.tvhelper.lib.util.ApplicationHolder;
import com.likangr.tvhelper.lib.util.Logger;
import com.likangr.tvhelper.lib.util.ScreenUtil;


/**
 * @author Likang
 * @date 2017/6/12 21:44
 * email：15034671952@163.com
 * focused view's decoration;
 */
public class BorderView extends View {
    private String TAG = BorderView.class.getSimpleName();
    private float mStrokeWidth = ScreenUtil.dip2px(ApplicationHolder.getApplication(), 1f);
    private BorderParams mBorderParams = ViewFocusAppearance.DEFAULT_BORDER_PARAMS;
    private Paint mPaint = new Paint();
    private RectF mRectF = new RectF();
    private int[] mGradientColors;
    private int mGradientRadius;

    public BorderView(Context context) {
        super(context);
        setParams();
    }

    private void setParams() {
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(mStrokeWidth);

        mPaint.setStyle(Paint.Style.STROKE);
        mGradientRadius = (int) (mBorderParams.shadowWidth / mStrokeWidth + 0.5);
        Logger.d(TAG, "mGradientRadius:" + mGradientRadius);
        Logger.d(TAG, "mStrokeWidth:" + mStrokeWidth);
        Logger.d(TAG, "shadowWidth:" + mBorderParams.shadowWidth);
        computeGradientColors();
    }

    private void computeGradientColors() {
        mGradientColors = new int[mGradientRadius];
        int argb = mBorderParams.shadowColor;
        Logger.d(TAG, "parseInt:" + argb);
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        for (int i = 0; i < mGradientRadius; i++) {
            int tempA = a - (a / (mGradientRadius - 1)) * i;
            mGradientColors[i] = Color.argb(tempA, r, g, b);
            Logger.d(TAG, "parseARGB-->" + tempA + "," + r + "," + g + "," + b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mGradientRadius; i++) {
            mRectF.set(getLeft() + BorderView.BorderParams.SHADOW_MAX_WIDTH - i * mStrokeWidth,
                    getTop() + BorderView.BorderParams.SHADOW_MAX_WIDTH - i * mStrokeWidth,
                    getRight() - BorderView.BorderParams.SHADOW_MAX_WIDTH + i * mStrokeWidth,
                    getBottom() - BorderView.BorderParams.SHADOW_MAX_WIDTH + i * mStrokeWidth);
            mPaint.setColor(mGradientColors[i]);
//            mPath.addRoundRect(mRectF, st.mRadiusArray, Path.Direction.CW);

            if (mBorderParams.rx == BorderView.BorderParams.SHADOW_RADIUS_CIRCULAR && mBorderParams.ry == BorderView.BorderParams.SHADOW_RADIUS_CIRCULAR) {
                float rad = Math.min(mRectF.width(), mRectF.height()) * 0.5f;
                canvas.drawRoundRect(mRectF, rad, rad, mPaint);
            } else if (mBorderParams.rx == BorderView.BorderParams.SHADOW_RADIUS_CIRCULAR) {
                float rad = Math.min(mRectF.width(), mRectF.height()) * 0.5f;
                canvas.drawRoundRect(mRectF, rad, mBorderParams.ry == 0 ? 0 : mBorderParams.ry + mStrokeWidth * i, mPaint);
            } else if (mBorderParams.ry == BorderView.BorderParams.SHADOW_RADIUS_CIRCULAR) {
                float rad = Math.min(mRectF.width(), mRectF.height()) * 0.5f;
                canvas.drawRoundRect(mRectF, mBorderParams.rx == 0 ? 0 : mBorderParams.rx + mStrokeWidth * i, rad, mPaint);
            } else {
                canvas.drawRoundRect(mRectF, mBorderParams.rx == 0 ? 0 : mBorderParams.rx + mStrokeWidth * i, mBorderParams.ry == 0 ? 0 : mBorderParams.ry + mStrokeWidth * i, mPaint);
            }
        }

    }


    public BorderParams getBorderParams() {
        return mBorderParams;
    }

    public void setBorderParams(BorderParams params) {
        this.mBorderParams = params;
        setParams();
    }

    public final static class BorderParams {

        public final static int SHADOW_MAX_WIDTH = ScreenUtil.dip2px(ApplicationHolder.getApplication(), 10);

        public final static int DEFAULT_SHADOW_RX = ScreenUtil.dip2px(ApplicationHolder.getApplication(), 2);
        public final static int DEFAULT_SHADOW_RY = ScreenUtil.dip2px(ApplicationHolder.getApplication(), 2);
        public final static int SHADOW_RADIUS_CIRCULAR = -1;

        public final static int DEFAULT_SHADOW_COLOR = Color.parseColor("#3AAF3C");
        public final static int SHADOW_COLOR_BLUE = Color.parseColor("#00FFD1");

        private int shadowColor = DEFAULT_SHADOW_COLOR;
        private int shadowWidth = SHADOW_MAX_WIDTH / 2;
        private float rx = DEFAULT_SHADOW_RX;
        private float ry = DEFAULT_SHADOW_RY;
//
//        private float topLeftRadius = DEFAULT_SHADOW_RY;
//        private float topRightRadius = DEFAULT_SHADOW_RY;
//        private float bottomLeftRadius = DEFAULT_SHADOW_RY;
//        private float bottomRightRadius = DEFAULT_SHADOW_RY;

        public int getShadowColor() {
            return shadowColor;
        }

        public BorderParams setShadowColor(int shadowColor) {
            this.shadowColor = shadowColor;
            return this;
        }

        public int getShadowWidth() {
            return shadowWidth;
        }

        public BorderParams setShadowWidth(int shadowWidth) {
            this.shadowWidth = shadowWidth;
            return this;
        }

        public float getRx() {
            return rx;

        }

        public BorderParams setRx(float rx) {
            this.rx = rx;
            return this;
        }

        public float getRy() {
            return ry;
        }

        public BorderParams setRy(float ry) {
            this.ry = ry;
            return this;
        }
    }
}
