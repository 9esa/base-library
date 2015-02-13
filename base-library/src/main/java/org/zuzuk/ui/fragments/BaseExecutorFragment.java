package org.zuzuk.ui.fragments;

import android.os.Bundle;

import org.zuzuk.tasks.aggregationtask.AggregationTask;
import org.zuzuk.tasks.aggregationtask.AggregationTaskExecutor;
import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;
import org.zuzuk.tasks.aggregationtask.TaskExecutorHelper;

/**
 * Created by Gavriil Sitnikov on 06/02/2015.
 * Base fragment that can execute tasks and requests
 */
public abstract class BaseExecutorFragment<TRequestAndTaskExecutor extends RequestAndTaskExecutor> extends BaseFragment implements AggregationTaskExecutor<TRequestAndTaskExecutor> {

    private TaskExecutorHelper<TRequestAndTaskExecutor> taskExecutorHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskExecutorHelper = createTaskExecutorHelper();
    }

    protected abstract TaskExecutorHelper<TRequestAndTaskExecutor> createTaskExecutorHelper();

    @Override
    public void onResume() {
        super.onResume();
        taskExecutorHelper.onResume(getActivity());
    }

    @Override
    public void executeAggregationTask(AggregationTask<TRequestAndTaskExecutor> aggregationTask) {
        taskExecutorHelper.executeAggregationTask(aggregationTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        taskExecutorHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        taskExecutorHelper = null;
    }

}
