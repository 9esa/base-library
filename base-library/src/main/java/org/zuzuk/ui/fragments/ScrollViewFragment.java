package org.zuzuk.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Loading fragment that based on ScrollView so it have common restore logic
 */
public abstract class ScrollViewFragment extends LoadingFragment {
    private final static String SCROLL_Y_KEY = "SCROLL_Y_KEY";

    private View scrollView;
    private int scrollY;
    private final Runnable updatePositionAction = new Runnable() {
        public void run() {
            scrollView.scrollTo(0, scrollY);
        }
    };

    /* Clears cached position of ScrollView */
    protected void clearPosition() {
        scrollY = 0;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollView = findViewByType(ScrollView.class, getView());
    }

    @Override
    public void onLoaded() {
        super.onLoaded();
        getPostHandler().post(updatePositionAction);
    }

    @Override
    public void onPause() {
        super.onPause();
        scrollY = scrollView.getScrollY();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            scrollY = savedInstanceState.getInt(SCROLL_Y_KEY, 0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SCROLL_Y_KEY, scrollY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        scrollView = null;
    }
}
