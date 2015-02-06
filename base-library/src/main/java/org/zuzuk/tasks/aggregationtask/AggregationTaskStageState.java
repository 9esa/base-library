package org.zuzuk.tasks.aggregationtask;

/**
 * Created by Gavriil Sitnikov on 03/02/2015.
 * Current status of data on some state
 */
public class AggregationTaskStageState {
    public final AggregationTaskStage taskStage;
    public final boolean isLoaded;
    public final boolean isLoadingNeeded;
    public final AggregationTaskStageState previousStageState;

    /* Finds state of specific stage in previous stages */
    public AggregationTaskStageState findByStage(AggregationTaskStage stage) {
        if (taskStage == stage) {
            return this;
        }
        return previousStageState != null ? previousStageState.findByStage(stage) : null;
    }

    AggregationTaskStageState(AggregationTaskStage taskStage,
                              boolean isLoaded,
                              boolean isLoadingNeeded,
                              AggregationTaskStageState previousStageState) {
        this.taskStage = taskStage;
        this.isLoaded = isLoaded;
        this.isLoadingNeeded = isLoadingNeeded;
        this.previousStageState = previousStageState;
    }
}
