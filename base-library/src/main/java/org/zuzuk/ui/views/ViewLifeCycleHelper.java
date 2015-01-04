package org.zuzuk.ui.views;

import android.view.View;

/**
 * Created by Gavriil Sitnikov on 22/12/2014.
 * Object that helps to determine view state
 */
public class ViewLifeCycleHelper<T extends View & LifeCycleView> {
    private final T view;
    private boolean isResumed = false;
    private boolean isAttachedToWindow = false;

    public ViewLifeCycleHelper(T view) {
        this.view = view;
    }

    /* Returns if view is resumed */
    public boolean isResumed() {
        return isResumed;
    }

    public void onAttachedToWindow() {
        isAttachedToWindow = true;
        updateLifeCycleState();
    }

    public void updateLifeCycleState() {
        if (isResumed != checkIfViewResumed()) {
            isResumed = checkIfViewResumed();
            if (isResumed) {
                view.onResume();
            } else {
                view.onPause();
            }
        }
    }

    private boolean checkIfViewResumed() {
        return isAttachedToWindow && view.getWindowVisibility() == View.VISIBLE;
    }

    public void onWindowVisibilityChanged(int visibility) {
        updateLifeCycleState();
    }

    public void onDetachedFromWindow() {
        isAttachedToWindow = false;
        updateLifeCycleState();
    }
}
