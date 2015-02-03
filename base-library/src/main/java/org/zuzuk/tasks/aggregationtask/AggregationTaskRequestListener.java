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
        if (taskExecutorHelper.isPaused()) {
            return;
        }

        taskExecutorHelper.startWrappingRequestsAsAggregation(parentTaskController);
        if (requestListener != null) {
            requestListener.onRequestSuccess(response);
        }
        taskExecutorHelper.stopWrapRequestsAsAggregation();

        parentTaskController.unregisterListener(this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        if (taskExecutorHelper.isPaused()) {
            return;
        }

        taskExecutorHelper.startWrappingRequestsAsAggregation(parentTaskController);
        if (requestListener != null) {
            requestListener.onRequestFailure(spiceException);
        }
        parentTaskController.addFail(spiceException);
        taskExecutorHelper.stopWrapRequestsAsAggregation();

        parentTaskController.unregisterListener(this);
    }
}