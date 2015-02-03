package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Class that contains wrapped requests listeners and controls task state
 */
class AggregationTaskController {
    private final TaskExecutorHelper taskExecutorHelper;
    AggregationTaskStage taskStage = AggregationTaskStage.PRE_LOADING;
    // task that is controlling by this object
    final AggregationTask task;
    // listeners that is wrapped around passed into TaskExecutorHelper listener
    private final List<RequestListener> wrappedRequestListeners = new ArrayList<>();
    // fails that occurred during task execution
    private final List<Exception> collectedFails = new ArrayList<>();
    final boolean isInBackground;

    /* Returns if all observed tasks finished so controller not listen to any task execution */
    private boolean noOneListenToRequests() {
        return wrappedRequestListeners.isEmpty();
    }

    AggregationTaskController(TaskExecutorHelper taskExecutorHelper,
                              AggregationTask task,
                              boolean isInBackground) {
        this.taskExecutorHelper = taskExecutorHelper;
        this.task = task;
        this.isInBackground = isInBackground;
    }

    /* Changing state of task from PRE_LOADING to LOADED */
    void nextStep() {
        if (taskStage != AggregationTaskStage.LOADED) {
            taskExecutorHelper.executeTaskBackground(new AggregationTaskStageStateTask(this, taskStage), taskStageStateListener);
        }
    }

    /* Start listening to some task */
    void registerListener(RequestListener requestListener) {
        wrappedRequestListeners.add(requestListener);
    }

    /* Some task have failed so we collect exception */
    void addFail(Exception ex) {
        collectedFails.add(ex);
    }

    /* End listening to some task */
    void unregisterListener(RequestListener requestListener) {
        wrappedRequestListeners.remove(requestListener);
        if (noOneListenToRequests()) {
            nextStep();
        }
    }

    /* Object that listens to tasks which calculates states of stages */
    private final RequestListener<AggregationTaskStageState> taskStageStateListener = new RequestListener<AggregationTaskStageState>() {

        @Override
        public void onRequestSuccess(AggregationTaskStageState aggregationTaskStageState) {
            if (aggregationTaskStageState.isLoaded) {
                task.onLoaded(isInBackground, aggregationTaskStageState.isLoadingNeeded ? taskStage : AggregationTaskStage.LOADED);
            } else {
                task.onFailed(isInBackground, taskStage, collectedFails);
            }

            if (aggregationTaskStageState.isLoadingNeeded) {
                switch (taskStage) {
                    case PRE_LOADING:
                        taskStage = AggregationTaskStage.LOADING_LOCALLY;
                        task.onLoadingStarted(isInBackground, taskStage);
                        taskExecutorHelper.loadAggregationTask(AggregationTaskController.this);
                        break;
                    case LOADING_LOCALLY:
                        taskStage = AggregationTaskStage.LOADING_REMOTELY;
                        task.onLoadingStarted(isInBackground, taskStage);
                        taskExecutorHelper.loadAggregationTask(AggregationTaskController.this);
                        break;
                    case LOADING_REMOTELY:
                        task.onFailed(isInBackground, AggregationTaskStage.LOADED, collectedFails);
                        break;
                }
            }
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            ArrayList<Exception> exceptions = new ArrayList<>(1);
            exceptions.add(spiceException);
            task.onFailed(isInBackground, taskStage, exceptions);
        }
    };
}