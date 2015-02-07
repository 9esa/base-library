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
public class TaskExecutorHelper implements RequestExecutor, TaskExecutor {
    private final Handler postHandler = new Handler();
    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;
    private AggregationTaskController currentTaskController;
    private boolean isPaused = true;

    public TaskExecutorHelper() {
    }

    public TaskExecutorHelper(SpiceManagerProvider spiceManagerProvider) {
        localSpiceManager = spiceManagerProvider.createLocalSpiceManager();
        remoteSpiceManager = spiceManagerProvider.createRemoteSpiceManager();
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

    /* Executes aggregation task */
    public void executeAggregationTask(final AggregationTask aggregationTask) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!checkIsAggregationTaskAvailableToRun()) {
                    return;
                }

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
        executeRequestInternal(request, requestListener, false);
    }

    @Override
    public <T> void executeRealLoadingRequest(final RemoteRequest<T> request,
                                              final RequestListener<T> requestListener,
                                              final AggregationTaskListener taskListener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!checkIsAggregationTaskAvailableToRun()) {
                    return;
                }

                if (isPaused) {
                    return;
                }

                currentTaskController = new AggregationTaskController(TaskExecutorHelper.this, new JustRealLoadingAggregationTask(taskListener));
                currentTaskController.stageState = new AggregationTaskStageState(AggregationTaskStage.REAL_LOADING, null);
                executeRequestInternal(request, requestListener, false);
                currentTaskController = null;
            }
        });
    }

    private <T> void executeRequestInternal(final RemoteRequest<T> request,
                                            final RequestListener<T> requestListener,
                                            final boolean doNotWrap) {
        if (!checkManagersState(request)
                || (!doNotWrap && !checkIfTaskExecutedAsPartOfAggregationTask())) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                CachedSpiceRequest<T> cacheSpiceRequest = request.wrapAsCacheRequest(remoteSpiceManager,
                        currentTaskController.stageState.getTaskStage() == AggregationTaskStage.LOADING_LOCALLY);

                if (doNotWrap) {
                    remoteSpiceManager.execute(cacheSpiceRequest, requestListener);
                } else {
                    remoteSpiceManager.execute(cacheSpiceRequest, wrapForAggregationTask(requestListener));
                }
            }
        });
    }

    @Override
    public void executeTask(LocalTask task) {
        executeTaskInternal(task, null, false);
    }

    @Override
    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        executeTaskInternal(task, requestListener, false);
    }

    @Override
    public <T> void executeRealLoadingTask(final Task<T> task,
                                           final RequestListener<T> requestListener,
                                           final AggregationTaskListener taskListener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!checkIsAggregationTaskAvailableToRun()) {
                    return;
                }

                if (isPaused) {
                    return;
                }

                currentTaskController = new AggregationTaskController(TaskExecutorHelper.this, new JustRealLoadingAggregationTask(taskListener));
                currentTaskController.stageState = new AggregationTaskStageState(AggregationTaskStage.REAL_LOADING, null);
                executeTaskInternal(task, requestListener, false);
                currentTaskController = null;
            }
        });
    }

    <T> void executeTaskInternal(final Task<T> task,
                                 final RequestListener<T> requestListener,
                                 final boolean doNotWrap) {
        if (!checkManagersState(task)
                || (!doNotWrap && !checkIfTaskExecutedAsPartOfAggregationTask())) {
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
                    localSpiceManager.execute(nonCachedTask, wrapForAggregationTask(requestListener));
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

    private boolean checkIsAggregationTaskAvailableToRun() {
        if (currentTaskController != null) {
            Lc.fatalException(new IllegalStateException("AggregationTask cannot be loaded while another aggregation task is loading." +
                    " Make sure that you are starting AggregationTask in right moment"));
            return false;
        }
        return true;
    }

    private boolean checkIfTaskExecutedAsPartOfAggregationTask() {
        if (currentTaskController == null) {
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
        taskController.task.load(taskController.stageState.getTaskStage(), taskController.stageState);
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

    private void runOnUiThread(Runnable runnable) {
        if (UIUtils.isCurrentThreadMain()) {
            runnable.run();
        } else {
            postHandler.post(runnable);
        }
    }

    private class JustRealLoadingAggregationTask implements AggregationTask {
        private final AggregationTaskListener taskListener;

        private JustRealLoadingAggregationTask(AggregationTaskListener taskListener) {
            this.taskListener = taskListener;
        }

        @Override
        public boolean isLoadingNeeded(AggregationTaskStage currentTaskStage, AggregationTaskStageState currentTaskStageState) {
            return true;
        }

        @Override
        public boolean isLoaded(AggregationTaskStage currentTaskStage, AggregationTaskStageState currentTaskStageState) {
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
        public void onFailed(AggregationTaskStage currentTaskStage, AggregationTaskStageState currentTaskStageState) {
        }
    }
}
