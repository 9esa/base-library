package org.zuzuk.ui.fragments;

/**
 * Created by Gavriil Sitnikov on 08/10/2014.
 * Base interface to listen fragment changing
 */
public interface OnFragmentStartedListener {

    /* Raises by fragment to notify that it is started */
    void onFragmentStarted(BaseFragment fragment);
}
