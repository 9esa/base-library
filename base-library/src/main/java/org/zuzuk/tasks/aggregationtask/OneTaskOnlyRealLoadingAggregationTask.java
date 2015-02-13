package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;

public abstract class OneTaskOnlyRealLoadingAggregationTask<TResult> extends OnlyRealLoadingAggregationTask {

    private final Task<TResult> task;
    private final RequestListener<TResult> requestListener;

    public OneTaskOnlyRealLoadingAggregationTask(Task<TResult> task, RequestListener<TResult> requestListener) {
        this.task = task;
        this.requestListener = requestListener;
    }

    @Override
    protected <TRequestAndTaskExecutor extends RequestAndTaskExecutor> void load(TRequestAndTaskExecutor executor) {
        executor.executeTask(task, requestListener);
    }

}
