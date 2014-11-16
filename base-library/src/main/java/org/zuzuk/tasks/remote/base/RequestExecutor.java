package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply async remote request executing
 */
public interface RequestExecutor {

    /* Returns spice manager that executes requests */
    public SpiceManager getSpiceManager();

    /* Executes request in foreground */
    public <T> void executeRequest(RemoteRequest<T> request, RequestListener<T> requestListener);

    /* Executes wrapped request in foreground */
    public <T> void executeRequest(RequestWrapper<T> requestWrapper);

    /* Executes request in background */
    public <T> void executeRequestBackground(RemoteRequest<T> request,
                                   RequestListener<T> requestListener);

    /* Executes wrapped request in background */
    public <T> void executeRequestBackground(RequestWrapper<T> requestWrapper);
}