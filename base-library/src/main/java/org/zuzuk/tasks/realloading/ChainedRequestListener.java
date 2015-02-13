package org.zuzuk.tasks.realloading;

import com.octo.android.robospice.persistence.exception.SpiceException;

import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;

public interface ChainedRequestListener<TResult, TRequestAndTaskExecutor extends RequestAndTaskExecutor> {

    public void onRequestSuccess(TResult result, TRequestAndTaskExecutor executor);

    public void onRequestFailure(SpiceException spiceException, TRequestAndTaskExecutor executor);

}
