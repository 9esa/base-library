package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

class JustRealLoadingAggregationTask implements AggregationTask {

    private final AggregationTaskListener taskListener;

    JustRealLoadingAggregationTask(AggregationTaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    public boolean isLoadingNeeded(AggregationTaskStageState currentTaskStageState) {
        return true;
    }

    @Override
    public boolean isLoaded(AggregationTaskStageState currentTaskStageState) {
        return true;
    }

    @Override
    public void load(AggregationTaskStageState currentTaskStageState) {
    }

    @Override
    public void onLoadingStarted(AggregationTaskStageState currentTaskStageState) {
        if (taskListener != null) {
            taskListener.onLoadingStarted(currentTaskStageState);
        }
    }

    @Override
    public void onLoaded(AggregationTaskStageState currentTaskStageState) {
        if (taskListener != null) {
            taskListener.onLoadingFinished(currentTaskStageState);
        }
    }

    @Override
    public void onFailed(AggregationTaskStageState currentTaskStageState) {
    }

    @Override
    public void processTask(Task task, AggregationTaskStageState currentTaskStageState) {
    }

}
