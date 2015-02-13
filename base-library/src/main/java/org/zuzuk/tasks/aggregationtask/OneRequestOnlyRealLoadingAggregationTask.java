package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.remote.base.RemoteRequest;

public abstract class OneRequestOnlyRealLoadingAggregationTask<TResult> extends OnlyRealLoadingAggregationTask {

    private final RemoteRequest<TResult> request;
    private final RequestListener<TResult> requestListener;

    public OneRequestOnlyRealLoadingAggregationTask(RemoteRequest<TResult> request, RequestListener<TResult> requestListener) {
        this.request = request;
        this.requestListener = requestListener;
    }

    @Override
    protected <TRequestAndTaskExecutor extends RequestAndTaskExecutor> void load(TRequestAndTaskExecutor executor) {
        executor.executeRequest(request, requestListener);
    }

}
