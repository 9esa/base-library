package org.zuzuk.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;

import org.zuzuk.events.EventListener;
import org.zuzuk.events.EventListenerHelper;
import org.zuzuk.tasks.aggregationtask.TaskExecutorHelper;
import org.zuzuk.ui.activities.BaseActivity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Fragment that include base logic to hold views and navigation logic
 */
public abstract class BaseFragment extends Fragment implements EventListener,
        OnFragmentStartedListener {
    private final HashMap<Integer, View> viewsHolder = new HashMap<>();
    private final Handler postHandler = new Handler();
    private final EventListenerHelper eventListenerHelper = new EventListenerHelper(this);

    /* Returns post handler to executes code on UI thread */
    public Handler getPostHandler() {
        return postHandler;
    }

    /* Returns base activity */
    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    public void onFragmentStarted(BaseFragment fragment) {
    }

    /* Returns title of fragment. Class name by default */
    public CharSequence getTitle() {
        return ((Object) this).getClass().getSimpleName();
    }

    /* Configuring action bar. using title by default */
    public void configureActionBar() {
        getBaseActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
        getBaseActivity().getSupportActionBar().setDisplayShowCustomEnabled(false);
        getBaseActivity().getSupportActionBar().setTitle(getTitle());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        eventListenerHelper.onCreate(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            if (parentFragment instanceof OnFragmentStartedListener) {
                ((OnFragmentStartedListener) parentFragment).onFragmentStarted(this);
            }
        } else {
            getBaseActivity().onFragmentStarted(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eventListenerHelper.onResume();
    }

    /* Raises when device back button pressed */
    public boolean onBackPressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        boolean result = false;

        if (fragmentManager.getFragments() == null) {
            return false;
        }

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null && fragment.isResumed() && fragment instanceof BaseFragment) {
                result = result || ((BaseFragment) fragment).onBackPressed();
            }
        }
        return result;
    }

    /* Raises when ActionBar home button pressed */
    public boolean onHomePressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        boolean result = false;

        if (fragmentManager.getFragments() == null) {
            return false;
        }

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null && fragment.isResumed() && fragment instanceof BaseFragment) {
                result = result || ((BaseFragment) fragment).onHomePressed();
            }
        }
        return result;
    }

    @Override
    public void onEvent(Context context, @NonNull String eventName, Intent intent) {
    }

    @Override
    public void onPause() {
        super.onPause();
        eventListenerHelper.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        postHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewsHolder.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        eventListenerHelper.onDestroy();
    }

    /* Finds view by id and stores it in cache till view destroys */
    @SuppressWarnings("unchecked")
    protected <TView extends View> TView findViewById(int viewId) {
        View result = viewsHolder.get(viewId);
        if (result == null) {
            result = getView().findViewById(viewId);
            viewsHolder.put(viewId, result);
        }
        return (TView) result;
    }

    /* Finds view by type */
    @SuppressWarnings("unchecked")
    protected <T> T findViewByType(Class<T> clazz, View parentView) {
        if (clazz.isInstance(parentView)) {
            return (T) parentView;
        }

        Queue<ViewGroup> viewGroupQueue = new LinkedList<>();
        if (parentView instanceof ViewGroup) {
            viewGroupQueue.add((ViewGroup) parentView);
        }
        while (!viewGroupQueue.isEmpty()) {
            ViewGroup viewGroup = viewGroupQueue.poll();
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (clazz.isInstance(child)) {
                    return (T) child;
                }
                if (child instanceof ViewGroup) {
                    viewGroupQueue.add((ViewGroup) child);
                }
            }
        }
        return null;
    }
}
