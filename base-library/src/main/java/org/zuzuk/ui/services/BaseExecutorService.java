package org.zuzuk.ui.services;

import org.zuzuk.tasks.aggregationtask.AggregationTask;
import org.zuzuk.tasks.aggregationtask.AggregationTaskExecutor;
import org.zuzuk.tasks.aggregationtask.TaskExecutorHelper;

/**
 * Created by Gavriil Sitnikov on 03/10/2014.
 * Base service that can execute requests
 */
public abstract class BaseExecutorService extends BaseService implements AggregationTaskExecutor {

    private TaskExecutorHelper taskExecutorHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        taskExecutorHelper = TaskExecutorHelper.newInstance(this);
        taskExecutorHelper.onResume(this);
    }

    @Override
    public void executeAggregationTask(AggregationTask aggregationTask) {
        taskExecutorHelper.executeAggregationTask(aggregationTask);
    }

    @Override
    public void cancelAggregationTask(AggregationTask aggregationTask) {
        taskExecutorHelper.cancelAggregationTask(aggregationTask);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        taskExecutorHelper.onPause();
        taskExecutorHelper = null;
    }

    public TaskExecutorHelper getTaskExecutorHelper() {
        return taskExecutorHelper;
    }

}