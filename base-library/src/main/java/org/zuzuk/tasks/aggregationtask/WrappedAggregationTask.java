package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 08/02/2015.
 * Aggregation task over aggregation. Enjoy!
 */
public class WrappedAggregationTask<TRequestAndTaskExecutor extends RequestAndTaskExecutor> implements AggregationTask<TRequestAndTaskExecutor> {

    private final AggregationTask<TRequestAndTaskExecutor> aggregationTaskToWrap;

    public WrappedAggregationTask(AggregationTask<TRequestAndTaskExecutor> aggregationTaskToWrap) {
        this.aggregationTaskToWrap = aggregationTaskToWrap;
    }

    @Override
    public boolean isLoadingNeeded(AggregationTaskStageState currentTaskStageState) {
        return aggregationTaskToWrap.isLoadingNeeded(currentTaskStageState);
    }

    @Override
    public boolean isLoaded(AggregationTaskStageState currentTaskStageState) {
        return aggregationTaskToWrap.isLoaded(currentTaskStageState);
    }

    @Override
    public void load(TRequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
        aggregationTaskToWrap.load(executor, currentTaskStageState);
    }

    @Override
    public void onLoadingStarted(AggregationTaskStageState currentTaskStageState) {
        aggregationTaskToWrap.onLoadingStarted(currentTaskStageState);
    }

    @Override
    public void onLoaded(AggregationTaskStageState currentTaskStageState) {
        aggregationTaskToWrap.onLoaded(currentTaskStageState);
    }

    @Override
    public void onFailed(AggregationTaskStageState currentTaskStageState) {
        aggregationTaskToWrap.onFailed(currentTaskStageState);
    }

    @Override
    public void processTask(Task task, AggregationTaskStageState currentTaskStageState) {
        aggregationTaskToWrap.processTask(task, currentTaskStageState);
    }

}
