package org.zuzuk.tasks.remote;

import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestWrapper;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Class that supports RoboSpice caching so can be used as wrapper if request can be cached
 */
public class RequestCacheWrapper<T> extends RequestWrapper<T> {
    private final CachedSpiceRequest<T> spiceRequest;

    public RequestCacheWrapper(RemoteRequest<T> request) {
        spiceRequest = prepareRequest(request);
    }

    @Override
    protected CachedSpiceRequest<T> getPreparedRequest() {
        return spiceRequest;
    }

    private CachedSpiceRequest<T> prepareRequest(RemoteRequest<T> request) {
        CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<>(request, request.getCacheKey(), request.getCacheExpiryDuration());
        cachedSpiceRequest.setOffline(request.isOffline());
        cachedSpiceRequest.setAggregatable(true);
        cachedSpiceRequest.setAcceptingDirtyCache(request.isAcceptDirtyCache());
        return cachedSpiceRequest;
    }
}
