package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 08/02/2015.
 * Simple loading task that executes only REAL_LOADING stage
 */
public abstract class OnlyRealLoadingAggregationTask implements AggregationTask {

    protected abstract void onLoadingStarted();

    protected abstract <TRequestAndTaskExecutor extends RequestAndTaskExecutor> void load(TRequestAndTaskExecutor executor);

    protected abstract void onLoaded();

    protected abstract void onFailed(List<Exception> exceptions);

    @Override
    public boolean isLoadingNeeded(AggregationTaskStageState currentTaskStageState) {
        return currentTaskStageState.getTaskStage() != AggregationTaskStage.REAL_LOADING;
    }

    @Override
    public boolean isLoaded(AggregationTaskStageState currentTaskStageState) {
        return currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING;
    }

    @Override
    public <TRequestAndTaskExecutor extends RequestAndTaskExecutor> void load(TRequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
        if (currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING) {
            load(executor);
        }
    }

    @Override
    public void onLoadingStarted(AggregationTaskStageState currentTaskStageState) {
        if (currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING) {
            onLoadingStarted();
        }
    }

    @Override
    public void onLoaded(AggregationTaskStageState currentTaskStageState) {
        onLoaded();
    }

    @Override
    public void onFailed(AggregationTaskStageState currentTaskStageState) {
        if (currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING) {
            onFailed(currentTaskStageState.getExceptions());
        }
    }

    @Override
    public void processTask(Task task, AggregationTaskStageState currentTaskStageState) {
    }

}
