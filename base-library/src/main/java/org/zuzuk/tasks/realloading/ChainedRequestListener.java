package org.zuzuk.tasks.realloading;

import com.octo.android.robospice.persistence.exception.SpiceException;

import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;

public interface ChainedRequestListener<TResult> {

    public void onRequestSuccess(TResult result, RequestAndTaskExecutor executor);

    public void onRequestFailure(SpiceException spiceException, RequestAndTaskExecutor executor);

}
