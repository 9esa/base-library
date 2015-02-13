package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.remote.base.RemoteRequest;

public abstract class OneRequestOnlyRealLoadingAggregationTask<TResult, TRequestAndTaskExecutor extends RequestAndTaskExecutor>
        extends OnlyRealLoadingAggregationTask<TRequestAndTaskExecutor> {

    private final RemoteRequest<TResult> task;
    private final RequestListener<TResult> requestListener;

    public OneRequestOnlyRealLoadingAggregationTask(RemoteRequest<TResult> request, RequestListener<TResult> requestListener) {
        this.task = request;
        this.requestListener = requestListener;
    }

    @Override
    protected void load(TRequestAndTaskExecutor executor) {
        executor.executeRequest(task, requestListener);
    }

}
