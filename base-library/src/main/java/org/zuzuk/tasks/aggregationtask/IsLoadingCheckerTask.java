package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 03/02/2015.
 * Task that calculating if aggregation task needs next loading
 */
class IsLoadingCheckerTask extends Task<Boolean> {
    private final AggregationTaskController taskController;
    private final AggregationTaskStageState stageState;

    public IsLoadingCheckerTask(AggregationTaskController taskController,
                                AggregationTaskStageState stageState) {
        super(Boolean.class);
        this.taskController = taskController;
        this.stageState = stageState;
    }

    @Override
    public Boolean execute() throws Exception {
        return taskController.task.isLoadingNeeded(stageState);
    }
}
