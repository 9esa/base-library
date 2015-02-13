package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 08/02/2015.
 * Simple loading task that executes only REAL_LOADING stage
 */
public abstract class OnlyRealLoadingAggregationTask<TRequestAndTaskExecutor extends RequestAndTaskExecutor<TRequestAndTaskExecutor>>
        implements AggregationTask<TRequestAndTaskExecutor> {

    protected void onRealLoadingStarted(AggregationTaskStageState currentTaskStageState) {
    }

    protected abstract void realLoad(TRequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState);

    protected void onRealLoaded(AggregationTaskStageState currentTaskStageState) {
    }

    protected void onRealFailed(AggregationTaskStageState currentTaskStageState) {
    }

    @Override
    public boolean isLoadingNeeded(AggregationTaskStageState currentTaskStageState) {
        return currentTaskStageState.getTaskStage() != AggregationTaskStage.REAL_LOADING;
    }

    @Override
    public boolean isLoaded(AggregationTaskStageState currentTaskStageState) {
        return currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING;
    }

    @Override
    public void load(TRequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
        if (currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING) {
            realLoad(executor, currentTaskStageState);
        }
    }

    @Override
    public void onLoadingStarted(AggregationTaskStageState currentTaskStageState) {
        if (currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING) {
            onRealLoadingStarted(currentTaskStageState);
        }
    }

    @Override
    public void onLoaded(AggregationTaskStageState currentTaskStageState) {
        onRealLoaded(currentTaskStageState);
    }

    @Override
    public void onFailed(AggregationTaskStageState currentTaskStageState) {
        if (currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING) {
            onRealFailed(currentTaskStageState);
        }
    }

    @Override
    public void processTask(Task task, AggregationTaskStageState currentTaskStageState) {
    }

}
