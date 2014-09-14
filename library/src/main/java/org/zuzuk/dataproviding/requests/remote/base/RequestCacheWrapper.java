package org.zuzuk.dataproviding.requests.remote.base;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.dataproviding.requests.base.ResultListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Class that supports RoboSpice caching so can be used as wrapper if request can be cached
 */
public class RequestCacheWrapper<T> {
    private final CachedSpiceRequest<T> spiceRequest;
    private final ResultListener<T> resultListener;

    public RequestCacheWrapper(RemoteRequest<T> request, ResultListener<T> resultListener) {
        spiceRequest = prepareRequest(request);
        this.resultListener = resultListener;
    }

    /* Executes cached request */
    public void execute(SpiceManager spiceManager) {
        if (spiceManager.isStarted()) {
            spiceManager.execute(spiceRequest, new RequestListener<T>() {
                @Override
                public void onRequestSuccess(T response) {
                    resultListener.onSuccess(response);
                }

                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    resultListener.onFailure(spiceException);
                }
            });
        }
    }

    private CachedSpiceRequest<T> prepareRequest(RemoteRequest<T> request) {
        CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<>(request, request.getCacheKey(), request.getCacheExpiryDuration());
        cachedSpiceRequest.setOffline(request.isOffline());
        cachedSpiceRequest.setAggregatable(true);
        return cachedSpiceRequest;
    }
}
