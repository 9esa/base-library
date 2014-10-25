package org.zuzuk.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.zuzuk.ui.fragments.BaseFragment;
import org.zuzuk.ui.fragments.OnFragmentChangedListener;
import org.zuzuk.ui.fragments.StaticFragment;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Activity that include basic fragment navigation logic
 */
public abstract class BaseActivity extends ActionBarActivity
        implements FragmentManager.OnBackStackChangedListener, OnFragmentChangedListener {
    private final static String STATIC_FRAGMENTS_EXTRA = "STATIC_FRAGMENTS_EXTRA";
    private final static String BOTTOM_FRAGMENT_EXTRA = "BOTTOM_FRAGMENT_EXTRA";

    private HashMap<Class, String> staticFragmentsTags = new HashMap<>();
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

    @Override
    public void onFragmentChanged(BaseFragment fragment) {
        currentFragment = fragment;
    }

    /* Raises when back stack changes */
    @Override
    public void onBackStackChanged() {
    }

    public void setStaticFragment(Class fragmentClass) {
        setStaticFragment(fragmentClass, null);
    }

    /**
     * Setting fragment of special class as single on top and one of static fragments.
     * Static fragments are fragments that is stored in background after loading so they can
     * restores faster with last state.
     */
    public void setStaticFragment(Class fragmentClass, Bundle args) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        String fragmentTag = staticFragmentsTags.get(fragmentClass);
        if (fragmentTag == null) {
            fragmentTag = UUID.randomUUID().toString();
            staticFragmentsTags.put(fragmentClass, fragmentTag);
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

        if (!(fragment instanceof StaticFragment))
            throw new IllegalStateException(fragmentClass.getName() + " should implement StaticFragment interface");

        if (args != null) {
            ((StaticFragment) fragment).applyArguments(args);
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
        if (staticFragmentsTags.containsKey(fragmentClass))
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
        if (bottomFragmentTag == null) {
            return;
        }

        Fragment bottomFragment = getSupportFragmentManager().findFragmentByTag(bottomFragmentTag);
        if (bottomFragment == null) {
            return;
        }

        for (String tag : staticFragmentsTags.values()) {
            if (tag.equals(bottomFragmentTag)) {
                transaction.detach(bottomFragment);
                return;
            }
        }
        transaction.remove(bottomFragment);
    }

    /* Pushing fragment of special class on top of fragments stack */
    public void pushFragment(Class fragmentClass) {
        pushFragment(fragmentClass, null);
    }

    /* Pushing fragment of special class with args on top of fragments stack */
    public void pushFragment(Class fragmentClass, Bundle args) {
        if (staticFragmentsTags.containsKey(fragmentClass))
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

    /* Shows device keyboard */
    public void showSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATIC_FRAGMENTS_EXTRA, staticFragmentsTags);
        outState.putString(BOTTOM_FRAGMENT_EXTRA, bottomFragmentTag);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        staticFragmentsTags = (HashMap<Class, String>) savedInstanceState.getSerializable(STATIC_FRAGMENTS_EXTRA);
        bottomFragmentTag = savedInstanceState.getString(BOTTOM_FRAGMENT_EXTRA);
    }
}
