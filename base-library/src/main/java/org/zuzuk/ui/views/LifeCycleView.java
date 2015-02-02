package org.zuzuk.ui.views;

/**
 * Created by Gavriil Sitnikov on 22/12/2014.
 * View with lifecycle
 */
public interface LifeCycleView {

    /* Calls when view becomes in started state */
    void onStart();

    /* Calls when view becomes in resumed state */
    void onResume();

    /* Calls when view becomes in paused state */
    void onPause();

    /* Calls when view becomes in stooped state */
    void onStop();
}
