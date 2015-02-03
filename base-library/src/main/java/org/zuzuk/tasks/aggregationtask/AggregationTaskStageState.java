package org.zuzuk.tasks.aggregationtask;

/**
 * Created by Gavriil Sitnikov on 03/02/2015.
 * Current status of data on some state
 */
class AggregationTaskStageState {
    final AggregationTaskStage taskStage;
    final boolean isLoaded;
    final boolean isLoadingNeeded;

    AggregationTaskStageState(AggregationTaskStage taskStage, boolean isLoaded, boolean isLoadingNeeded) {
        this.taskStage = taskStage;
        this.isLoaded = isLoaded;
        this.isLoadingNeeded = isLoadingNeeded;
    }
}
