package org.zuzuk.tasks.realloading;

import org.zuzuk.tasks.aggregationtask.AggregationTask;
import org.zuzuk.tasks.aggregationtask.AggregationTaskStage;
import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;
import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;
import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 08/02/2015.
 * Simple loading task that executes only REAL_LOADING stage
 */
public abstract class OnlyRealLoadingAggregationTask implements AggregationTask, RealLoadingAggregationTaskListener {

    private RealLoadingAggregationTaskListener realLoadingAggregationTaskListener;

    public OnlyRealLoadingAggregationTask(RealLoadingAggregationTaskListener realLoadingAggregationTaskListener) {
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

    protected abstract void realLoad(RequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState);

    @Override
    public boolean isLoadingNeeded(AggregationTaskStageState currentTaskStageState) {
        return currentTaskStageState.getTaskStage() != AggregationTaskStage.REAL_LOADING;
    }

    @Override
    public boolean isLoaded(AggregationTaskStageState currentTaskStageState) {
        return currentTaskStageState.getTaskStage() == AggregationTaskStage.REAL_LOADING;
    }

    @Override
    public void load(RequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
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
