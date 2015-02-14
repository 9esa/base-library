package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.utils.Lc;

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
    final List<RequestListener> wrappedRequestListeners = new ArrayList<>();
    AggregationTaskStageState stageState = AggregationTaskStageState.createPreLoadingStageState();
    private boolean isWrappingTasks = false;

    /* Returns if all observed tasks finished so controller not listen to any task execution */
    private boolean noOneListenToRequests() {
        return wrappedRequestListeners.isEmpty();
    }

    AggregationTaskController(TaskExecutorHelper taskExecutorHelper, AggregationTask task) {
        this.taskExecutorHelper = taskExecutorHelper;
        this.task = task;
    }

    /* Changing state of task from PRE_LOADING to LOADED */
    @SuppressWarnings("unchecked")
    void nextStep() {
        taskExecutorHelper.executeTaskInternal(new AggregationTaskStageStateTask(this, stageState), taskStageStateListener, true, this);
    }

    /* Start listening to some task */
    void registerListener(RequestListener requestListener) {
        wrappedRequestListeners.add(requestListener);
    }

    /* End listening to some task */
    void unregisterListener(RequestListener requestListener) {
        wrappedRequestListeners.remove(requestListener);
        checkIfTaskFinished();
    }

    void checkIfTaskFinished() {
        if (noOneListenToRequests()) {
            nextStep();
        }
    }

    /* Object that listens to tasks which calculates states of stages */
    private final RequestListener<AggregationTaskStageState> taskStageStateListener = new RequestListener<AggregationTaskStageState>() {

        @Override
        public void onRequestSuccess(AggregationTaskStageState aggregationTaskStageState) {
            if (stageState.isLoaded() == UnknownableBoolean.TRUE) {
                task.onLoaded(stageState);
                stageState.notifyListenerAboutLoadSuccess();
            } else if (stageState.getTaskStage() != AggregationTaskStage.PRE_LOADING) {
                task.onFailed(stageState);
                stageState.notifyListenerAboutLoadFailure();
            }

            if (stageState.isLoadingNeeded() == UnknownableBoolean.TRUE) {
                switch (stageState.getTaskStage()) {
                    case PRE_LOADING:
                        stageState = new AggregationTaskStageState(AggregationTaskStage.LOADING_LOCALLY, stageState);
                        task.onLoadingStarted(stageState);
                        stageState.notifyListenerAboutLoadingStart();
                        loadAggregationTask();
                        break;
                    case LOADING_LOCALLY:
                        stageState = new AggregationTaskStageState(AggregationTaskStage.REAL_LOADING, stageState);
                        task.onLoadingStarted(stageState);
                        stageState.notifyListenerAboutLoadingStart();
                        loadAggregationTask();
                        break;
                }
            }
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            stageState = new AggregationTaskStageState(stageState.getTaskStage(), stageState);
            stageState.addFail(spiceException);
            task.onFailed(stageState);
            stageState.notifyListenerAboutLoadFailure();
            Lc.e("Failed on getting isLoaded() or isLoadingNeeded() on stage " + stageState.getTaskStage());
        }
    };

    @SuppressWarnings("unchecked")
    private void loadAggregationTask() {
        RequestAndTaskExecutor requestAndTaskExecutor = taskExecutorHelper.createRequestAndTaskExecutor();
        requestAndTaskExecutor.setAggregationTaskController(this);
        startWrappingRequestsAsAggregation();
        task.load(requestAndTaskExecutor, stageState);
        stopWrapRequestsAsAggregation();
        checkIfTaskFinished();
    }

    void startWrappingRequestsAsAggregation() {
        if (isWrappingTasks) {
            Lc.fatalException(new IllegalStateException("You cannot start another task while current task is already set. Let current task end before start new task. Use post() method as simpliest solution"));
        }
        isWrappingTasks = true;
    }

    void stopWrapRequestsAsAggregation() {
        isWrappingTasks = false;
    }

    boolean checkIfTaskExecutedAsPartOfAggregationTask() {
        if (!isWrappingTasks) {
            Lc.fatalException(new IllegalStateException("Any tasks ore requests should be in load() block of AggregationTask " +
                    "or in any RequestListener callback"));
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequestInternal(request, requestListener, this);
    }

    @SuppressWarnings("unchecked")
    <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        taskExecutorHelper.executeTaskInternal(task, requestListener, false, this);
    }

}