package org.zuzuk.tasks.aggregationtask;

import android.content.Context;
import android.os.Handler;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.local.LocalSpiceService;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.TaskExecutorHelperCreator;
import org.zuzuk.tasks.remote.cache.ORMLiteDatabaseCacheService;
import org.zuzuk.ui.UIUtils;
import org.zuzuk.utils.Lc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Helper to work with tasks execution during lifecycle of object
 */
public class TaskExecutorHelper implements AggregationTaskExecutor {

    private final Handler postHandler = new Handler();

    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;

    private boolean isPaused = true;
    final List<AggregationTaskController> controllers = new ArrayList<>();

    /* Returns if executor in paused state so it can't execute requests */
    boolean isPaused() {
        return isPaused;
    }

    protected RequestAndTaskExecutor createRequestAndTaskExecutor() {
        return new RequestAndTaskExecutor();
    }

    protected SpiceManager createLocalSpiceManager() {
        return new SpiceManager(LocalSpiceService.class);
    }

    protected SpiceManager createRemoteSpiceManager() {
        return new SpiceManager(ORMLiteDatabaseCacheService.class);
    }

    protected TaskExecutorHelper() {
    }

    public static TaskExecutorHelper newInstance(Context context) {
        return ((TaskExecutorHelperCreator) context.getApplicationContext()).createTaskExecutorHelper();
    }

    /* Associated lifecycle method */
    public void onResume(Context context) {
        if (localSpiceManager == null || remoteSpiceManager == null) {
            localSpiceManager = createLocalSpiceManager();
            remoteSpiceManager = createRemoteSpiceManager();
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

                executeAggregationTaskInternal(new AggregationTaskController(TaskExecutorHelper.this, aggregationTask));
            }
        });
    }

    @Override
    public void cancelAggregationTask(final AggregationTask aggregationTask) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AggregationTaskController controllerToCancel = null;
                for (AggregationTaskController controller : controllers) {
                    if (controller.task == aggregationTask) {
                        controllerToCancel = controller;
                        break;
                    }
                }
                if (controllerToCancel != null) {
                    controllerToCancel.endTask();
                }
            }
        });
    }

    private void executeAggregationTaskInternal(final AggregationTaskController taskController) {
        taskController.nextStep();
    }

    <T> void executeRequestInternal(final RemoteRequest<T> request,
                                    final RequestListener<T> requestListener,
                                    final AggregationTaskController aggregationTaskController) {
        if (!checkManagersState(request) || !aggregationTaskController.checkIfTaskExecutedAsPartOfAggregationTask()) {
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

    <T> void executeTaskInternal(final Task<T> task,
                                 final RequestListener<T> requestListener,
                                 final boolean doNotWrap, final AggregationTaskController aggregationTaskController) {
        if (!checkManagersState(task)
                || (!doNotWrap && !aggregationTaskController.checkIfTaskExecutedAsPartOfAggregationTask())) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                CachedSpiceRequest<T> nonCachedTask = new CachedSpiceRequest<>(task, null, DurationInMillis.ALWAYS_EXPIRED);
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
            Lc.e(request.getClass().getName() + " is requested after onPause");
            return false;
        }
        return true;
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

    public SpiceManager getLocalSpiceManager() {
        return localSpiceManager;
    }

    public SpiceManager getRemoteSpiceManager() {
        return remoteSpiceManager;
    }

}
