package org.zuzuk.tasks.aggregationtask;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Wrapped request listener to help aggregation task controller
 * to listen all requests which started inside load() method of aggregation task
 * and also all requests which started inside other requests callbacks
 */
class AggregationTaskRequestListener<T> implements RequestListener<T> {
    private final TaskExecutorHelper taskExecutorHelper;
    private final RequestListener<T> requestListener;
    private final AggregationTaskController parentTaskController;

    AggregationTaskRequestListener(TaskExecutorHelper taskExecutorHelper,
                                   AggregationTaskController parentTaskController,
                                   RequestListener<T> requestListener) {
        this.taskExecutorHelper = taskExecutorHelper;
        this.parentTaskController = parentTaskController;
        this.requestListener = requestListener;
    }

    @Override
    public void onRequestSuccess(T response) {
        if (parentTaskController.isEnded() || taskExecutorHelper.isPaused()) {
            return;
        }

        parentTaskController.startWrappingRequestsAsAggregation();
        if (requestListener != null) {
            requestListener.onRequestSuccess(response);
        }
        parentTaskController.stopWrapRequestsAsAggregation();

        parentTaskController.unregisterListener(this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        if (parentTaskController.isEnded() || taskExecutorHelper.isPaused()) {
            return;
        }

        parentTaskController.startWrappingRequestsAsAggregation();
        if (requestListener != null) {
            int countOfListeners = parentTaskController.wrappedRequestListeners.size();
            requestListener.onRequestFailure(spiceException);
            // add fail if nothing else started in failure callback
            if (countOfListeners == parentTaskController.wrappedRequestListeners.size()) {
                parentTaskController.stageState.addFail(spiceException);
            }
        } else {
            parentTaskController.stageState.addFail(spiceException);
        }
        parentTaskController.stopWrapRequestsAsAggregation();

        parentTaskController.unregisterListener(this);
    }
}