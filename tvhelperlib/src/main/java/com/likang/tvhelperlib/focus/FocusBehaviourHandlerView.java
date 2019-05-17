package com.likang.tvhelperlib.focus;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.likang.tvhelperlib.util.ApplicationHolder;
import com.likang.tvhelperlib.util.Logger;
import com.likang.tvhelperlib.util.ScreenUtil;

import java.util.ArrayList;

/**
 * @author Likang
 * @date 2017/7/7 18:45
 * email：15034671952@163.com
 * des: The root view of all the interfaces to determine whether the next focus is what we want, not just shake.
 */
public class FocusBehaviourHandlerView extends FrameLayout {

    private final String TAG = FocusBehaviourHandlerView.class.getSimpleName();
    private ObjectAnimator mAnimator;

    public FocusBehaviourHandlerView(@NonNull Context context) {
        super(context);
    }

    public FocusBehaviourHandlerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusBehaviourHandlerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        Logger.d(TAG, views.toString() + "===addFocusables===" + views.size());
        super.addFocusables(views, direction, focusableMode);
        Logger.d(TAG, views.toString() + "===addFocusables===" + views.size());

    }

    @Override
    public View focusSearch(View focused, int direction) {
        Logger.d(TAG, "focusSearch");

        View next = super.focusSearch(focused, direction);

        Logger.d(TAG, "focusSearch--focused:" + focused);
        Logger.d(TAG, "focusSearch--next:" + next);
        Logger.d(TAG, "focusSearch--direction:" + direction);
        boolean preferredNextFocus = isPreferredNextFocus(focused, next, direction);
        Logger.d(TAG, "focusSearch--isPreferredNextFocus:" + preferredNextFocus);

        return preferredNextFocus ? next : null;
    }


    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        Logger.d(TAG, "dispatchUnhandledMove");
        if (focused != null) {
            cancelShake(focused);

            if (direction == View.FOCUS_FORWARD || direction == View.FOCUS_BACKWARD) {
                if (direction == View.FOCUS_FORWARD) {
                    startShake(focused, View.FOCUS_DOWN);
                } else {
                    startShake(focused, View.FOCUS_UP);
                }
            } else {
                startShake(focused, direction);
            }
            return true;
        } else {
            return super.dispatchUnhandledMove(focused, direction);
        }

    }

    private void cancelShake(View focused) {
        if (mAnimator != null) {
            if ("ShadowOverlayContainer".equals(focused.getParent().getClass().getSimpleName())) {
                focused = (View) focused.getParent();
            }
            mAnimator.cancel();
            focused.setTranslationX(0);
            focused.setTranslationY(0);
        }
    }

    private void startShake(View focused, int direction) {
        Log.d(TAG, "startShake: focused=" + focused);
        if ("IjkVideoView".equals(focused.getClass().getSimpleName())) {
            return;
        }
        if ("ShadowOverlayContainer".equals(focused.getParent().getClass().getSimpleName())) {
            focused = (View) focused.getParent();
        }
        mAnimator = shaker(focused, direction);
        mAnimator.setDuration(500);
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                View target = (View) mAnimator.getTarget();
                if (target.isFocused()) {
                    mAnimator.start();
                }
            }
        }, 200);

    }

    private Rect mFocusedViewLocationRect = new Rect();
    private Rect mNextViewLocationRect = new Rect();

    private boolean isPreferredNextFocus(View focused, View next, int direction) {

        if (next == null) {
            return false;
        }
        if (focused == null) {
            return true;
        }

        if (direction == View.FOCUS_FORWARD || direction == View.FOCUS_BACKWARD) {
            if (direction == View.FOCUS_FORWARD) {
                return isPreferredNextFocusAbsolute(focused, next, View.FOCUS_DOWN);
            } else {
                return isPreferredNextFocusAbsolute(focused, next, View.FOCUS_UP);
            }
        } else {
            return isPreferredNextFocusAbsolute(focused, next, direction);
        }

    }

    /**
     * Logic taken from FocusSearch#isCandidate
     */
    private boolean isPreferredNextFocusAbsolute(View focused, View next, int direction) {

        int[] focusedViewLocation = getLocation(focused);
        int[] nextViewLocation = getLocation(next);
        mFocusedViewLocationRect.set(focusedViewLocation[0], focusedViewLocation[1], focusedViewLocation[0] + focused.getWidth(), focusedViewLocation[1] + focused.getHeight());
        mNextViewLocationRect.set(nextViewLocation[0], nextViewLocation[1], nextViewLocation[0] + next.getWidth(), nextViewLocation[1] + next.getHeight());

        switch (direction) {
            case View.FOCUS_LEFT:
                return mFocusedViewLocationRect.left >= mNextViewLocationRect.left;
            case View.FOCUS_RIGHT:
                return mFocusedViewLocationRect.right <= mNextViewLocationRect.right;
            case View.FOCUS_UP:
                return mFocusedViewLocationRect.top >= mNextViewLocationRect.top;
            case View.FOCUS_DOWN:
                return mFocusedViewLocationRect.bottom <= mNextViewLocationRect.bottom;
            default:
                break;
        }
        throw new IllegalArgumentException("direction must be absolute. received:" + direction);
    }

    private int[] getLocation(View focused) {
        int location[] = {0, 0};
        focused.getLocationInWindow(location);
        return location;
    }

    private ObjectAnimator shaker(View view, int direction) {
        int shaker = ScreenUtil.dip2px(ApplicationHolder.getApplication(), 15);

        switch (direction) {
            case View.FOCUS_RIGHT:
            case View.FOCUS_DOWN:
                shaker = -shaker;
                break;
            default:
                break;
        }

        Keyframe keyframe[] = {Keyframe.ofFloat(0f, 0),
                Keyframe.ofFloat(.10f, -shaker * .9f),
                Keyframe.ofFloat(.20f, 0),
                Keyframe.ofFloat(.30f, -shaker * .7f),
                Keyframe.ofFloat(.40f, 0),
                Keyframe.ofFloat(.50f, -shaker * .5f),
                Keyframe.ofFloat(.60f, 0),
                Keyframe.ofFloat(.70f, -shaker * .3f),
                Keyframe.ofFloat(.80f, 0),
                Keyframe.ofFloat(.90f, -shaker * .1f),
                Keyframe.ofFloat(1f, 0f)};

        PropertyValuesHolder pvh = null;
        switch (direction) {
            case View.FOCUS_LEFT:
            case View.FOCUS_RIGHT:
                pvh = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X, keyframe);
                break;
            case View.FOCUS_UP:
            case View.FOCUS_DOWN:
                pvh = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_Y, keyframe);
                break;
            default:
                break;
        }

        return ObjectAnimator.ofPropertyValuesHolder(view, pvh);
    }
}
