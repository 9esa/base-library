package org.zuzuk.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

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
import org.zuzuk.tasks.remote.base.RequestWrapper;

/**
 * Created by Gavriil Sitnikov on 06/02/2015.
 * Base fragment that can execute tasks and requests
 */
public class BaseExecutorFragment extends BaseFragment
        implements TaskExecutor, RequestExecutor {
    private TaskExecutorHelper taskExecutorHelper = new TaskExecutorHelper();

    @Override
    public void onResume() {
        super.onResume();
        taskExecutorHelper.onResume(getActivity());
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
    public <T> void executeRequest(RequestWrapper<T> requestWrapper) {
        taskExecutorHelper.executeRequest(requestWrapper.getPreparedRequest(), requestWrapper);
    }

    @Override
    public <T> void executeRealLoadingRequest(RequestWrapper<T> requestWrapper,
                                              AggregationTaskListener taskListener) {
        taskExecutorHelper.executeRealLoadingRequest(requestWrapper.getPreparedRequest(), requestWrapper, taskListener);
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
    public void onPause() {
        super.onPause();
        taskExecutorHelper.onPause();
    }
}
