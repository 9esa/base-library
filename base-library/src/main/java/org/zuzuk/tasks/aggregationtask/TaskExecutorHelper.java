package org.zuzuk.tasks.aggregationtask;

import android.content.Context;
import android.os.Handler;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.SpiceManagerProvider;
import org.zuzuk.ui.UIUtils;
import org.zuzuk.utils.Lc;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Helper to work with tasks execution during lifecycle of object
 */
public abstract class TaskExecutorHelper<TRequestAndTaskExecutor extends RequestAndTaskExecutor> implements AggregationTaskExecutor<TRequestAndTaskExecutor> {

    private final Handler postHandler = new Handler();

    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;

    private boolean isPaused = true;

    /* Returns if executor in paused state so it can't execute requests */
    boolean isPaused() {
        return isPaused;
    }

    protected abstract TRequestAndTaskExecutor createRequestAndTaskExecutor();

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
    public void executeAggregationTask(final AggregationTask<TRequestAndTaskExecutor> aggregationTask) {
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

    private void executeAggregationTaskInternal(final AggregationTaskController taskController) {
        taskController.nextStep();
    }

    <T> void executeRequestInternal(final RemoteRequest<T> request,
                                            final RequestListener<T> requestListener,
                                            final AggregationTaskController aggregationTaskController) {
        if (!checkManagersState(request)) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                aggregationTaskController.task.processTask(request, aggregationTaskController.stageState);
                CachedSpiceRequest<T> cacheSpiceRequest = request.wrapAsCacheRequest(remoteSpiceManager,
                        aggregationTaskController.stageState.getTaskStage() == AggregationTaskStage.LOADING_LOCALLY);

                remoteSpiceManager.execute(cacheSpiceRequest, wrapForAggregationTask(requestListener, aggregationTaskController));
            }
        });
    }

    <T> void executeTaskInternal(final Task<T> task,
                                 final RequestListener<T> requestListener,
                                 final boolean doNotWrap, final AggregationTaskController aggregationTaskController) {
        if (!checkManagersState(task)) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    return;
                }

                aggregationTaskController.task.processTask(task, aggregationTaskController.stageState);
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
