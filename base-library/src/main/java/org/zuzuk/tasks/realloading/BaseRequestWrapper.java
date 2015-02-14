package org.zuzuk.tasks.realloading;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;
import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;
import org.zuzuk.tasks.remote.base.RemoteRequest;

public class BaseRequestWrapper extends OnlyRealLoadingAggregationTask {

    private final RemoteRequest request;
    private final ChainedRequestListener chainedRequestListener;

    public <TResult> BaseRequestWrapper(RemoteRequest<TResult> request,
                                        ChainedRequestListener<TResult> chainedRequestListener,
                                        RealLoadingAggregationTaskListener realLoadingAggregationTaskListener) {
        super(realLoadingAggregationTaskListener);
        this.request = request;
        this.chainedRequestListener = chainedRequestListener;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void realLoad(final RequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
        executor.executeRequest(request, new RequestListener() {
            @Override
            public void onRequestFailure(SpiceException exception) {
                if (chainedRequestListener != null) {
                    chainedRequestListener.onRequestFailure(exception, executor);
                }
            }

            @Override
            public void onRequestSuccess(Object result) {
                if (chainedRequestListener != null) {
                    chainedRequestListener.onRequestSuccess(result, executor);
                }
            }
        });
    }

}
