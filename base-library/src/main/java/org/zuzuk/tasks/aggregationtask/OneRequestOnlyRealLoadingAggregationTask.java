package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.remote.base.RemoteRequest;

public class OneRequestOnlyRealLoadingAggregationTask<TResult, TRequestAndTaskExecutor extends RequestAndTaskExecutor<TRequestAndTaskExecutor>>
        extends OnlyRealLoadingAggregationTask<TRequestAndTaskExecutor> {

    private final RemoteRequest<TResult> request;
    private final RequestListener<TResult> requestListener;

    public OneRequestOnlyRealLoadingAggregationTask(RemoteRequest<TResult> request, RequestListener<TResult> requestListener) {
        this.request = request;
        this.requestListener = requestListener;
    }

    @Override
    protected void realLoad(TRequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
        executor.executeRequest(request, requestListener);
    }

}
