package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 08/02/2015.
 * Simple loading task that executes only REAL_LOADING stage
 */
public abstract class SimpleAggregationTask implements AggregationTask {

    public abstract void onStarted(AggregationTaskStageState currentTaskStageState);

    protected abstract void realLoad(AggregationTaskStageState currentTaskStageState);

    public abstract void onFinished(AggregationTaskStageState currentTaskStageState);

    @Override
    public boolean isLoadingNeeded(AggregationTaskStageState currentTaskStageState) {
        return currentTaskStageState.getTaskStage() != AggregationTaskStage.REAL_LOADING;
    }

    @Override
    public boolean isLoaded(AggregationTaskStageState currentTaskStageState) {
        return currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING;
    }

    @Override
    public void load(AggregationTaskStageState currentTaskStageState) {
        if (currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING) {
            realLoad(currentTaskStageState);
        }
    }

    @Override
    public void onLoadingStarted(AggregationTaskStageState currentTaskStageState) {
        if (currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING) {
            onStarted(currentTaskStageState);
        }
    }

    @Override
    public void onLoaded(AggregationTaskStageState currentTaskStageState) {
        onFinished(currentTaskStageState);
    }

    @Override
    public void onFailed(AggregationTaskStageState currentTaskStageState) {
    }

    @Override
    public void processTask(Task task, AggregationTaskStageState currentTaskStageState) {
    }
}
