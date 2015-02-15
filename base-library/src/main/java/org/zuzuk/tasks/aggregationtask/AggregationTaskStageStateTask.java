package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 03/02/2015.
 * Task that calculating aggregation task state
 */
class AggregationTaskStageStateTask extends Task<AggregationTaskStageState> {
    private final AggregationTaskController taskController;
    private final AggregationTaskStageState stageState;

    public AggregationTaskStageStateTask(AggregationTaskController taskController, AggregationTaskStageState stageState) {
        super(AggregationTaskStageState.class);
        this.taskController = taskController;
        this.stageState = stageState;
    }

    @Override
    public AggregationTaskStageState execute() throws Exception {
        boolean isLoaded = taskController.task.isLoaded(stageState);
        stageState.setIsLoaded(isLoaded);
        boolean isLoadingNeeded = taskController.task.isLoadingNeeded(stageState);
        stageState.setIsLoadingNeeded(isLoadingNeeded);
        return stageState;
    }
}
