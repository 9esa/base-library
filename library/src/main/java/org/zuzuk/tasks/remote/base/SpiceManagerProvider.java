package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.SpiceManager;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply creating spice managers. Usually it should be Application class instance
 */
public interface SpiceManagerProvider {

    /* Creates SpiceManager object for remote requests executing */
    public SpiceManager createRemoteSpiceManager();

    /* Creates SpiceManager object for local tasks executing */
    public SpiceManager createLocalSpiceManager();
}
