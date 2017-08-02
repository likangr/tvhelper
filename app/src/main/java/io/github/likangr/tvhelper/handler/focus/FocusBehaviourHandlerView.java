package io.github.likangr.tvhelper.handler.focus;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.ShadowOverlayContainer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import io.github.likangr.tvhelper.R;


/**
 * Created by Likang on 2017/7/7 18:45.
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
    public View focusSearch(View focused, int direction) {
        Log.d(TAG, "focusSearch");

        View next = super.focusSearch(focused, direction);

        Log.d(TAG, "focusSearch--focused:" + focused);
        Log.d(TAG, "focusSearch--next:" + next);
        Log.d(TAG, "focusSearch--direction:" + direction);
        boolean preferredNextFocus = isPreferredNextFocus(focused, next, direction);
        Log.d(TAG, "focusSearch--isPreferredNextFocus:" + preferredNextFocus);

        return preferredNextFocus ? next : null;
//        return next;
    }


    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        Log.d(TAG, "dispatchUnhandledMove");
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
            if (focused.getParent() instanceof ShadowOverlayContainer) {
                focused = (View) focused.getParent();
            }
            mAnimator.cancel();
            focused.setTranslationX(0);
            focused.setTranslationY(0);
        }
    }

    private void startShake(View focused, int direction) {
        if (focused.getParent() instanceof ShadowOverlayContainer) {
            focused = (View) focused.getParent();
        }
        mAnimator = shaker(focused, direction);
        mAnimator.setDuration(500);
        mAnimator.start();
    }

    private Rect mTempRect = new Rect();
    private Rect mTempRect2 = new Rect();

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

        int[] location = getLocation(focused);
        int[] location2 = getLocation(next);
        mTempRect.set(location[0], location[1], location[0] + focused.getWidth(), location[1] + focused.getHeight());
        mTempRect2.set(location2[0], location2[1], location2[0] + next.getWidth(), location2[1] + next.getHeight());

        switch (direction) {
            case View.FOCUS_LEFT:
                return mTempRect.left >= mTempRect2.right;
            case View.FOCUS_RIGHT:
                return mTempRect.right <= mTempRect2.left;
            case View.FOCUS_UP:
                return mTempRect.top >= mTempRect2.bottom;
            case View.FOCUS_DOWN:
                return mTempRect.bottom <= mTempRect2.top;
        }
        throw new IllegalArgumentException("direction must be absolute. received:" + direction);
    }

    private int[] getLocation(View focused) {
        int location[] = {0, 0};
        focused.getLocationOnScreen(location);
        return location;
    }

    private ObjectAnimator shaker(View view, int direction) {
        int shaker = view.getResources().getDimensionPixelOffset(R.dimen.px30);

        switch (direction) {
            case View.FOCUS_RIGHT:
            case View.FOCUS_DOWN:
                shaker = -shaker;
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
        }

        return ObjectAnimator.ofPropertyValuesHolder(view, pvh);
    }
}
