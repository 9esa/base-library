package org.zuzuk.ui.fragments;

/**
 * Created by Gavriil Sitnikov on 08/10/2014.
 * Base interface to listen fragment changing
 */
public interface OnFragmentChangedListener {

    /* Raises by fragment to notify that it is showing */
    void onFragmentChanged(BaseFragment fragment);
}
