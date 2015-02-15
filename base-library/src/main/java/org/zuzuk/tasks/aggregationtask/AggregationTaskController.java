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
    RequestAndTaskExecutor requestAndTaskExecutor;

    /* Returns if all observed tasks finished so controller not listen to any task execution */
    private boolean noOneListenToRequests() {
        return wrappedRequestListeners.isEmpty();
    }

    AggregationTaskController(TaskExecutorHelper taskExecutorHelper, AggregationTask task) {
        this.taskExecutorHelper = taskExecutorHelper;
        this.task = task;
    }

    /* Changing state of task from PRE_LOADING to LOADED */
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
            if (stageState.isLoaded()) {
                task.onLoaded(stageState);
                stageState.notifyListenerAboutLoadSuccess();
            } else if (stageState.getTaskStage() != AggregationTaskStage.PRE_LOADING) {
                task.onFailed(stageState);
                stageState.notifyListenerAboutLoadFailure();
            }

            if (stageState.isLoadingNeeded()) {
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
        startWrappingRequestsAsAggregation();
        requestAndTaskExecutor = taskExecutorHelper.createRequestAndTaskExecutor();
        requestAndTaskExecutor.setAggregationTaskController(this);
        stageState.setIsTaskWrapped(false);
        task.load(requestAndTaskExecutor, stageState);
        stageState.setIsTaskWrapped(null);
        stopWrapRequestsAsAggregation();
        checkIfTaskFinished();
    }

    <T> void executeRequest(RemoteRequest<T> request,
                            RequestListener<T> requestListener,
                            TaskProcessor taskProcessor) {
        if (taskProcessor != null) {
            taskProcessor.processTask(request, stageState);
        }
        taskExecutorHelper.executeRequestInternal(request, requestListener, this);
    }

    <T> void executeTask(Task<T> task,
                         RequestListener<T> requestListener,
                         TaskProcessor taskProcessor) {
        if (taskProcessor != null) {
            taskProcessor.processTask(task, stageState);
        }
        taskExecutorHelper.executeTaskInternal(task, requestListener, false, this);
    }

    void executeWrappedAggregationTask(AggregationTask aggregationTask) {
        stageState.setIsTaskWrapped(true);
        aggregationTask.load(requestAndTaskExecutor, stageState);
        stageState.setIsTaskWrapped(null);
    }

    boolean checkIfTaskExecutedAsPartOfAggregationTask() {
        if (!isWrappingTasks) {
            Lc.fatalException(new IllegalStateException("Any tasks or requests should be in load() block of AggregationTask or in any RequestListener callback"));
            return false;
        }
        return true;
    }

    void startWrappingRequestsAsAggregation() {
        isWrappingTasks = true;
    }

    void stopWrapRequestsAsAggregation() {
        isWrappingTasks = false;
    }

}