package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply async remote request executing
 */
public interface RequestExecutor {

    /* Executes request */
    public <T> void executeRequest(RemoteRequest<T> request, RequestListener<T> requestListener);

    /* Executes wrapped request */
    public <T> void executeRequest(RequestWrapper<T> requestWrapper);
}