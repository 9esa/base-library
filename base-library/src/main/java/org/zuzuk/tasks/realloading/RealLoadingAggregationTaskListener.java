package org.zuzuk.tasks.realloading;

import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;

public interface RealLoadingAggregationTaskListener {

    public void onRealLoadingStarted(AggregationTaskStageState currentTaskStageState);

    public void onRealLoaded(AggregationTaskStageState currentTaskStageState);

    public void onRealFailed(AggregationTaskStageState currentTaskStageState);

}
