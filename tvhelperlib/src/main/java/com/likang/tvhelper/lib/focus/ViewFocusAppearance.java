package com.likang.tvhelper.lib.focus;

/**
 *
 * @author Likang
 * @date 2017/6/12 21:01
 * email：15034671952@163.com
 * 定义 view 获取焦点后如何变化{1.缩放，2.是否需要边框}
 */
public class ViewFocusAppearance {

    public final static int DEFAULT_ANIM_TIME = 200;
    public final static float DEFAULT_X_SCALE_VALUE = 1.14f;
    public final static float DEFAULT_Y_SCALE_VALUE = 1.14f;
    public final static ViewFocusStrategy DEFAULT_VIEW_FOCUS_STRATEGY = ViewFocusStrategy.STRATEGY_N_SCALE_Y_BORDER;
    public final static BorderView.BorderParams DEFAULT_BORDER_PARAMS = new BorderView.BorderParams();


    private int animTime = DEFAULT_ANIM_TIME;
    private float xScaleValue = DEFAULT_X_SCALE_VALUE;
    private float yScaleValue = DEFAULT_Y_SCALE_VALUE;
    private ViewFocusStrategy focusStrategy = DEFAULT_VIEW_FOCUS_STRATEGY;
    private BorderView.BorderParams borderParams = DEFAULT_BORDER_PARAMS;

    /**
     * 设置获取焦点后边框的一些属性{@link BorderView.BorderParams#shadowColor 阴影颜色等}
     *
     * @param borderParams
     * @return
     */
    public ViewFocusAppearance setBorderParams(BorderView.BorderParams borderParams) {
        this.borderParams = borderParams;
        return this;
    }

    /**
     * 设置获取焦点后View X轴缩放值
     *
     * @param xScaleValue
     * @return
     */
    public ViewFocusAppearance setXScaleValue(float xScaleValue) {
        this.xScaleValue = xScaleValue;
        return this;
    }

    /**
     * 设置获取焦点后View Y轴缩放值
     *
     * @param yScaleValue
     * @return
     */
    public ViewFocusAppearance setYScaleValue(float yScaleValue) {
        this.yScaleValue = yScaleValue;
        return this;
    }

    /**
     * 设置获取焦点后View 缩放动画时间值
     *
     * @param animTime
     * @return
     */
    public ViewFocusAppearance setAnimTime(int animTime) {
        this.animTime = animTime;
        return this;
    }

    /**
     * 设置获取焦点后View 的变化策略{@link ViewFocusStrategy}
     *
     * @param focusStrategy
     * @return
     */
    public ViewFocusAppearance setFocusStrategy(ViewFocusStrategy focusStrategy) {
        this.focusStrategy = focusStrategy;
        return this;
    }


    public float getXScaleValue() {
        return xScaleValue;
    }

    public float getYScaleValue() {
        return yScaleValue;
    }

    public int getAnimTime() {
        return animTime;
    }

    public ViewFocusStrategy getFocusStrategy() {
        return focusStrategy;
    }

    public BorderView.BorderParams getBorderParams() {
        return borderParams;
    }
}