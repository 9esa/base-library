package org.zuzuk.providers.base;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Listener that listen to data initialization events
 */
public interface InitializationListener {

    /* Raises when data initialized */
    void onInitialized();

    /* Raises when data initialization failed */
    void onInitializationFailed(List<Exception> exceptions);
}
