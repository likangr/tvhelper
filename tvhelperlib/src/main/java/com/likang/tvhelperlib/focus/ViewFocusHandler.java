package com.likang.tvhelperlib.focus;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.likang.tvhelperlib.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Likang
 * @date 2017/6/12 21:01
 * email：15034671952@163.com
 * global focus handler
 */
public class ViewFocusHandler implements ViewTreeObserver.OnGlobalFocusChangeListener,
        ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnPreDrawListener {

    private final String TAG = ViewFocusHandler.class.getSimpleName();
    private Context mContext;
    private FrameLayout mRootView;
    private BorderView mBorder;
    private View mNewFocus;
    private View mOldFocus;
    private ViewTreeObserver mViewTreeObserver;

    private HashMap<ViewGroup, LastViewInfo> mLastFocusedViews;
    private HashMap<View, ViewFocusAppearance> mViewsGainFocusAppearance;
    private HashMap<ViewGroup, ViewFocusAppearance> mWillBeUpdateViewsGainFocusAppearance;

    private AnimatorSet mViewAnimatorSet;
    private Handler mHandler = new Handler();

    private ViewFocusStrategy mFocusStrategy = ViewFocusAppearance.DEFAULT_VIEW_FOCUS_STRATEGY;
    private BorderView.BorderParams mBorderParams = ViewFocusAppearance.DEFAULT_BORDER_PARAMS;
    private float mXScaleValue = ViewFocusAppearance.DEFAULT_X_SCALE_VALUE;
    private float mYScaleValue = ViewFocusAppearance.DEFAULT_Y_SCALE_VALUE;
    private int mAnimTime = ViewFocusAppearance.DEFAULT_ANIM_TIME;

    public ViewFocusHandler(View view) {
        if (view == null) {
            return;
        }
        mContext = view.getContext();
        mRootView = (FrameLayout) view;
        startFocusHandle();
    }

    private void resetDefaultAppearance() {
        mFocusStrategy = ViewFocusAppearance.DEFAULT_VIEW_FOCUS_STRATEGY;
        mBorderParams = ViewFocusAppearance.DEFAULT_BORDER_PARAMS;
        mXScaleValue = ViewFocusAppearance.DEFAULT_X_SCALE_VALUE;
        mYScaleValue = ViewFocusAppearance.DEFAULT_Y_SCALE_VALUE;
        mAnimTime = ViewFocusAppearance.DEFAULT_ANIM_TIME;
    }


    private boolean changeFocusView(View oldFocus, final View newFocus) {
        Logger.d(TAG, "changeFocusView");
        ViewGroup parent = (ViewGroup) newFocus.getParent();
        LastViewInfo lastViewInfo = mLastFocusedViews.get(parent);

        if (oldFocus != null && oldFocus.getParent() == parent) {
            //都是一个viewGroup中的view；更新此viewGroup的lastFocusedView
            updateLastViewInfo(newFocus, parent, lastViewInfo);
        } else {
            //不在同一级viewGroup中；或oldFocus is null;
            if (lastViewInfo.index != -1 && lastViewInfo.view == null) {
                //有默认index
                if (parent instanceof RecyclerView) {
                    lastViewInfo.view = ((RecyclerView) parent).getLayoutManager().findViewByPosition(lastViewInfo.index);
                } else {
                    lastViewInfo.view = parent.getChildAt(lastViewInfo.index);
                }
            }

            if (lastViewInfo.view == null) {
                //此viewGroup中view 第一次获取焦点；
                updateLastViewInfo(newFocus, parent, lastViewInfo);
            } else {
                final View view = lastViewInfo.view;
                Logger.d(TAG, "lastViewInfo:" + view);

                if (view != newFocus
                        && view.getParent() == parent
                        && judgeIndexIsChanged(view, lastViewInfo, parent)) {
                    /*如果view已经不在viewgroup中了就不再去让它获取焦点(或者view在group中的位置发生变化)*/
                    final View.OnFocusChangeListener transferListener
                            = newFocus.getOnFocusChangeListener();
                    newFocus.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            view.requestFocus();
                            newFocus.setOnFocusChangeListener(transferListener);
                        }
                    });
                    Logger.w(TAG, "focusTransfer: from " + newFocus + "to" + view);
                    return true;
                } else {
                    updateLastViewInfo(newFocus, parent, lastViewInfo);
                }
            }
        }
        return false;
    }

    private boolean judgeIndexIsChanged(View view, LastViewInfo lastViewInfo, ViewGroup parent) {
        if (parent instanceof RecyclerView) {
            return lastViewInfo.index == ((RecyclerView) parent).getChildAdapterPosition(view);
        } else {
            return lastViewInfo.index == parent.indexOfChild(view);
        }
    }

    private void updateLastViewInfo(View newFocus, ViewGroup parent, LastViewInfo lastViewInfo) {
        if (parent instanceof RecyclerView) {
            lastViewInfo.index = ((RecyclerView) parent).getChildAdapterPosition(newFocus);
        } else {
            lastViewInfo.index = parent.indexOfChild(newFocus);
        }
        Logger.d("updateLastViewInfo", "index:" + lastViewInfo.index);
        lastViewInfo.view = newFocus;
    }

    private void resetView() {
        if (mViewAnimatorSet != null) {
            mViewAnimatorSet.cancel();
            mViewAnimatorSet = null;
        }
        if (mNewFocus != null) {
            mNewFocus.setScaleX(1);
            mNewFocus.setScaleY(1);
        }
        if (mOldFocus != null) {
            mOldFocus.setScaleX(1);
            mOldFocus.setScaleY(1);
        }
    }

    private void scaleView() {
        getViewFocusAppearance();
        mViewAnimatorSet = new AnimatorSet();
        ObjectAnimator shrinkScaleXAnimator
                = ObjectAnimator.ofFloat(mOldFocus, "ScaleX", 1f);
        ObjectAnimator shrinkScaleYAnimator
                = ObjectAnimator.ofFloat(mOldFocus, "ScaleY", 1f);


        if (mFocusStrategy == ViewFocusStrategy.STRATEGY_Y_SCALE_Y_BORDER
                || mFocusStrategy == ViewFocusStrategy.STRATEGY_Y_SCALE_N_BORDER) {

            //新获取焦点的view 需要缩放；
            if ("ShadowOverlayContainer".equals(mNewFocus.getParent().getClass().getSimpleName())) {
                //兼容leanback 已经默认进行放大效果，故在此不进行放大；
                if (mOldFocus != null) {
                    mViewAnimatorSet.play(shrinkScaleXAnimator)
                            .with(shrinkScaleYAnimator);
                }
            } else {
                ObjectAnimator enlargeScaleXAnimator =
                        ObjectAnimator.ofFloat(mNewFocus, "ScaleX", 1f, mXScaleValue);
                ObjectAnimator enlargeScaleYAnimator =
                        ObjectAnimator.ofFloat(mNewFocus, "ScaleY", 1f, mYScaleValue);

                if (mOldFocus == null) {
                    mViewAnimatorSet.play(enlargeScaleXAnimator)
                            .with(enlargeScaleYAnimator);
                } else {
                    mViewAnimatorSet.play(shrinkScaleXAnimator)
                            .with(shrinkScaleYAnimator)
                            .with(enlargeScaleXAnimator)
                            .with(enlargeScaleYAnimator);
                }
            }

        } else {
            //新获取焦点的view 不需要缩放；
            if (mOldFocus != null) {
                mViewAnimatorSet.play(shrinkScaleXAnimator).with(shrinkScaleYAnimator);
            }
        }

        mViewAnimatorSet.setDuration(mAnimTime);
        mViewAnimatorSet.start();
    }

    private void drawBorder() {
        Logger.d(TAG, "drawBorder:" + "mNewFocus:" + mNewFocus + "");
        if (mNewFocus == null) {
            return;
        }

        getViewFocusAppearance();
        //不画边框；
        if (mFocusStrategy == ViewFocusStrategy.STRATEGY_N_SCALE_N_BORDER
                || mFocusStrategy == ViewFocusStrategy.STRATEGY_Y_SCALE_N_BORDER) {
            //如果新的view 不需要画边框的话就隐藏边框 ；
            if (mBorder != null && mBorder.getWidth() != 0) {
                ViewGroup.LayoutParams params = mBorder.getLayoutParams();
                params.width = 0;
                mBorder.setLayoutParams(params);
            }
            return;
        }

        final int rootViewLocation[] = {0, 0};
        mRootView.getLocationInWindow(rootViewLocation);

        final int newFocusLocation[] = {0, 0};
        mNewFocus.getLocationInWindow(newFocusLocation);

        newFocusLocation[1] -= rootViewLocation[1];

        newFocusLocation[0] -= BorderView.BorderParams.SHADOW_MAX_WIDTH;
        newFocusLocation[1] -= BorderView.BorderParams.SHADOW_MAX_WIDTH;
        float borderViewWidth;
        float borderHeight;
        if ("ShadowOverlayContainer".equals(mNewFocus.getParent().getClass().getSimpleName())) {
            borderViewWidth = mNewFocus.getWidth() * ((View) mNewFocus.getParent()).getScaleX()
                    + BorderView.BorderParams.SHADOW_MAX_WIDTH * 2;
            borderHeight = mNewFocus.getHeight() * ((View) mNewFocus.getParent()).getScaleY()
                    + BorderView.BorderParams.SHADOW_MAX_WIDTH * 2;

        } else {
            borderViewWidth = mNewFocus.getWidth() * mNewFocus.getScaleX()
                    + BorderView.BorderParams.SHADOW_MAX_WIDTH * 2;
            borderHeight = mNewFocus.getHeight() * mNewFocus.getScaleY()
                    + BorderView.BorderParams.SHADOW_MAX_WIDTH * 2;
        }

        FrameLayout.LayoutParams params;
        if (mBorder != null) {
            params = (FrameLayout.LayoutParams) mBorder.getLayoutParams();
            if (mBorder.getWidth() == (int) borderViewWidth
                    && mBorder.getHeight() == (int) borderHeight
                    && mBorder.getTranslationX() == newFocusLocation[0]
                    && mBorder.getTranslationY() == newFocusLocation[1]) {

                mHandler.postDelayed(setBorderViewVisibleRunnable, 50);
                //会执行多次onPreDraw, 如果发现 border 的位置和大小已经 设置好就不需要向下执行了
                return;
            }

            params.height = (int) borderHeight;
            params.width = (int) borderViewWidth;
            mBorder.setBorderParams(mBorderParams);
        } else {
            mBorder = new BorderView(mContext);
            mBorder.setBorderParams(mBorderParams);
            mRootView.addView(mBorder);
            params = new FrameLayout.LayoutParams((int) borderViewWidth, (int) borderHeight);
        }

        Logger.d(TAG, "drawBorder:" + "borderViewWidth:" + borderViewWidth + ",borderHeight:" + borderHeight);
        Logger.d(TAG, "drawBorder:" + "locationx:" + newFocusLocation[0] + ",locationy:" + newFocusLocation[1]);

        mBorder.setTranslationX(newFocusLocation[0]);
        mBorder.setTranslationY(newFocusLocation[1]);
        mBorder.setLayoutParams(params);
        mBorder.setVisibility(View.INVISIBLE);
    }

    private void getViewFocusAppearance() {
        resetDefaultAppearance();
        if (mViewsGainFocusAppearance != null) {
            ViewFocusAppearance appearance = mViewsGainFocusAppearance.get(mNewFocus);
            if (appearance != null) {
                mFocusStrategy = appearance.getFocusStrategy();
                mXScaleValue = appearance.getXScaleValue();
                mYScaleValue = appearance.getYScaleValue();
                mAnimTime = appearance.getAnimTime();
                mBorderParams = appearance.getBorderParams();
            }
        }
    }

    private Runnable setBorderViewVisibleRunnable = new Runnable() {
        @Override
        public void run() {
            mBorder.setVisibility(View.VISIBLE);
        }
    };


    private void addViewFocusAppearanceTraverse() {
        if (mWillBeUpdateViewsGainFocusAppearance != null) {
            Iterator<Map.Entry<ViewGroup, ViewFocusAppearance>> iterator
                    = mWillBeUpdateViewsGainFocusAppearance.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ViewGroup, ViewFocusAppearance> next = iterator.next();
                ViewGroup viewGroup = next.getKey();
                ViewFocusAppearance nextValue = next.getValue();
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    addViewFocusAppearance(viewGroup.getChildAt(i), nextValue, false);
                }
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        Logger.d(TAG, "onGlobalLayout");
        allotFirstFocusView();
    }

    private void allotFirstFocusView() {
        Logger.d(TAG, "allotFirstFocusView :mNewFocus:" + mNewFocus);
        Logger.d(TAG, "allotFirstFocusView :mOldFocus:" + mOldFocus);
        if (mNewFocus == null && mOldFocus == null) {
            //第一次进入界面；
            View focusedChild = mRootView.findFocus();
            Logger.d(TAG, "allotFirstFocusView :focusedChild:" + focusedChild);
            if (focusedChild == null) {
                ArrayList<View> focusables = mRootView.getFocusables(View.FOCUS_DOWN);
                Logger.d(TAG, "allotFirstFocusView :focusables:" + focusables);
                if (focusables.size() != 0) {
                    focusables.get(0).requestFocus();
                }
                ArrayList<View> focusables2 = mRootView.getFocusables(View.FOCUS_RIGHT);
                Logger.d(TAG, "allotFirstFocusView :focusables2:" + focusables2);
                if (focusables2.size() != 0) {
                    focusables2.get(0).requestFocus();
                }

            } else {
                onGlobalFocusChanged(null, focusedChild);
            }
        }
    }


    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        addViewFocusAppearanceTraverse();
        Logger.d(TAG, "onGlobalFocusChanged{" + "oldFocus:" + oldFocus + ",newFocus:" + newFocus + "}");
        resetView();
        if (mLastFocusedViews != null && newFocus !=
                null && mLastFocusedViews.get(newFocus.getParent()) != null) {
            if (changeFocusView(oldFocus, newFocus)) {
                return;
            }
        }
        mNewFocus = newFocus;
        mOldFocus = oldFocus;
        scaleView();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        Logger.d(TAG, "onWindowFocusChanged :hasFocus:" + hasFocus);
        Logger.d(TAG, "onWindowFocusChanged :mNewFocus:" + mNewFocus);
        Logger.d(TAG, "onWindowFocusChanged :mOldFocus:" + mOldFocus);
    }


    @Override
    public boolean onPreDraw() {
        Logger.d(TAG, "onPreDraw");
        drawBorder();
        return true;
    }


    class LastViewInfo {
        LastViewInfo(int index, View view) {
            this.index = index;
            this.view = view;
        }

        private int index;
        private View view;

        @Override
        public String toString() {
            return "LastViewInfo{" +
                    "index=" + index +
                    ", view=" + view +
                    '}';
        }
    }


    /***********************api**********************/

    /**
     * 开始全局焦点处理；
     */
    public void startFocusHandle() {
        mViewTreeObserver = mRootView.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalFocusChangeListener(this);
        mViewTreeObserver.addOnGlobalLayoutListener(this);
        mViewTreeObserver.addOnPreDrawListener(this);
    }

    /**
     * 停止全局焦点处理；
     */
    public void stopFocusHandle() {
        if (mViewTreeObserver == null || !mViewTreeObserver.isAlive()) {
            return;
        }
        mViewTreeObserver.removeOnGlobalFocusChangeListener(this);
        mViewTreeObserver.removeOnGlobalLayoutListener(this);
        mViewTreeObserver.removeOnPreDrawListener(this);
    }


    /**
     * 为某个指定view 或一级子view 添加焦点变化外观
     *
     * @param view
     * @param appearance
     * @param isAddForLevel1 是否是为一级子view 添加外观
     */
    public void addViewFocusAppearance(View view, ViewFocusAppearance appearance,
                                       boolean isAddForLevel1) {
        if (isAddForLevel1) {
            if (mWillBeUpdateViewsGainFocusAppearance == null) {
                mWillBeUpdateViewsGainFocusAppearance = new HashMap<>();
            }
            mWillBeUpdateViewsGainFocusAppearance.put((ViewGroup) view, appearance);
        } else {
            if (mViewsGainFocusAppearance == null) {
                mViewsGainFocusAppearance = new HashMap<>();
            }
            mViewsGainFocusAppearance.put(view, appearance);

        }
    }


    /**
     * 为recycleView中的所有item 添加焦点变化外观
     *
     * @param recyclerView
     */
    public void addRecycleViewFocusAppearance(RecyclerView recyclerView) {
        /*设置recycleview 默认 item样式*/
        ViewFocusAppearance appearance = new ViewFocusAppearance().setFocusStrategy(ViewFocusStrategy.STRATEGY_Y_SCALE_Y_BORDER).setBorderParams(new BorderView.BorderParams().setShadowWidth(BorderView.BorderParams.SHADOW_MAX_WIDTH).setShadowColor(BorderView.BorderParams.SHADOW_COLOR_BLUE));

        addViewFocusAppearance(recyclerView, appearance, true);
    }

    /**
     * 为某个指定viewGroup设置记住最后获取过焦点的一级子view；
     *
     * @param viewGroup
     */
    public void rememberLastFocusView(ViewGroup viewGroup) {
        rememberLastFocusView(viewGroup, -1);
    }

    public void rememberLastFocusView(ViewGroup viewGroup, int defaultIndex) {

        if (mLastFocusedViews == null) {
            mLastFocusedViews = new HashMap<>();
        }
        if (mLastFocusedViews.get(viewGroup) != null) {
            return;
        }
        LastViewInfo viewInfo = new LastViewInfo(defaultIndex, null);
        mLastFocusedViews.put(viewGroup, viewInfo);
    }
}