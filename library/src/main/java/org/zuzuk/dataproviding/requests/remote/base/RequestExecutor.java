package org.zuzuk.dataproviding.requests.remote.base;

import org.zuzuk.dataproviding.requests.base.ResultListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply async remote request executing
 */
public interface RequestExecutor {

    /* Executes request */
    public <T> void executeRequest(RemoteRequest<T> request, ResultListener<T> resultListener);
}