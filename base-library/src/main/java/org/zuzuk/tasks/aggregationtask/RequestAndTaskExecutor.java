package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;

public class RequestAndTaskExecutor {

    private AggregationTaskController aggregationTaskController;

    void setAggregationTaskController(AggregationTaskController aggregationTaskController) {
        this.aggregationTaskController = aggregationTaskController;
    }

    public <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        aggregationTaskController.executeRequest(request, requestListener);
    }

    public void executeTask(LocalTask task) {
        executeTask(task, null);
    }

    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        aggregationTaskController.executeTask(task, requestListener);
    }

}
