package org.zuzuk.tasks.realloading;

import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;
import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;

public abstract class OnlyRealLoadingAggregationTaskWithListener<TRequestAndTaskExecutor extends RequestAndTaskExecutor>
        extends OnlyRealLoadingAggregationTask<TRequestAndTaskExecutor> {

    private RealLoadingAggregationTaskListener realLoadingAggregationTaskListener;

    public OnlyRealLoadingAggregationTaskWithListener(RealLoadingAggregationTaskListener realLoadingAggregationTaskListener) {
        this.realLoadingAggregationTaskListener = realLoadingAggregationTaskListener;
    }

    @Override
    public void onRealLoadingStarted(AggregationTaskStageState currentTaskStageState) {
        realLoadingAggregationTaskListener.onRealLoadingStarted(currentTaskStageState);
    }

    @Override
    public void onRealLoaded(AggregationTaskStageState currentTaskStageState) {
        realLoadingAggregationTaskListener.onRealLoaded(currentTaskStageState);
    }

    @Override
    public void onRealFailed(AggregationTaskStageState currentTaskStageState) {
        realLoadingAggregationTaskListener.onRealFailed(currentTaskStageState);
    }

}
