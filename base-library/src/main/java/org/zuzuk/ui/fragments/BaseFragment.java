package org.zuzuk.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;

import org.zuzuk.events.BroadcastEvents;
import org.zuzuk.events.EventAnnotation;
import org.zuzuk.ui.UIUtils;
import org.zuzuk.ui.activities.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Fragment that include base logic to hold views and navigation logic
 */
public abstract class BaseFragment extends Fragment {
    private final HashMap<Integer, View> viewsHolder = new HashMap<>();
    private final Handler postHandler = new Handler();
    private final List<String> globalEvents = new ArrayList<>();
    private final List<String> localEvents = new ArrayList<>();
    private final List<String> globalOnResumeEvents = new ArrayList<>();
    private final List<String> localOnResumeEvents = new ArrayList<>();
    private final BroadcastReceiver globalEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            onEvent(context, intent);
        }
    };
    private final BroadcastReceiver localEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            onEvent(context, intent);
        }
    };
    private final BroadcastReceiver globalOnResumeEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            onEvent(context, intent);
        }
    };
    private final BroadcastReceiver localOnResumeEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            onEvent(context, intent);
        }
    };

    /* Returns post handler to executes code on UI thread */
    public Handler getPostHandler() {
        return postHandler;
    }

    /* Returns base activity */
    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    /**
     * Returns if fragment is a part of another fragment or not.
     * If fragment is nested then it won't be as current fragment in BaseActivity
     */
    protected boolean isNestedFragment() {
        return getParentFragment() != null;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fillListeningBroadcastEvents();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(localEventReceiver, UIUtils.createIntentFilter(localEvents));
        getActivity().registerReceiver(globalEventReceiver, UIUtils.createIntentFilter(globalEvents));
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isNestedFragment()) {
            getBaseActivity().onFragmentChanged(this);
        } else {
            Fragment parentFragment = getParentFragment();
            if (parentFragment != null && parentFragment instanceof OnFragmentChangedListener) {
                ((OnFragmentChangedListener) parentFragment).onFragmentChanged(this);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(localOnResumeEventReceiver, UIUtils.createIntentFilter(localOnResumeEvents));
        getActivity().registerReceiver(globalOnResumeEventReceiver, UIUtils.createIntentFilter(globalOnResumeEvents));
    }

    /* Raises when even received */
    protected void onEvent(Context context, Intent intent) {
    }

    /* Raises when device back button pressed */
    public boolean onBackPressed() {
        return false;
    }

    /* Raises when ActionBar home button pressed */
    public boolean onHomePressed() {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(localOnResumeEventReceiver);
        getActivity().unregisterReceiver(globalOnResumeEventReceiver);
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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(localEventReceiver);
        getActivity().unregisterReceiver(globalEventReceiver);
    }

    private void fillListeningBroadcastEvents() {
        BroadcastEvents events = ((Object) this).getClass().getAnnotation(BroadcastEvents.class);
        if (events != null) {
            for (EventAnnotation eventAnnotation : events.value()) {
                if (eventAnnotation.isLocal()) {
                    if (eventAnnotation.isOnlyWhileResumed()) {
                        localOnResumeEvents.add(eventAnnotation.value());
                    } else {
                        localEvents.add(eventAnnotation.value());
                    }
                } else {
                    if (eventAnnotation.isOnlyWhileResumed()) {
                        globalOnResumeEvents.add(eventAnnotation.value());
                    } else {
                        globalEvents.add(eventAnnotation.value());
                    }
                }
            }
        }
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
