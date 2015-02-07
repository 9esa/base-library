package org.zuzuk.tasks.aggregationtask;

import android.content.Context;
import android.os.Handler;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.base.SpiceManagerProvider;
import org.zuzuk.ui.UIUtils;
import org.zuzuk.utils.Lc;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Helper to work with tasks execution during lifecycle of object
 */
public class TaskExecutorHelper implements RequestExecutor, TaskExecutor, AggregationTaskExecutor {
    private final Handler postHandler = new Handler();
    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;
    private AggregationTaskController currentTaskController;
    private boolean isPaused = true;

    @Override
    public SpiceManager getRemoteSpiceManager() {
        return remoteSpiceManager;
    }

    @Override
    public SpiceManager getLocalSpiceManager() {
        return localSpiceManager;
    }

    /* Returns if executor in paused state so it can't execute requests */
    boolean isPaused() {
        return isPaused;
    }

    /* Associated lifecycle method */
    public void onResume(Context context) {
        if (localSpiceManager == null || remoteSpiceManager == null) {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext instanceof SpiceManagerProvider) {
                localSpiceManager = ((SpiceManagerProvider) applicationContext).createLocalSpiceManager();
                remoteSpiceManager = ((SpiceManagerProvider) applicationContext).createRemoteSpiceManager();
            } else
                throw new RuntimeException("To use TaskExecutorHelper you should pass SpiceManagerProvider into constructor or your Application class should implement SpiceManagerProvider");
        }

        localSpiceManager.start(context);
        remoteSpiceManager.start(context);
        isPaused = false;
    }

    @Override
    public void executeAggregationTask(final AggregationTask aggregationTask) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                AggregationTaskController controller = new AggregationTaskController(TaskExecutorHelper.this, aggregationTask);
                executeAggregationTask(controller);
            }
        });
    }

    private void executeAggregationTask(final AggregationTaskController taskController) {
        taskController.nextStep();
    }

    @Override
    public <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        executeRequestInternal(request, requestListener, currentTaskController);
    }

    @Override
    public <T> void executeRealLoadingRequest(final RemoteRequest<T> request,
                                              final RequestListener<T> requestListener,
                                              final AggregationTaskListener taskListener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                AggregationTaskController aggregationTaskController = new AggregationTaskController(TaskExecutorHelper.this, new JustRealLoadingAggregationTask(taskListener));
                aggregationTaskController.stageState = new AggregationTaskStageState(AggregationTaskStage.REAL_LOADING, null);
                executeRequestInternal(request, requestListener, aggregationTaskController);
            }
        });
    }

    private <T> void executeRequestInternal(final RemoteRequest<T> request,
                                            final RequestListener<T> requestListener,
                                            final AggregationTaskController aggregationTaskController) {
        if (!checkManagersState(request) || !checkIfTaskExecutedAsPartOfAggregationTask(aggregationTaskController)) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                CachedSpiceRequest<T> cacheSpiceRequest = request.wrapAsCacheRequest(remoteSpiceManager,
                        aggregationTaskController.stageState.getTaskStage() == AggregationTaskStage.LOADING_LOCALLY);

                remoteSpiceManager.execute(cacheSpiceRequest, wrapForAggregationTask(requestListener, aggregationTaskController));
            }
        });
    }

    @Override
    public void executeTask(LocalTask task) {
        executeTaskInternal(task, null, false, currentTaskController);
    }

    @Override
    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        executeTaskInternal(task, requestListener, false, currentTaskController);
    }

    @Override
    public <T> void executeRealLoadingTask(final Task<T> task,
                                           final RequestListener<T> requestListener,
                                           final AggregationTaskListener taskListener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                AggregationTaskController aggregationTaskController = new AggregationTaskController(TaskExecutorHelper.this, new JustRealLoadingAggregationTask(taskListener));
                aggregationTaskController.stageState = new AggregationTaskStageState(AggregationTaskStage.REAL_LOADING, null);
                executeTaskInternal(task, requestListener, false, aggregationTaskController);
            }
        });
    }

    <T> void executeTaskInternal(final Task<T> task,
                                 final RequestListener<T> requestListener,
                                 final boolean doNotWrap, final AggregationTaskController aggregationTaskController) {
        if (!checkManagersState(task)
                || (!doNotWrap && !checkIfTaskExecutedAsPartOfAggregationTask(aggregationTaskController))) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                CachedSpiceRequest<T> nonCachedTask = new CachedSpiceRequest<>(task, null, DurationInMillis.ALWAYS_RETURNED);
                nonCachedTask.setOffline(true);
                if (doNotWrap) {
                    localSpiceManager.execute(nonCachedTask, requestListener);
                } else {
                    localSpiceManager.execute(nonCachedTask, wrapForAggregationTask(requestListener, aggregationTaskController));
                }
            }
        });
    }

    /* Associated lifecycle method */
    public void onPause() {
        isPaused = true;
        localSpiceManager.shouldStop();
        remoteSpiceManager.shouldStop();
    }

    private boolean checkManagersState(Object request) {
        if (!remoteSpiceManager.isStarted() || !localSpiceManager.isStarted()) {
            Lc.fatalException(new IllegalStateException(request.getClass().getName() + " is requested after onPause"));
            return false;
        }
        return true;
    }

    private boolean checkIfTaskExecutedAsPartOfAggregationTask(AggregationTaskController aggregationTaskController) {
        if (aggregationTaskController == null) {
            Lc.fatalException(new IllegalStateException("Any tasks ore requests should be in load() block of AggregationTask " +
                    "or in any RequestListener callback"));
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
        taskController.task.load(taskController.stageState);
        stopWrapRequestsAsAggregation();
    }

    void stopWrapRequestsAsAggregation() {
        currentTaskController = null;
    }

    private <T> AggregationTaskRequestListener<T> wrapForAggregationTask(RequestListener<T> requestListener, AggregationTaskController aggregationTaskController) {
        AggregationTaskRequestListener<T> result = new AggregationTaskRequestListener<>(this, aggregationTaskController, requestListener);
        aggregationTaskController.registerListener(result);
        return result;
    }

    private void runOnUiThread(Runnable runnable) {
        if (UIUtils.isCurrentThreadMain()) {
            runnable.run();
        } else {
            postHandler.post(runnable);
        }
    }

}
