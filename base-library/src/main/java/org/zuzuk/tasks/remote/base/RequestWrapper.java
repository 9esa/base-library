package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Wrapper around request
 */
public abstract class RequestWrapper<T> implements RequestListener<T> {

    /* Get prepared request before executing */
    public abstract RemoteRequest<T> getPreparedRequest();
}
