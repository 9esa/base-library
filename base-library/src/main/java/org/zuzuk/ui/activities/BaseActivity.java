package org.zuzuk.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.zuzuk.events.EventListener;
import org.zuzuk.events.EventListenerHelper;
import org.zuzuk.ui.fragments.BaseFragment;
import org.zuzuk.ui.fragments.OnFragmentStartedListener;
import org.zuzuk.utils.Lc;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Activity that include basic fragment navigation logic
 */
public abstract class BaseActivity extends ActionBarActivity
        implements FragmentManager.OnBackStackChangedListener,
        OnFragmentStartedListener,
        EventListener {
    private final static String TOP_FRAGMENT_TAG_MARK = "TOP_FRAGMENT";

    private final EventListenerHelper eventListenerHelper = new EventListenerHelper(this);
    private boolean isPaused = false;

    /* Returns id of main fragments container where navigation-node fragments should be */
    protected int getFragmentContainerId() {
        throw new UnsupportedOperationException("Implement getFragmentContainerId method to use fragment managing");
    }

    /* Returns if last fragment in stack is top (added by setFragment) like fragment from sidebar menu */
    public boolean isCurrentFragmentTop() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            return true;
        }

        String topFragmentTag = fragmentManager
                .getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1)
                .getName();
        return topFragmentTag != null && topFragmentTag.contains(TOP_FRAGMENT_TAG_MARK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        eventListenerHelper.onCreate(this);
    }

    @Override
    protected void onResume() {
        isPaused = false;
        super.onResume();
        eventListenerHelper.onResume();
    }

    @Override
    public void onEvent(Context context, @NonNull String eventName, Intent intent) {
    }

    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
        eventListenerHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventListenerHelper.onDestroy();
    }

    @Override
    public void onFragmentStarted(BaseFragment fragment) {
    }

    /* Raises when back stack changes */
    @Override
    public void onBackStackChanged() {
    }

    /* Setting fragment of special class as first in stack */
    public Fragment setFirstFragment(Class<?> fragmentClass) {
        return setFirstFragment(fragmentClass, null);
    }

    /* Setting fragment of special class as first in stack with args */
    public Fragment setFirstFragment(Class<?> fragmentClass, Bundle args) {
        if (isPaused) {
            Lc.e("Calling to fragment manager while activity is paused", new Exception());
            return null;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), args);
        fragmentManager.beginTransaction()
                .replace(getFragmentContainerId(), fragment, null)
                .commit();
        return fragment;
    }

    private Fragment addFragmentToStack(Class<?> fragmentClass, Bundle args, String backStackTag) {
        if (isPaused) {
            Lc.e("Calling to fragment manager while activity is paused", new Exception());
            return null;
        }

        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), args);
        getSupportFragmentManager().beginTransaction()
                .replace(getFragmentContainerId(), fragment, backStackTag)
                .addToBackStack(backStackTag)
                .commit();
        return fragment;
    }

    /* Setting fragment of special class as top */
    public Fragment setFragment(Class fragmentClass) {
        return setFragment(fragmentClass, null);
    }

    /* Setting fragment of special class as top with args */
    public Fragment setFragment(Class fragmentClass, Bundle args) {
        return addFragmentToStack(fragmentClass, args, fragmentClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK);
    }

    /* Pushing fragment of special class to fragments stack */
    public Fragment pushFragment(Class fragmentClass) {
        return pushFragment(fragmentClass, null);
    }

    /* Pushing fragment of special class with args to fragments stack */
    public Fragment pushFragment(Class fragmentClass, Bundle args) {
        return addFragmentToStack(fragmentClass, args, fragmentClass.getName());
    }

    /* Raises when device back button pressed */
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        boolean backPressResult = false;
        if (fragmentManager.getFragments() != null) {
            for (Fragment fragment : fragmentManager.getFragments()) {
                if (fragment != null && fragment.isResumed() && fragment instanceof BaseFragment) {
                    backPressResult = backPressResult || ((BaseFragment) fragment).onBackPressed();
                }
            }
        }

        if (!backPressResult) {
            super.onBackPressed();
        }
    }

    /* Hides device keyboard */
    public void hideSoftInput() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            View mainFragmentContainer = findViewById(getFragmentContainerId());
            if (mainFragmentContainer != null) {
                mainFragmentContainer.requestFocus();
            }
        }
    }

    /* Shows device keyboard */
    public void showSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
