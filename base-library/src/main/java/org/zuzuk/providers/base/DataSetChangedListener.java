package org.zuzuk.providers.base;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Listener that listen to data set changing events
 */
public interface DataSetChangedListener {

    /* Raises when data changing */
    void onDataSetChanged();
}