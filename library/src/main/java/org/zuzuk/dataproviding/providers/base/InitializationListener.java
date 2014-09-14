package org.zuzuk.dataproviding.providers.base;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Listener that listen to data initialization events
 */
public interface InitializationListener {

    /* Raises when data initialized */
    void onInitialized();

    /* Raises when data initialization failed */
    void onInitializationFailed(Exception ex);
}
