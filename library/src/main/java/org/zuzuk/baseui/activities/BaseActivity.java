package org.zuzuk.baseui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.zuzuk.baseui.fragments.BaseFragment;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Activity that include basic fragment navigation logic
 */
public abstract class BaseActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {
    private final static String HOME_FRAGMENTS_EXTRA = "HOME_FRAGMENTS_EXTRA";
    private final static String BOTTOM_FRAGMENT_EXTRA = "BOTTOM_FRAGMENT_EXTRA";

    private HashMap<Class, String> homeFragmentsTags = new HashMap<>();
    private String bottomFragmentTag;
    private BaseFragment currentFragment;

    /* Returns id of main fragments container where navigation-node fragments should be */
    protected int getFragmentContainerId() {
        throw new UnsupportedOperationException("Implement getFragmentContainerId method to use fragment managing");
    }

    /* Returns current navigation-node fragment */
    protected Fragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    /* Raises when current navigation-node fragment changes */
    public void onFragmentChanged(BaseFragment fragment) {
        currentFragment = fragment;
    }

    /* Raises when back stack changes */
    @Override
    public void onBackStackChanged() {
    }

    /**
     * Setting fragment of special class as single on top and one of home fragments.
     * Home fragments are fragments that is stored in background after loading so they can
     * restores faster with last state.
     * There couldn't be any args because this fragment couldn't replaces by any other fragment
     * of same class
     */
    public void setHomeFragment(Class fragmentClass) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        String fragmentTag = homeFragmentsTags.get(fragmentClass);
        if (fragmentTag == null) {
            fragmentTag = UUID.randomUUID().toString();
            homeFragmentsTags.put(fragmentClass, fragmentTag);
        }

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        if (fragmentTag.equals(bottomFragmentTag)) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        removeBottomFragment(transaction);

        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        if (fragment == null) {
            fragment = Fragment.instantiate(this, fragmentClass.getName());
            transaction.add(getFragmentContainerId(), fragment, fragmentTag);
        } else {
            transaction.attach(fragment);
        }

        transaction.commit();

        bottomFragmentTag = fragmentTag;
    }

    /* Setting fragment of special class as single on top */
    public void setFragment(Class fragmentClass) {
        setFragment(fragmentClass, null);
    }

    /* Setting fragment of special class as single on top with args */
    public void setFragment(Class fragmentClass, Bundle args) {
        if (homeFragmentsTags.containsKey(fragmentClass))
            throw new IllegalStateException("Fragment " + fragmentClass + " should be set as home fragment as it already is");

        String fragmentTag = UUID.randomUUID().toString();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), args);
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        removeBottomFragment(transaction);

        transaction.add(getFragmentContainerId(), fragment, fragmentTag).commit();

        bottomFragmentTag = fragmentTag;
    }

    private void removeBottomFragment(FragmentTransaction transaction) {
        if (bottomFragmentTag != null) {
            Fragment bottomFragment = getSupportFragmentManager().findFragmentByTag(bottomFragmentTag);
            for (String tag : homeFragmentsTags.values()) {
                if (tag.equals(bottomFragmentTag)) {
                    transaction.detach(bottomFragment);
                    return;
                }
            }
            transaction.remove(bottomFragment);
        }
    }

    /* Pushing fragment of special class on top of fragments stack */
    public void pushFragment(Class fragmentClass) {
        pushFragment(fragmentClass, null);
    }

    /* Pushing fragment of special class with args on top of fragments stack */
    public void pushFragment(Class fragmentClass, Bundle args) {
        if (homeFragmentsTags.containsKey(fragmentClass))
            throw new IllegalStateException("Fragment " + fragmentClass + " shouldn't be push as it is home fragment");

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), args);
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (fragmentManager.getBackStackEntryCount() == 0) {
            removeBottomFragment(transaction);
        }

        transaction.replace(getFragmentContainerId(), fragment)
                .addToBackStack(fragmentClass.getName())
                .commit();
    }

    /* Raises when device back button pressed */
    @Override
    public void onBackPressed() {
        if (currentFragment == null || !currentFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (currentFragment == null || currentFragment.onHomePressed()) {
                    return true;
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                int stackSize = fragmentManager.getBackStackEntryCount();

                switch (stackSize) {
                    case 0:
                        return false;
                    case 1:
                        fragmentManager.popBackStack();
                        return true;
                    default:
                        String lastFragmentName = fragmentManager.getBackStackEntryAt(stackSize - 1).getName();
                        for (int i = stackSize - 2; i >= 0; i--) {
                            String currentFragmentName = fragmentManager.getBackStackEntryAt(i).getName();
                            if (currentFragmentName == null || !currentFragmentName.equals(lastFragmentName)) {
                                fragmentManager.popBackStack(currentFragmentName, 0);
                                break;
                            } else if (i == 0) {
                                fragmentManager.popBackStack(currentFragmentName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            } else {
                                lastFragmentName = currentFragmentName;
                            }
                        }
                        return true;
                }
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(HOME_FRAGMENTS_EXTRA, homeFragmentsTags);
        outState.putString(BOTTOM_FRAGMENT_EXTRA, bottomFragmentTag);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        homeFragmentsTags = (HashMap<Class, String>) savedInstanceState.getSerializable(HOME_FRAGMENTS_EXTRA);
        bottomFragmentTag = savedInstanceState.getString(BOTTOM_FRAGMENT_EXTRA);
    }
}
