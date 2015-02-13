package org.zuzuk.ui.activities;

import android.os.Bundle;

import org.zuzuk.tasks.aggregationtask.AggregationTask;
import org.zuzuk.tasks.aggregationtask.AggregationTaskExecutor;
import org.zuzuk.tasks.aggregationtask.TaskExecutorHelper;

/**
 * Created by Gavriil Sitnikov on 06/02/2015.
 * Base activity that can execute tasks and requests
 */
public abstract class BaseExecutorActivity extends BaseActivity implements AggregationTaskExecutor {

    private TaskExecutorHelper taskExecutorHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskExecutorHelper = createTaskExecutorHelper();
    }

    protected abstract TaskExecutorHelper createTaskExecutorHelper();

    @Override
    protected void onResume() {
        super.onResume();
        taskExecutorHelper.onResume(this);
    }

    @Override
    public void executeAggregationTask(AggregationTask aggregationTask) {
        taskExecutorHelper.executeAggregationTask(aggregationTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        taskExecutorHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        taskExecutorHelper = null;
    }

}
