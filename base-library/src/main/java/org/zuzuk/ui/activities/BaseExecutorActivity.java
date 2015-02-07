package org.zuzuk.ui.activities;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.aggregationtask.AggregationTask;
import org.zuzuk.tasks.aggregationtask.AggregationTaskListener;
import org.zuzuk.tasks.aggregationtask.TaskExecutorHelper;
import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;

/**
 * Created by Gavriil Sitnikov on 06/02/2015.
 * Base activity that can execute tasks and requests
 */
public class BaseExecutorActivity extends BaseActivity
        implements TaskExecutor, RequestExecutor {
    private TaskExecutorHelper taskExecutorHelper = new TaskExecutorHelper();

    @Override
    protected void onResume() {
        super.onResume();
        taskExecutorHelper.onResume(this);
    }

    public void executeAggregationTask(AggregationTask aggregationTask) {
        taskExecutorHelper.executeAggregationTask(aggregationTask);
    }

    @Override
    public SpiceManager getSpiceManager() {
        return taskExecutorHelper.getSpiceManager();
    }

    @Override
    public <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequest(request, requestListener);
    }

    @Override
    public <T> void executeRealLoadingRequest(RemoteRequest<T> request,
                                              RequestListener<T> requestListener,
                                              AggregationTaskListener taskListener) {
        taskExecutorHelper.executeRealLoadingRequest(request, requestListener, taskListener);
    }

    @Override
    public void executeTask(LocalTask task) {
        taskExecutorHelper.executeTask(task);
    }

    @Override
    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        taskExecutorHelper.executeTask(task, requestListener);
    }

    @Override
    public <T> void executeRealLoadingTask(Task<T> task,
                                           RequestListener<T> requestListener,
                                           AggregationTaskListener taskListener) {
        taskExecutorHelper.executeRealLoadingTask(task, requestListener, taskListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        taskExecutorHelper.onPause();
    }
}
