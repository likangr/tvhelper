package com.likang.tvhelper.lib.focus;

/**
 * Created by Likang on 2017/6/12 21:01.
 * email：15034671952@163.com
 * <p>
 * view 焦点变化相应策略
 * 默认所有view 为 {@link #STRATEGY_N_SCALE_Y_BORDER }
 */
public enum ViewFocusStrategy {

    /**
     * 不缩放不加边框
     */
    STRATEGY_N_SCALE_N_BORDER,

    /**
     * 不缩放加边框
     */
    STRATEGY_N_SCALE_Y_BORDER,

    /**
     * 缩放不加边框
     */
    STRATEGY_Y_SCALE_N_BORDER,

    /**
     * 缩放加边框
     */
    STRATEGY_Y_SCALE_Y_BORDER
}