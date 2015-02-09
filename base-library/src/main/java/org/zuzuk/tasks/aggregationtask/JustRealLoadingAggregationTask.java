package org.zuzuk.tasks.aggregationtask;

abstract class JustRealLoadingAggregationTask extends SimpleAggregationTask {

    private final AggregationTaskListener taskListener;

    JustRealLoadingAggregationTask(AggregationTaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    public void onStarted(AggregationTaskStageState currentTaskStageState) {
        if (taskListener != null) {
            taskListener.onLoadingStarted(currentTaskStageState);
        }
    }

    @Override
    public void onFinished(AggregationTaskStageState currentTaskStageState) {
        if (taskListener != null) {
            taskListener.onLoadingFinished(currentTaskStageState);
        }
    }
}
