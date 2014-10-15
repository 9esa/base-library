package org.zuzuk.ui.fragments;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import org.zuzuk.ui.activities.BaseActivity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Fragment that include base logic to hold views and navigation logic
 */
public abstract class BaseFragment extends Fragment {
    private final HashMap<Integer, View> viewsHolder = new HashMap<>();
    private final Handler postHandler = new Handler();

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

    /* Raises when device back button pressed */
    public boolean onBackPressed() {
        return false;
    }

    /* Raises when ActionBar home button pressed */
    public boolean onHomePressed() {
        return false;
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
