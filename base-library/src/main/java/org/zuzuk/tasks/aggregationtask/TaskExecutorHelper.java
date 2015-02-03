package org.zuzuk.tasks.aggregationtask;

import android.content.Context;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.base.RequestWrapper;
import org.zuzuk.tasks.remote.base.SpiceManagerProvider;
import org.zuzuk.utils.Lc;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Helper to work with tasks execution during lifecycle of object
 */
public class TaskExecutorHelper implements RequestExecutor, TaskExecutor {
    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;
    private Context context;
    private AggregationTaskController currentTaskController;
    private boolean isPaused = true;

    @Override
    public SpiceManager getSpiceManager() {
        return remoteSpiceManager;
    }

    /* Returns if executor in paused state so it can't execute requests */
    boolean isPaused() {
        return isPaused;
    }

    /* Creates task for someone who using helper without caring of creating their own aggregation task */
    public AggregationTask createTemporaryTask() {
        throw new RuntimeException("This method should be override to use temporary non-background tasks");
    }

    /* Associated lifecycle method */
    public void onCreate(Context context) {
        this.context = context;
        Context applicationContext = context.getApplicationContext();
        if (applicationContext instanceof SpiceManagerProvider) {
            localSpiceManager = ((SpiceManagerProvider) applicationContext).createLocalSpiceManager();
            remoteSpiceManager = ((SpiceManagerProvider) applicationContext).createRemoteSpiceManager();
        } else
            throw new RuntimeException("To use TaskExecutorHelper your Application class should implement SpiceManagerProvider");
    }

    /* Associated lifecycle method */
    public void onResume() {
        localSpiceManager.start(context);
        remoteSpiceManager.start(context);
        isPaused = false;
    }

    /* Executes aggregation task */
    public void executeAggregationTask(AggregationTask aggregationTask, boolean isInBackground) {
        AggregationTaskController controller = new AggregationTaskController(this, aggregationTask, isInBackground);
        executeAggregationTask(controller, isInBackground);
    }

    private void executeAggregationTask(final AggregationTaskController taskController, final boolean isInBackground) {
        taskController.nextStep();
    }

    @Override
    public <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        executeRequestInternal(request, requestListener);
    }

    @Override
    public <T> void executeRequestBackground(RemoteRequest<T> request,
                                             RequestListener<T> requestListener) {
        executeRequestBackgroundInternal(request, requestListener);
    }

    @Override
    public <T> void executeRequest(RequestWrapper<T> requestWrapper) {
        executeRequestInternal(requestWrapper.getPreparedRequest(), requestWrapper);
    }

    @Override
    public <T> void executeRequestBackground(RequestWrapper<T> requestWrapper) {
        executeRequestBackgroundInternal(requestWrapper.getPreparedRequest(), requestWrapper);
    }

    private <T> void executeRequestInternal(RemoteRequest<T> request,
                                            RequestListener<T> requestListener) {
        boolean wasNoCurrentTaskController = currentTaskController == null;
        // if there is no AggregationTask so we should wrap executing by new temporary AggregationTask
        if (wasNoCurrentTaskController) {
            currentTaskController = new AggregationTaskController(this, createTemporaryTask(), false);
            currentTaskController.taskStage = AggregationTaskStage.LOADING_REMOTELY;
        }

        executeRequestBackground(request, requestListener);

        if (wasNoCurrentTaskController) {
            currentTaskController = null;
        }
    }

    private <T> void executeRequestBackgroundInternal(RemoteRequest<T> request,
                                                      RequestListener<T> requestListener) {
        if (!checkManagersState(request)) {
            return;
        }

        CachedSpiceRequest<T> cacheSpiceRequest = request.wrapAsCacheRequest(remoteSpiceManager);
        if (currentTaskController != null
                && currentTaskController.taskStage == AggregationTaskStage.LOADING_LOCALLY) {
            cacheSpiceRequest.setOffline(true);
        }

        if (currentTaskController != null) {
            remoteSpiceManager.execute(cacheSpiceRequest, wrapForAggregationTask(requestListener));
        } else {
            remoteSpiceManager.execute(cacheSpiceRequest, requestListener);
        }
    }

    @Override
    public void executeTask(LocalTask task) {
        executeTask(task, null);
    }

    @Override
    public void executeTaskBackground(LocalTask task) {
        executeTaskBackground(task, null);
    }

    @Override
    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        boolean wasNoCurrentTaskController = currentTaskController == null;
        // if there is no AggregationTask so we should wrap executing by new temporary AggregationTask
        if (wasNoCurrentTaskController) {
            currentTaskController = new AggregationTaskController(this, createTemporaryTask(), false);
            currentTaskController.taskStage = AggregationTaskStage.LOADING_REMOTELY;
        }

        executeTaskBackground(task, requestListener);

        if (wasNoCurrentTaskController) {
            currentTaskController = null;
        }
    }

    @Override
    public <T> void executeTaskBackground(Task<T> task,
                                          RequestListener<T> requestListener) {
        if (!checkManagersState(task)) {
            return;
        }

        CachedSpiceRequest<T> nonCachedTask = new CachedSpiceRequest<>(task, null, DurationInMillis.ALWAYS_RETURNED);
        nonCachedTask.setOffline(true);

        if (currentTaskController != null) {
            localSpiceManager.execute(nonCachedTask, wrapForAggregationTask(requestListener));
        } else {
            localSpiceManager.execute(nonCachedTask, requestListener);
        }
    }

    /* Associated lifecycle method */
    public void onPause() {
        isPaused = true;
        localSpiceManager.shouldStop();
        remoteSpiceManager.shouldStop();
    }

    /* Associated lifecycle method */
    public void onDestroy() {
        context = null;
        localSpiceManager = null;
        remoteSpiceManager = null;
    }

    private boolean checkManagersState(Object request) {
        if (!remoteSpiceManager.isStarted() || !localSpiceManager.isStarted()) {
            Lc.e(request.getClass().getName() + " is requested after onPause");
            return false;
        }
        return true;
    }

    void startWrappingRequestsAsAggregation(AggregationTaskController aggregationTaskController) {
        if (this.currentTaskController != null)
            throw new RuntimeException("You cannot start another task while current task is already set. Let current task end before start new task. Use post() method as simpliest solution");

        currentTaskController = aggregationTaskController;
    }

    void loadAggregationTask(AggregationTaskController taskController) {
        startWrappingRequestsAsAggregation(taskController);
        taskController.task.load(taskController.isInBackground, taskController.taskStage);
        stopWrapRequestsAsAggregation();
    }

    void stopWrapRequestsAsAggregation() {
        currentTaskController = null;
    }

    private <T> AggregationTaskRequestListener<T> wrapForAggregationTask(RequestListener<T> requestListener) {
        AggregationTaskRequestListener<T> result = new AggregationTaskRequestListener<>(this, currentTaskController, requestListener);
        currentTaskController.registerListener(result);
        return result;
    }
}
