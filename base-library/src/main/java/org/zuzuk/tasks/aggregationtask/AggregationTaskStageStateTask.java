package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 03/02/2015.
 * Task that calculating aggregation task state
 */
class AggregationTaskStageStateTask extends Task<AggregationTaskStageState> {
    private final AggregationTaskController taskController;
    private final AggregationTaskStage taskStage;
    private final AggregationTaskStageState previousStageState;

    public AggregationTaskStageStateTask(AggregationTaskController taskController,
                                         AggregationTaskStage taskStage, AggregationTaskStageState previousStageState) {
        super(AggregationTaskStageState.class);
        this.taskController = taskController;
        this.taskStage = taskStage;
        this.previousStageState = previousStageState;
    }

    @Override
    public AggregationTaskStageState execute() throws Exception {
        return new AggregationTaskStageState(taskStage,
                taskController.task.isLoaded(taskStage),
                taskController.task.isLoadingNeeded(taskStage),
                previousStageState);
    }
}
