package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

public interface TaskProcessor {

    void processTask(Task task, AggregationTaskStageState currentTaskStageState);

}
