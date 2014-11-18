package org.zuzuk.tasks;

import android.content.Context;
import android.content.Intent;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.events.EventListener;
import org.zuzuk.events.EventListenerHelper;
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
public class TaskExecutorHelper implements RequestExecutor, TaskExecutor, EventListener {
    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;
    private Context context;
    private final EventListenerHelper eventListenerHelper = new EventListenerHelper(this);
    private final HashMap<AggregationTaskController, List<RequestListener>> tasksControllers = new HashMap<>();
    private final HashSet<AggregationTaskController> temporaryTasksControllers = new HashSet<>();
    private AggregationTaskController currentTaskController;
    private boolean isCurrentTaskTemporary;
    private boolean isPaused = true;

    @Override
    public SpiceManager getSpiceManager() {
        return remoteSpiceManager;
    }

    private void setCurrentTaskController(AggregationTaskController currentTaskController) {
        if (this.currentTaskController != null)
            throw new RuntimeException("You cannot start another task while current task is already set. Let current task end before start new task. Use post() method as simpliest solution");

        this.currentTaskController = currentTaskController;
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

        eventListenerHelper.onCreate(context);
    }

    /**
     * Adding aggregation task that will try rise every onResume or call onLoaded
     * if loading is not needed. Add all tasks in onCreate
     */
    public void addLoadingTask(AggregationTask loadingTask) {
        tasksControllers.put(new AggregationTaskController(loadingTask), new ArrayList<RequestListener>());
    }

    /* Associated lifecycle method */
    public void onResume() {
        localSpiceManager.start(context);
        remoteSpiceManager.start(context);
        isPaused = false;
        eventListenerHelper.onResume();
    }

    @Override
    public void onEvent(Context context, String eventName, Intent intent) {
    }

    /* Executes all tasks that needed to be reloaded */
    public void reload(final boolean isInBackground) {
        for (final AggregationTaskController taskController : tasksControllers.keySet()) {
            executeAggregationTask(taskController, isInBackground);
        }
    }

    /* Executes aggregation task */
    public void executeAggregationTask(AggregationTask aggregationTask, boolean isInBackground) {
        AggregationTaskController controller = new AggregationTaskController(aggregationTask);
        tasksControllers.put(controller, new ArrayList<RequestListener>());
        temporaryTasksControllers.add(controller);
        executeAggregationTask(controller, isInBackground);
    }

    private void executeAggregationTask(final AggregationTaskController taskController, final boolean isInBackground) {
        final boolean isLoaded = taskController.task.isLoaded();
        if (isLoaded) {
            taskController.task.onLoaded();
        }

        executeTaskBackground(new Task<Boolean>(Boolean.class) {
            @Override
            public Boolean execute() throws Exception {
                return taskController.task.isLoadingNeeded();
            }
        }, new RequestListener<Boolean>() {

            @Override
            public void onRequestSuccess(Boolean isLoadingNeeded) {
                if (isLoadingNeeded) {
                    setCurrentTaskController(taskController);
                    taskController.task.load(isInBackground || isLoaded);
                    if (!tasksControllers.get(taskController).isEmpty()) {
                        taskController.task.onLoadingStarted(isInBackground);
                    } else {
                        finishTask(taskController);
                    }
                    currentTaskController = null;
                }
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                throw new RuntimeException(spiceException);
            }
        });
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
        if (currentTaskController != null) {
            remoteSpiceManager.execute(request.wrapAsCacheRequest(), wrapToAggregationTask(requestListener));
        } else {
            remoteSpiceManager.execute(request.wrapAsCacheRequest(), requestListener);
        }
    }

    @Override
    public <T> void executeRequest(RequestWrapper<T> requestWrapper) {
        beforeExecution();
        executeRequestBackground(requestWrapper);
        afterExecution();
    }

    @Override
    public <T> void executeRequestBackground(RequestWrapper<T> requestWrapper) {
        checkManagersState(requestWrapper);
        if (currentTaskController != null) {
            remoteSpiceManager.execute(requestWrapper.getPreparedRequest().wrapAsCacheRequest(), wrapToAggregationTask(requestWrapper));
        } else {
            remoteSpiceManager.execute(requestWrapper.getPreparedRequest().wrapAsCacheRequest(), requestWrapper);
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
        beforeExecution();
        executeTaskBackground(task, requestListener);
        afterExecution();
    }

    @Override
    public <T> void executeTaskBackground(Task<T> task,
                                          RequestListener<T> requestListener) {
        checkManagersState(task);
        if (currentTaskController != null) {
            localSpiceManager.execute(task, wrapToAggregationTask(requestListener));
        } else {
            localSpiceManager.execute(task, requestListener);
        }
    }

    /* Associated lifecycle method */
    public void onPause() {
        isPaused = true;
        localSpiceManager.shouldStop();
        remoteSpiceManager.shouldStop();
        eventListenerHelper.onPause();
    }

    /* Associated lifecycle method */
    public void onDestroy() {
        context = null;
        localSpiceManager = null;
        remoteSpiceManager = null;
        eventListenerHelper.onDestroy();
    }

    private void checkManagersState(Object request) {
        if (!remoteSpiceManager.isStarted() || !localSpiceManager.isStarted())
            throw new RuntimeException(request.getClass().getName() + " is requested after onPause");
    }

    private void beforeExecution() {
        isCurrentTaskTemporary = currentTaskController == null;
        if (isCurrentTaskTemporary) {
            currentTaskController = new AggregationTaskController(createTemporaryTask());
            tasksControllers.put(currentTaskController, new ArrayList<RequestListener>());
            temporaryTasksControllers.add(currentTaskController);
            currentTaskController.task.onLoadingStarted(false);
        }
    }

    private void afterExecution() {
        if (isCurrentTaskTemporary) {
            currentTaskController = null;
        }
    }

    private <T> AggregationTaskRequestListener<T> wrapToAggregationTask(RequestListener<T> requestListener) {
        AggregationTaskRequestListener<T> result = new AggregationTaskRequestListener<>(requestListener);
        tasksControllers.get(currentTaskController).add(result);
        return result;
    }

    private void finishTask(AggregationTaskController controller) {
        if (controller.task.isLoaded()) {
            controller.task.onLoaded();
        } else {
            controller.task.onFailed(controller.fails.size() > 0 ? controller.fails.get(controller.fails.size() - 1) : null);
        }

        if (isCurrentTaskTemporary) {
            temporaryTasksControllers.remove(controller);
            tasksControllers.remove(controller);
        }
    }

    private class AggregationTaskController {
        private final AggregationTask task;
        private final List<Exception> fails = new ArrayList<>();

        private void addFail(Exception ex) {
            fails.add(ex);
        }

        private AggregationTaskController(AggregationTask task) {
            this.task = task;
        }
    }

    private class AggregationTaskRequestListener<T> implements RequestListener<T> {
        private final RequestListener<T> requestListener;
        private final AggregationTaskController parentTaskController;

        private AggregationTaskRequestListener(RequestListener<T> requestListener) {
            this.parentTaskController = currentTaskController;
            this.requestListener = requestListener;
        }

        @Override
        public void onRequestSuccess(T response) {
            if (isPaused) {
                return;
            }

            setCurrentTaskController(parentTaskController);
            isCurrentTaskTemporary = temporaryTasksControllers.contains(parentTaskController);

            if (requestListener != null) {
                requestListener.onRequestSuccess(response);
            }

            List<RequestListener> listeners = tasksControllers.get(parentTaskController);
            listeners.remove(this);

            if (listeners.isEmpty()) {
                finishTask(parentTaskController);
            }

            currentTaskController = null;
            isCurrentTaskTemporary = false;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            if (isPaused) {
                return;
            }

            setCurrentTaskController(parentTaskController);
            isCurrentTaskTemporary = temporaryTasksControllers.contains(parentTaskController);

            if (requestListener != null) {
                requestListener.onRequestFailure(spiceException);
            }

            List<RequestListener> listeners = tasksControllers.get(parentTaskController);
            listeners.remove(this);
            parentTaskController.addFail(spiceException);

            if (listeners.isEmpty()) {
                finishTask(parentTaskController);
            }

            currentTaskController = null;
            isCurrentTaskTemporary = false;
        }
    }

    public static class DefaultTemporaryTask implements AggregationTask {

        @Override
        public boolean isLoadingNeeded() {
            return false;
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public void load(boolean b) {
        }

        @Override
        public void onLoadingStarted(boolean b) {
        }

        @Override
        public void onLoaded() {
        }

        @Override
        public void onFailed(Exception e) {
        }
    }
}
