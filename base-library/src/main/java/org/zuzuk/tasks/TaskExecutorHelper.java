package org.zuzuk.tasks;

import android.content.Context;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.base.RequestWrapper;
import org.zuzuk.tasks.remote.base.SpiceManagerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Helper to work with tasks execution during lifecycle of object
 */
public class TaskExecutorHelper implements RequestExecutor, TaskExecutor {
    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;
    private Context context;
    private final HashMap<AggregationTask, List<RequestListener>> aggregationTasks = new HashMap<>();
    private final HashSet<AggregationTask> temporaryAggregationTasks = new HashSet<>();
    private AggregationTask currentAggregationTask;
    private boolean isCurrentTaskTemporary;

    private void setCurrentAggregationTask(AggregationTask currentAggregationTask) {
        if (this.currentAggregationTask != null)
            throw new RuntimeException("Current aggregation task have set already");

        this.currentAggregationTask = currentAggregationTask;
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

    /**
     * Adding aggregation task that will try rise every onResume or call onLoaded
     * if loading is not needed. Add all tasks in onCreate
     */
    public void addLoadingTask(AggregationTask loadingTask) {
        aggregationTasks.put(loadingTask, new ArrayList<RequestListener>());
    }

    /* Associated lifecycle method */
    public void onResume() {
        localSpiceManager.start(context);
        remoteSpiceManager.start(context);

        reload(false);
    }

    /* Executes all tasks that needed to be reloaded */
    public void reload(boolean isInBackground) {
        for (AggregationTask aggregationTask : aggregationTasks.keySet()) {
            if (!aggregationTask.isLoaded() || aggregationTask.isLoadingNeeded()) {
                setCurrentAggregationTask(aggregationTask);
                aggregationTask.load(false);
                aggregationTask.onLoadingStarted(isInBackground);
            } else {
                aggregationTask.onLoaded();
            }
        }

        currentAggregationTask = null;
    }

    @Override
    public <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        beforeExecution();
        executeRequestBackground(request, requestListener);
        afterExecution();
    }

    @Override
    public <T> void executeRequestBackground(RemoteRequest<T> request,
                                             RequestListener<T> requestListener) {
        checkManagersState(request);
        if(currentAggregationTask != null) {
            remoteSpiceManager.execute(request, wrapToAggregationTask(requestListener));
        } else {
            remoteSpiceManager.execute(request, requestListener);
        }
    }

    @Override
    public <T> void executeRequest(RequestWrapper<T> requestWrapper,
                                   RequestListener<T> requestListener) {
        beforeExecution();
        executeRequestBackground(requestWrapper, requestListener);
        afterExecution();
    }

    @Override
    public <T> void executeRequestBackground(RequestWrapper<T> requestWrapper,
                                             RequestListener<T> requestListener) {
        checkManagersState(requestWrapper);
        if(currentAggregationTask != null) {
            requestWrapper.setRequestListener(wrapToAggregationTask(requestListener));
        } else {
            requestWrapper.setRequestListener(requestListener);
        }
        requestWrapper.execute(remoteSpiceManager);
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
        beforeExecution();
        executeTaskBackground(task, requestListener);
        afterExecution();
    }

    @Override
    public <T> void executeTaskBackground(Task<T> task,
                                          RequestListener<T> requestListener) {
        checkManagersState(task);
        if(currentAggregationTask != null) {
            localSpiceManager.execute(task, wrapToAggregationTask(requestListener));
        } else {
            localSpiceManager.execute(task, requestListener);
        }
    }

    /* Associated lifecycle method */
    public void onPause() {
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

    private void beforeExecution() {
        isCurrentTaskTemporary = currentAggregationTask == null;
        if (isCurrentTaskTemporary) {
            currentAggregationTask = createTemporaryTask();
            aggregationTasks.put(currentAggregationTask, new ArrayList<RequestListener>());
            temporaryAggregationTasks.add(currentAggregationTask);
            currentAggregationTask.onLoadingStarted(false);
        }
    }

    private void afterExecution() {
        if (isCurrentTaskTemporary) {
            currentAggregationTask = null;
        }
    }

    private <T> AggregationTaskRequestListener<T> wrapToAggregationTask(RequestListener<T> requestListener) {
        AggregationTaskRequestListener<T> result = new AggregationTaskRequestListener<>(requestListener);
        aggregationTasks.get(currentAggregationTask).add(result);
        return result;
    }

    private class AggregationTaskRequestListener<T> implements RequestListener<T> {
        private final AggregationTask parentTask;
        private final RequestListener<T> requestListener;

        private AggregationTaskRequestListener(RequestListener<T> requestListener) {
            this.parentTask = currentAggregationTask;
            this.requestListener = requestListener;
        }

        @Override
        public void onRequestSuccess(T response) {
            setCurrentAggregationTask(parentTask);
            isCurrentTaskTemporary = temporaryAggregationTasks.contains(parentTask);

            if (requestListener != null) {
                requestListener.onRequestSuccess(response);
            }

            List<RequestListener> listeners = aggregationTasks.get(parentTask);
            listeners.remove(this);
            if (listeners.isEmpty()) {
                parentTask.onLoaded();
                if (isCurrentTaskTemporary) {
                    temporaryAggregationTasks.remove(parentTask);
                    aggregationTasks.remove(parentTask);
                }
            }
            currentAggregationTask = null;
            isCurrentTaskTemporary = false;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            setCurrentAggregationTask(parentTask);
            isCurrentTaskTemporary = temporaryAggregationTasks.contains(parentTask);

            if (requestListener != null) {
                requestListener.onRequestFailure(spiceException);
            }

            List<RequestListener> listeners = aggregationTasks.get(parentTask);
            listeners.remove(this);
            if (listeners.isEmpty()) {
                parentTask.onFailed(spiceException);
                if (isCurrentTaskTemporary) {
                    temporaryAggregationTasks.remove(parentTask);
                    aggregationTasks.remove(parentTask);
                }
            }
            currentAggregationTask = null;
            isCurrentTaskTemporary = false;
        }
    }
}
