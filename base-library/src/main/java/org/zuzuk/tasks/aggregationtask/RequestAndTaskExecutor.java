package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;

public class RequestAndTaskExecutor {

    private TaskExecutorHelper taskExecutorHelper;

    public RequestAndTaskExecutor(TaskExecutorHelper taskExecutorHelper) {
        this.taskExecutorHelper = taskExecutorHelper;
    }

    public <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequest(request, requestListener);
    }

    public void executeTask(LocalTask task) {
        executeTask(task, null);
    }

    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        taskExecutorHelper.executeTask(task, requestListener);
    }

    @SuppressWarnings("unchecked")
    public <TRequestAndTaskExecutor extends RequestAndTaskExecutor> TRequestAndTaskExecutor cast() {
        return (TRequestAndTaskExecutor) this;
    }

}
