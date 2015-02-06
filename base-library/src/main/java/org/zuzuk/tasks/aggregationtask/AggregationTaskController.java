package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

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
    AggregationTaskStageState stageState = new AggregationTaskStageState(AggregationTaskStage.PRE_LOADING, null);

    /* Returns if all observed tasks finished so controller not listen to any task execution */
    private boolean noOneListenToRequests() {
        return wrappedRequestListeners.isEmpty();
    }

    AggregationTaskController(TaskExecutorHelper taskExecutorHelper,
                              AggregationTask task) {
        this.taskExecutorHelper = taskExecutorHelper;
        this.task = task;
    }

    /* Changing state of task from PRE_LOADING to LOADED */
    void nextStep() {
        taskExecutorHelper.executeTaskInternal(new AggregationTaskStageStateTask(this, stageState), taskStageStateListener, true);
    }

    /* Start listening to some task */
    void registerListener(RequestListener requestListener) {
        wrappedRequestListeners.add(requestListener);
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
            if (stageState.isLoaded() == UnknownableBoolean.TRUE) {
                task.onLoaded(stageState.getTaskStage(), stageState);
            } else if (stageState.getTaskStage() != AggregationTaskStage.PRE_LOADING) {
                task.onFailed(stageState.getTaskStage(), stageState);
            }

            if (stageState.isLoadingNeeded() == UnknownableBoolean.TRUE) {
                switch (stageState.getTaskStage()) {
                    case PRE_LOADING:
                        stageState = new AggregationTaskStageState(AggregationTaskStage.LOADING_LOCALLY, stageState);
                        task.onLoadingStarted(stageState.getTaskStage(), stageState);
                        taskExecutorHelper.loadAggregationTask(AggregationTaskController.this);
                        break;
                    case LOADING_LOCALLY:
                        stageState = new AggregationTaskStageState(AggregationTaskStage.REAL_LOADING, stageState);
                        task.onLoadingStarted(stageState.getTaskStage(), stageState);
                        taskExecutorHelper.loadAggregationTask(AggregationTaskController.this);
                        break;
                }
            }
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            stageState = new AggregationTaskStageState(stageState.getTaskStage(), stageState);
            stageState.addFail(spiceException);
            task.onFailed(stageState.getTaskStage(), stageState);
            Lc.e("Failed on getting isLoaded() or isLoadingNeeded() on stage " + stageState.getTaskStage());
        }
    };
}