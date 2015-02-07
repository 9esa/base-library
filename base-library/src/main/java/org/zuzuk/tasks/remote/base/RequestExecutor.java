package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.aggregationtask.AggregationTaskListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply async remote request executing
 */
public interface RequestExecutor {

    /* Returns spice manager that executes requests */
    public SpiceManager getRemoteSpiceManager();

    /* Executes request */
    public <T> void executeRequest(RemoteRequest<T> request, RequestListener<T> requestListener);

    /* Add wrapped request to generated AggregationTask and executes it only on REAL_LOADING stage */
    public <T> void executeRealLoadingRequest(RemoteRequest<T> request,
                                              RequestListener<T> requestListener,
                                              AggregationTaskListener taskListener);
}