package org.zuzuk.tasks.aggregationtask;

/**
 * Created by Gavriil Sitnikov on 03/02/2015.
 * Default aggregation task
 */
public abstract class DefaultAggregationTask implements AggregationTask {

    @Override
    public boolean isLoadingNeeded(AggregationTaskStage taskState) {
        return taskState != AggregationTaskStage.LOADING_REMOTELY;
    }

    @Override
    public boolean isLoaded(AggregationTaskStage taskState) {
        return true;
    }
}
