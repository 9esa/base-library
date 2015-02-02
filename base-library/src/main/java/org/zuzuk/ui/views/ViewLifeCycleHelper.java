package org.zuzuk.ui.views;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Gavriil Sitnikov on 22/12/2014.
 * Object that helps to determine view state
 */
public class ViewLifeCycleHelper<T extends View & LifeCycleView> {
    private enum State {STOPPED, PAUSED, RESUMED}

    private final T view;
    private State state = State.STOPPED;
    private boolean isAttachedToWindow = false;

    public ViewLifeCycleHelper(@NonNull T view) {
        this.view = view;
    }

    /* Returns if view is started */
    public boolean isStarted() {
        return state != State.STOPPED;
    }

    /* Returns if view is resumed */
    public boolean isResumed() {
        return state == State.RESUMED;
    }

    public void onAttachedToWindow() {
        isAttachedToWindow = true;
        updateLifeCycleState();
    }

    public void updateLifeCycleState() {
        State oldState = state;
        state = calculateViewState();

        if (oldState == state) {
            return;
        }

        switch (state) {
            case STOPPED:
                if (oldState == State.RESUMED) {
                    view.onPause();
                }
                view.onStop();
                break;
            case PAUSED:
                if (oldState == State.RESUMED) {
                    view.onPause();
                } else {
                    view.onStart();
                }
                break;
            case RESUMED:
                if (oldState == State.STOPPED) {
                    view.onStart();
                }
                view.onResume();
                break;
        }
    }

    private State calculateViewState() {
        if (isAttachedToWindow
                && view.getWindowVisibility() == View.VISIBLE) {
            return view.hasWindowFocus() ? State.RESUMED : State.PAUSED;
        } else {
            return State.STOPPED;
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        updateLifeCycleState();
    }

    public void onWindowVisibilityChanged(int visibility) {
        updateLifeCycleState();
    }

    public void onDetachedFromWindow() {
        isAttachedToWindow = false;
        updateLifeCycleState();
    }
}
