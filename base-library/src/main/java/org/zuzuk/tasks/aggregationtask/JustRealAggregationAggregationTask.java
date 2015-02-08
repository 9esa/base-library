package org.zuzuk.tasks.aggregationtask;

class JustRealAggregationAggregationTask extends SimpleAggregationTask {

    private final AggregationTaskListener taskListener;

    JustRealAggregationAggregationTask(AggregationTaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    public void onStarted(AggregationTaskStageState currentTaskStageState) {
        if (taskListener != null) {
            taskListener.onLoadingStarted(currentTaskStageState);
        }
    }

    @Override
    protected void realLoad(AggregationTaskStageState currentTaskStageState) {
    }

    @Override
    public void onFinished(AggregationTaskStageState currentTaskStageState) {
        if (taskListener != null) {
            taskListener.onLoadingFinished(currentTaskStageState);
        }
    }
}
