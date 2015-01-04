package org.zuzuk.tasks;

import android.content.Context;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.base.RequestWrapper;
import org.zuzuk.tasks.remote.base.SpiceManagerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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

    /* Creates task for someone who using helper not only as loader but as task executor */
    public AggregationTask createTemporaryTask() {
        throw new RuntimeException("This method should be override to use temporary tasks");
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
        AggregationTaskController controller = new AggregationTaskController(this, aggregationTask, isInBackground, false);
        executeAggregationTask(controller, isInBackground);
    }

    private void executeAggregationTask(AggregationTaskController taskController, boolean isInBackground) {
        boolean isCachedDataLoaded = taskController.task.isLoaded(true);
        // if task loaded from cache and it is not cache loading aggregation task (pre-loading)
        if (isCachedDataLoaded && !taskController.isLoadingFromCache) {
            taskController.task.onLoaded(false, true);
        }

        PreLoadingTask preLoadingTask = new PreLoadingTask(taskController, isCachedDataLoaded);
        PreLoadingTaskListener preLoadingTaskListener = new PreLoadingTaskListener(preLoadingTask, isInBackground, isCachedDataLoaded);
        executeTaskBackground(preLoadingTask, preLoadingTaskListener);
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
            currentTaskController = new AggregationTaskController(this, createTemporaryTask(), false, false);
            currentTaskController.task.onLoadingStarted(false, currentTaskController.isLoadingFromCache);
        }

        executeRequestBackground(request, requestListener);

        if (wasNoCurrentTaskController) {
            currentTaskController = null;
        }
    }

    private <T> void executeRequestBackgroundInternal(RemoteRequest<T> request,
                                                      RequestListener<T> requestListener) {
        checkManagersState(request);
        CachedSpiceRequest<T> cacheSpiceRequest = request.wrapAsCacheRequest(remoteSpiceManager);
        if (currentTaskController != null && currentTaskController.isLoadingFromCache) {
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
            currentTaskController = new AggregationTaskController(this, createTemporaryTask(), false, false);
            currentTaskController.task.onLoadingStarted(false, currentTaskController.isLoadingFromCache);
        }

        executeTaskBackground(task, requestListener);

        if (wasNoCurrentTaskController) {
            currentTaskController = null;
        }
    }

    @Override
    public <T> void executeTaskBackground(Task<T> task,
                                          RequestListener<T> requestListener) {
        checkManagersState(task);
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

    private void checkManagersState(Object request) {
        if (!remoteSpiceManager.isStarted() || !localSpiceManager.isStarted())
            throw new RuntimeException(request.getClass().getName() + " is requested after onPause");
    }

    void startWrappingRequestsAsAggregation(AggregationTaskController aggregationTaskController) {
        if (this.currentTaskController != null)
            throw new RuntimeException("You cannot start another task while current task is already set. Let current task end before start new task. Use post() method as simpliest solution");

        currentTaskController = aggregationTaskController;
    }

    void stopWrapRequestsAsAggregation() {
        currentTaskController = null;
    }

    private <T> AggregationTaskRequestListener<T> wrapForAggregationTask(RequestListener<T> requestListener) {
        AggregationTaskRequestListener<T> result = new AggregationTaskRequestListener<>(this, currentTaskController, requestListener);
        currentTaskController.registerListener(result);
        return result;
    }

    // task that executes before main loading and it tries to load cache before and also check isLoadingNeeded flag
    private class PreLoadingTask extends Task<Boolean> {
        private final AggregationTaskController taskController;
        private final boolean isCachedDataLoaded;

        PreLoadingTask(AggregationTaskController taskController, boolean isCachedDataLoaded) {
            super(Boolean.class);
            this.taskController = taskController;
            this.isCachedDataLoaded = isCachedDataLoaded;
        }

        @Override
        public Boolean execute() throws Exception {
            if (!isCachedDataLoaded && !taskController.isLoadingFromCache) {
                final CountDownLatch cacheLoadingWaiter = new CountDownLatch(1);

                AggregationTask cacheLoadingTask = new DefaultTemporaryTask() {

                    @Override
                    public void load(boolean isInBackground, boolean isFromCache) {
                        taskController.task.load(true, true);
                    }

                    @Override
                    public void onLoaded(boolean isInBackground, boolean isFromCache) {
                        cacheLoadingWaiter.countDown();
                    }

                    @Override
                    public void onFailed(boolean isInBackground, boolean isFromCache, List<Exception> exceptions) {
                        cacheLoadingWaiter.countDown();
                    }
                };

                AggregationTaskController cacheLoadingController = new AggregationTaskController(TaskExecutorHelper.this, cacheLoadingTask, true, false);
                executeAggregationTask(cacheLoadingController, false);
                //TODO: possible deadlock by thread pool, :D (should be fixed in future)
                //wait until cache loading task executes
                cacheLoadingWaiter.await();
            }

            return taskController.task.isLoadingNeeded();
        }
    }

    private class PreLoadingTaskListener implements RequestListener<Boolean> {
        private final PreLoadingTask preLoadingTask;
        private final boolean isInBackground;
        private final boolean isCachedDataLoaded;

        PreLoadingTaskListener(PreLoadingTask preLoadingTask,
                               boolean isInBackground,
                               boolean isCachedDataLoaded) {
            this.preLoadingTask = preLoadingTask;
            this.isInBackground = isInBackground;
            this.isCachedDataLoaded = isCachedDataLoaded;
        }

        @Override
        public void onRequestSuccess(Boolean isLoadingNeeded) {
            boolean isLoadedFromCacheDuringPreLoading = preLoadingTask.taskController.task.isLoaded(true);
            // if firstly there was no data loaded but it have just loaded from cache
            if (!isCachedDataLoaded && isLoadedFromCacheDuringPreLoading) {
                preLoadingTask.taskController.task.onLoaded(isInBackground, true);
            }

            if (!isLoadingNeeded) {
                return;
            }

            startWrappingRequestsAsAggregation(preLoadingTask.taskController);

            boolean isReallyInBackground = isInBackground
                    || isLoadedFromCacheDuringPreLoading
                    || preLoadingTask.taskController.isLoadingFromCache;

            // now load it seriously from network etc.
            preLoadingTask.taskController.task.load(isReallyInBackground, false);
            if (preLoadingTask.taskController.isNoOneListenToRequests()) {
                preLoadingTask.taskController.finishTask();
            } else {
                preLoadingTask.taskController.task.onLoadingStarted(isReallyInBackground, false);
            }

            stopWrapRequestsAsAggregation();
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            throw new RuntimeException(spiceException);
        }
    }

    public static class DefaultTemporaryTask implements AggregationTask {

        @Override
        public boolean isLoadingNeeded() {
            return true;
        }

        @Override
        public boolean isLoaded(boolean isFromCache) {
            return true;
        }

        @Override
        public void load(boolean isInBackground, boolean isFromCache) {
        }

        @Override
        public void onLoadingStarted(boolean isInBackground, boolean isFromCache) {
        }

        @Override
        public void onLoaded(boolean isInBackground, boolean isFromCache) {
        }

        @Override
        public void onFailed(boolean isInBackground, boolean isFromCache, List<Exception> exceptions) {
        }
    }
}
