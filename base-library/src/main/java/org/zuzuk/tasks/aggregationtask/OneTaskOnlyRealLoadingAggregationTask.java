package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;

public class OneTaskOnlyRealLoadingAggregationTask<TResult, TRequestAndTaskExecutor extends RequestAndTaskExecutor>
        extends OnlyRealLoadingAggregationTask<TRequestAndTaskExecutor> {

    private final Task<TResult> task;
    private final RequestListener<TResult> requestListener;

    public OneTaskOnlyRealLoadingAggregationTask(Task<TResult> task, RequestListener<TResult> requestListener) {
        this.task = task;
        this.requestListener = requestListener;
    }

    @Override
    protected void realLoad(TRequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
        executor.executeTask(task, requestListener);
    }

}
