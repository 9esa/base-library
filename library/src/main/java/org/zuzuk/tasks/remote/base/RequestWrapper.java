package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Wrapper around request
 */
public abstract class RequestWrapper<T> {
    private final RequestListener<T> requestListener;

    public RequestWrapper(RequestListener<T> requestListener) {
        this.requestListener = requestListener;
    }

    /* Get prepared request before executing */
    protected abstract SpiceRequest<T> getPreparedRequest();

    /* Executes cached request */
    public void execute(SpiceManager spiceManager) {
        if (spiceManager.isStarted()) {
            spiceManager.execute(getPreparedRequest(), requestListener);
        }
    }
}
