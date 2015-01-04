package org.zuzuk.tasks;

import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Class that contains wrapped requests listeners and controls task state
 */
class AggregationTaskController {
    private final TaskExecutorHelper taskExecutorHelper;
    // task that is controlling by this object
    final AggregationTask task;
    // listeners that is wrapped around passed into TaskExecutorHelper listener
    private final List<RequestListener> wrappedRequestListeners = new ArrayList<>();
    // fails that occurred during task execution
    private final List<Exception> collectedFails = new ArrayList<>();
    private final boolean isInBackground;
    // is this task can only touch local cache or not
    final boolean isLoadingFromCache;

    boolean isNoOneListenToRequests() {
        return wrappedRequestListeners.isEmpty();
    }

    AggregationTaskController(TaskExecutorHelper taskExecutorHelper,
                              AggregationTask task,
                              boolean isInBackground,
                              boolean isLoadingFromCache) {
        this.taskExecutorHelper = taskExecutorHelper;
        this.task = task;
        this.isInBackground = isInBackground;
        this.isLoadingFromCache = isLoadingFromCache;
    }

    void registerListener(RequestListener requestListener) {
        wrappedRequestListeners.add(requestListener);
    }

    void addFail(Exception ex) {
        collectedFails.add(ex);
    }

    void unregisterListener(RequestListener requestListener) {
        wrappedRequestListeners.remove(requestListener);
        if (isNoOneListenToRequests()) {
            finishTask();
        }
    }

    void finishTask(){
        if (task.isLoaded(isLoadingFromCache)) {
            task.onLoaded(isInBackground, isLoadingFromCache);
        } else {
            task.onFailed(isInBackground, isLoadingFromCache, collectedFails);
        }
    }
}