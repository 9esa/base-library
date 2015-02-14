package org.zuzuk.tasks.realloading;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;
import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;
import org.zuzuk.tasks.base.Task;

public class BaseTaskWrapper extends OnlyRealLoadingAggregationTask {

    private final Task task;
    private final ChainedRequestListener chainedRequestListener;

    public <TResult> BaseTaskWrapper(Task<TResult> task,
                                     ChainedRequestListener<TResult> chainedRequestListener,
                                     RealLoadingAggregationTaskListener realLoadingAggregationTaskListener) {
        super(realLoadingAggregationTaskListener);
        this.task = task;
        this.chainedRequestListener = chainedRequestListener;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void realLoad(final RequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
        if (task != null) {
            executor.executeTask(task, new RequestListener() {
                @Override
                public void onRequestFailure(SpiceException exception) {
                    if (chainedRequestListener != null) {
                        chainedRequestListener.onRequestFailure(exception, executor);
                    }
                }

                @Override
                public void onRequestSuccess(Object result) {
                    if (chainedRequestListener != null) {
                        chainedRequestListener.onRequestSuccess(result, executor);
                    }
                }
            });
        }
    }

}
