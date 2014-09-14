package org.zuzuk.dataproviding.requests.remote.base;

import com.octo.android.robospice.SpiceManager;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply creating spice manager. usually it should be Application class instance
 */
public interface SpiceManagerProvider {

    /* Creates SpiceManager object */
    public SpiceManager createSpiceManager();
}
