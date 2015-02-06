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

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Helper to work with tasks execution during lifecycle of object
 */
public class TaskExecutorHelper implements RequestExecutor, TaskExecutor {
    private final WeakReference<Thread> ownerThread;
    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;
    private Context context;
    private AggregationTaskController currentTaskController;
    private boolean isPaused = true;

    public TaskExecutorHelper() {
        this.ownerThread = new WeakReference<>(Thread.currentThread());
    }

    @Override
    public SpiceManager getSpiceManager() {
        return remoteSpiceManager;
    }

    /* Returns if executor in paused state so it can't execute requests */
    boolean isPaused() {
        return isPaused;
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
    public void executeAggregationTask(AggregationTask aggregationTask) {
        if (!checkIsAggregationTaskAvailableToRun()) {
            return;
        }

        AggregationTaskController controller = new AggregationTaskController(this, aggregationTask);
        executeAggregationTask(controller);
    }

    private void executeAggregationTask(final AggregationTaskController taskController) {
        taskController.nextStep();
    }

    @Override
    public <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        executeRequestInternal(request, requestListener);
    }

    @Override
    public <T> void executeRequest(RequestWrapper<T> requestWrapper) {
        executeRequestInternal(requestWrapper.getPreparedRequest(), requestWrapper);
    }

    @Override
    public <T> void executeRealLoadingRequest(RequestWrapper<T> requestWrapper,
                                              AggregationTaskListener taskListener) {
        executeRealLoadingRequest(requestWrapper.getPreparedRequest(), requestWrapper, taskListener);
    }

    @Override
    public <T> void executeRealLoadingRequest(RemoteRequest<T> request,
                                              RequestListener<T> requestListener,
                                              AggregationTaskListener taskListener) {
        if (!checkIsAggregationTaskAvailableToRun()) {
            return;
        }

        currentTaskController = new AggregationTaskController(this, new JustRealLoadingAggregationTask(taskListener));
        currentTaskController.taskStage = AggregationTaskStage.REAL_LOADING;
        executeRequestInternal(request, requestListener);
        currentTaskController = null;
    }

    <T> void executeRequestInternal(RemoteRequest<T> request,
                                    RequestListener<T> requestListener) {
        if (!checkThread() || !checkManagersState(request)) {
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
        executeTaskInternal(task, null);
    }

    @Override
    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        executeTaskInternal(task, requestListener);
    }

    @Override
    public <T> void executeRealLoadingTask(Task<T> task,
                                           RequestListener<T> requestListener,
                                           AggregationTaskListener taskListener) {
        if (!checkIsAggregationTaskAvailableToRun()) {
            return;
        }

        currentTaskController = new AggregationTaskController(this, new JustRealLoadingAggregationTask(taskListener));
        currentTaskController.taskStage = AggregationTaskStage.REAL_LOADING;
        executeTaskInternal(task, requestListener);
        currentTaskController = null;
    }

    <T> void executeTaskInternal(Task<T> task,
                                 RequestListener<T> requestListener) {
        if (!checkThread() || !checkManagersState(task)) {
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

    private boolean checkIsAggregationTaskAvailableToRun() {
        if (currentTaskController != null) {
            Lc.fatalException(new IllegalStateException("AggregationTask cannot be loaded while another aggregation task is loading." +
                    " Make sure that you are starting AggregationTask in right moment"));
            return false;
        }
        return true;
    }

    private boolean checkManagersState(Object request) {
        if (!remoteSpiceManager.isStarted() || !localSpiceManager.isStarted()) {
            Lc.fatalException(new IllegalStateException(request.getClass().getName() + " is requested after onPause"));
            return false;
        }
        return true;
    }

    private boolean checkThread() {
        if (ownerThread.get() != Thread.currentThread()) {
            Lc.fatalException(new IllegalStateException("TaskExecutorHelper could be accessed from one thread. Create new TaskExecutorHelper for new thread"));
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
        taskController.task.load(taskController.taskStage, taskController.lastLoadedStageState);
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

    private class JustRealLoadingAggregationTask implements AggregationTask {
        private final AggregationTaskListener taskListener;

        private JustRealLoadingAggregationTask(AggregationTaskListener taskListener) {
            this.taskListener = taskListener;
        }

        @Override
        public boolean isLoadingNeeded(AggregationTaskStage currentTaskStage) {
            return true;
        }

        @Override
        public boolean isLoaded(AggregationTaskStage currentTaskStage) {
            return true;
        }

        @Override
        public void load(AggregationTaskStage currentTaskStage, AggregationTaskStageState currentTaskStageState) {
        }

        @Override
        public void onLoadingStarted(AggregationTaskStage currentTaskStage, AggregationTaskStageState currentTaskStageState) {
            if (taskListener != null) {
                taskListener.onLoadingStarted(currentTaskStage);
            }
        }

        @Override
        public void onLoaded(AggregationTaskStage currentTaskStage, AggregationTaskStageState currentTaskStageState) {
            if (taskListener != null) {
                taskListener.onLoadingFinished(currentTaskStage);
            }
        }

        @Override
        public void onFailed(AggregationTaskStage currentTaskStage, List<Exception> exceptions, AggregationTaskStageState currentTaskStageState) {
        }
    }
}
