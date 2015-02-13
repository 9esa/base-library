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
        if (realLoadingAggregationTaskListener != null) {
            realLoadingAggregationTaskListener.onRealLoadingStarted(currentTaskStageState);
        }
    }

    @Override
    public void onRealLoaded(AggregationTaskStageState currentTaskStageState) {
        if (realLoadingAggregationTaskListener != null) {
            realLoadingAggregationTaskListener.onRealLoaded(currentTaskStageState);
        }
    }

    @Override
    public void onRealFailed(AggregationTaskStageState currentTaskStageState) {
        if (realLoadingAggregationTaskListener != null) {
            realLoadingAggregationTaskListener.onRealFailed(currentTaskStageState);
        }
    }

}
